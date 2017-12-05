/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.kb.sparql;

import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 * @author Lorenz Buehmann
 *
 */
public class ConciseBoundedDescriptionGeneratorImpl implements ConciseBoundedDescriptionGenerator{
	
	private static final Logger logger = LoggerFactory.getLogger(ConciseBoundedDescriptionGeneratorImpl.class);
	
	private Set<String> allowedPropertyNamespaces = new TreeSet<>();
	private Set<String> allowedObjectNamespaces = new TreeSet<>();
	private Set<String> allowedClassNamespaces = new TreeSet<>();
	private Set<String> ignoredProperties = new TreeSet<>();
	
	private QueryExecutionFactory qef;

	private boolean useSingleQuery = false;
	
	public ConciseBoundedDescriptionGeneratorImpl(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, String cacheDir) {
		qef = FluentQueryExecutionFactory
				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs())
				.config().withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee())
						.setModelContentType(WebContent.contentTypeRDFXML))
				.end()
				.create();

		if(cacheDir != null){
				long timeToLive = TimeUnit.DAYS.toMillis(30);
				CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend(cacheDir, true, timeToLive);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		}
		qef = new QueryExecutionFactoryPaginated(qef, 10000);
	}

	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint) {
		this(endpoint, null);
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(Model model) {
		this(new QueryExecutionFactoryModel(model));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	@Override
	public Model getConciseBoundedDescription(String resource, int depth, boolean withTypesForLeafs) {
		logger.trace("Computing CBD for {} ...", resource);
		long start = System.currentTimeMillis();
		String query = generateQuery(resource, depth, withTypesForLeafs);
		logger.info(query);
		try(QueryExecution qe = qef.createQueryExecution(query)) {
			Model model = qe.execConstruct();
			logger.trace("Got {} triples in {} ms.", model.size(), (System.currentTimeMillis() - start));
			return model;
		} catch (Exception e) {
			logger.error("Failed to computed CBD for resource {}", resource );
			throw new RuntimeException("Failed to computed CBD for resource " + resource, e);
		}
	}

	@Override
	public Model getConciseBoundedDescription(Set<String> resources, int depth, boolean withTypesForLeafs) {
		if (useSingleQuery) {
			logger.trace("Computing CBDs for {} ...", resources);
			long start = System.currentTimeMillis();
			// build the template
			ParameterizedSparqlString template = generateQueryTemplate(depth, withTypesForLeafs, true);

			// set the VALUES clause
			String query = template.toString().replace("%VALUES%", resources.stream().map(r -> "<" + r + ">").collect(Collectors.joining(" ")));
			logger.trace(query);

			try (QueryExecution qe = qef.createQueryExecution(query)) {
				Model model = qe.execConstruct();
				logger.trace("Got {} triples in {} ms.", model.size(), (System.currentTimeMillis() - start));
				return model;
			} catch (Exception e) {
				logger.error("Failed to computed CBD for resources {}", resources);
				throw new RuntimeException("Failed to computed CBD for resource " + resources, e);
			}
		} else {
			return ConciseBoundedDescriptionGenerator.super.getConciseBoundedDescription(resources, depth, withTypesForLeafs);
		}
	}

	@Override
	public void setAllowedPropertyNamespaces(Set<String> namespaces) {
		this.allowedPropertyNamespaces = namespaces;
	}
	
	@Override
	public void setAllowedObjectNamespaces(Set<String> namespaces) {
		this.allowedObjectNamespaces = namespaces;
	}

	public void setAllowedClassNamespaces(Set<String> allowedClassNamespaces) {
		this.allowedClassNamespaces = allowedClassNamespaces;
	}

	@Override
	public void setIgnoredProperties(Set<String> properties) {
		this.ignoredProperties = properties;
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param resource The example resource for which a CONSTRUCT query is created.
	 * @return the SPARQL query
	 */
	private String generateQuery(String resource, int depth, boolean withTypesForLeafs){
		ParameterizedSparqlString template = generateQueryTemplate(depth, withTypesForLeafs, false);
		template.setIri("s", resource);
		return template.toString();
	}

	private ParameterizedSparqlString generateQueryTemplate(int depth, boolean withTypesForLeafs, boolean withValuesSubjectAnchor){
		int lastIndex = Math.max(0, depth - 1);


		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append(triplePattern("?s", "?p0", "?o0"));
//		sb.append("?p0 a ?type0.\n");
		for(int i = 1; i < depth; i++){
			sb.append(triplePattern("?o" + (i-1), "?p" + i, "?o" + i));
		}
		if(withTypesForLeafs){
			sb.append("?o").append(lastIndex).append(" a ?type.\n");
		}
		sb.append("} WHERE {\n");
		if(withValuesSubjectAnchor) {
			sb.append("VALUES ?s {%VALUES%}");
		}
		sb.append(triplePattern("?s", "?p0", "?o0"));
		sb.append(createPropertyFilter(Var.alloc("p0")));
		sb.append(createObjectFilter(Var.alloc("p0"), Var.alloc("o0")));
//		sb.append("?p0 a ?type0.\n");
		for(int i = 1; i < depth; i++){
			sb.append("OPTIONAL{\n");
			sb.append(triplePattern("?o" + (i-1), "?p" + i, "?o" + i));
			sb.append(createPropertyFilter(Var.alloc("p" + i)));
			sb.append(createObjectFilter(Var.alloc("p" + i), Var.alloc("o" + i)));
		}
		if(withTypesForLeafs){
			sb.append("OPTIONAL{?o").append(lastIndex).append(" a ?type.}\n");
		}
		for(int i = 1; i < depth; i++){
			sb.append("}");
		}
		sb.append("}\n");

		return new ParameterizedSparqlString(sb.toString());
	}

	private static final String TP = "%s %s %s .\n";
	private String triplePattern(String s, String p, String o) {
		return String.format(TP, s, p, o);
	}

	private boolean USE_FILTER_IN = true;
	private static final String FILTER_NOT_IN_CLAUSE = "%s NOT IN (%s)";
	private static final String FILTER_IN_CLAUSE = "%s IN (%s)";
	
	private String createPropertyFilter(final Var var) {
		String filter = "";
		
		if(!ignoredProperties.isEmpty()) {
			filter += "FILTER(";
			if(USE_FILTER_IN) {
				filter += String.format(
						FILTER_NOT_IN_CLAUSE,
						var.toString(),
						ignoredProperties.stream()
								.map(p -> "<" + p + ">")
								.collect(Collectors.joining(",")));
			} else {
				filter += ignoredProperties.stream()
						.map(input -> var.toString() + " != <" + input + ">")
						.collect(Collectors.joining(" && "));
			}
			filter += ")\n";
		}

		if(!allowedPropertyNamespaces.isEmpty()){
			filter += "FILTER(" + var + " = <" + RDF.type.getURI() + "> || ";
			filter += allowedPropertyNamespaces.stream()
					.map(ns -> "(STRSTARTS(STR(" + var + "),'" + ns + "'))")
					.collect(Collectors.joining(" || "));
			filter += ")\n";
		}
		
		return filter;
	}
	
	private String createObjectFilter(Var predicateVar, Var targetVar){
		String filter = "";
		if(!allowedObjectNamespaces.isEmpty() || !allowedClassNamespaces.isEmpty()) {
			filter += "FILTER(ISLITERAL(" + targetVar + ")";
		}

		if(!allowedObjectNamespaces.isEmpty()){
			filter += " || (" + predicateVar + " != " + FmtUtils.stringForResource(RDF.type) + " && ";
			filter += allowedObjectNamespaces.stream()
					.map(ns -> "(STRSTARTS(STR(" + targetVar + "),'" + ns + "'))")
					.collect(Collectors.joining(" || "));
			filter += ")\n";
		} else if(!allowedClassNamespaces.isEmpty()){
			filter += predicateVar + " != " + FmtUtils.stringForResource(RDF.type) + " || ";
		}

		if(!allowedClassNamespaces.isEmpty()){
//			if(allowedObjectNamespaces.isEmpty()) {
//				filter += predicateVar + " != " + FmtUtils.stringForResource(RDF.type) + " || ";
//			}
			filter += "|| (" + predicateVar + " = " + FmtUtils.stringForResource(RDF.type) + " && ";
			filter += allowedClassNamespaces.stream()
					.map(ns -> "(STRSTARTS(STR(" + targetVar + "),'" + ns + "'))")
					.collect(Collectors.joining(" || "));
			filter += ")\n";
		} else if(!allowedObjectNamespaces.isEmpty()){
			filter += " || " + predicateVar + " = " + FmtUtils.stringForResource(RDF.type);
		}

		if(!allowedObjectNamespaces.isEmpty() || !allowedClassNamespaces.isEmpty()) {
			filter += ")";
		}
		return filter;
	}
	
	public static void main(String[] args) {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
		Set<String> ignoredProperties = Sets.newHashSet(
				"http://dbpedia.org/ontology/abstract",
				"http://dbpedia.org/ontology/wikiPageID",
				"http://dbpedia.org/ontology/wikiPageRevisionID",
				"http://dbpedia.org/ontology/wikiPageID");

		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(endpoint);
//		cbdGen.setIgnoredProperties(ignoredProperties);
//		cbdGen.setAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));
//		cbdGen.setAllowedClassNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));
//		cbdGen.setAllowedObjectNamespaces(Sets.newHashSet("http://dbpedia.org/resource/"));
		cbdGen = new CachingConciseBoundedDescriptionGenerator(cbdGen);
//		cbdGen.setRestrictToNamespaces(Arrays.asList(new String[]{"http://dbpedia.org/ontology/", RDF.getURI(), RDFS.getURI()}));
		Model cbd = cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Leipzig", 2);

		System.out.println(cbd.size());
	}

	

}
