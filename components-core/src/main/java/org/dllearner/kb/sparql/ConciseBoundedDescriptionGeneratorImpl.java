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

import com.google.common.base.Joiner;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
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
	
	private static final int MAX_RECURSION_DEPTH_DEFAULT = 1;
	
	private int maxRecursionDepth = 1;
	
	private Model baseModel;
	private QueryExecutionFactory qef;
	
	private Set<String> propertyBlacklist = new TreeSet<>();
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, CacheFrontend cache) {
		qef = FluentQueryExecutionFactory
				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs())
				.config().withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee())
						.setModelContentType(WebContent.contentTypeRDFXML))
				.end()
				.create();

		if(cache != null){
			qef = new QueryExecutionFactoryCacheEx(qef, cache);
		}
		qef = new QueryExecutionFactoryPaginated(qef, 10000);
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, String cacheDir, int maxRecursionDepth) {
		this.maxRecursionDepth = maxRecursionDepth;

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
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, String cacheDir) {
		this(endpoint, cacheDir, MAX_RECURSION_DEPTH_DEFAULT);
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint) {
		this(endpoint, (String)null);
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(Model model) {
		this.baseModel = model;
		
		qef = new QueryExecutionFactoryModel(baseModel);
	}
	
	@Override
	public Model getConciseBoundedDescription(String resourceURI){
		return getConciseBoundedDescription(resourceURI, maxRecursionDepth);
	}
	
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth){
		return getConciseBoundedDescription(resourceURI, depth, false);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs) {
		logger.trace("Computing CBD for {} ...", resourceURI);
		long start = System.currentTimeMillis();
		String query = generateQuery(resourceURI, depth, withTypesForLeafs);
		QueryExecution qe = qef.createQueryExecution(query);
		Model model = qe.execConstruct();
		qe.close(); 
		long end = System.currentTimeMillis();
		logger.trace("Got {} triples in {} ms.", model.size(), (end - start));
		return model;
	}
	
	@Override
	public void addAllowedPropertyNamespaces(Set<String> namespaces) {
		this.allowedPropertyNamespaces.addAll(namespaces);
	}
	
	@Override
	public void addAllowedObjectNamespaces(Set<String> namespaces) {
		this.allowedObjectNamespaces.addAll(namespaces);
	}
	
	@Override
	public void setRecursionDepth(int maxRecursionDepth) {
		this.maxRecursionDepth = maxRecursionDepth;
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param resource The example resource for which a CONSTRUCT query is created.
	 * @return the SPARQL query
	 */
	private String generateQuery(String resource, int depth, boolean withTypesForLeafs){
		int lastIndex = Math.max(0, depth - 1);
		
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append(String.format("<%s> ?p0 ?o0 .\n", resource));
//		sb.append("?p0 a ?type0.\n");
		for(int i = 1; i < depth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		if(withTypesForLeafs){
			sb.append("?o").append(lastIndex).append(" a ?type.\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		sb.append(createPropertyNamespacesFilter("?p0"));
		sb.append(createPropertyFilter(Var.alloc("p0")));
		sb.append(createObjectNamespacesFilter("?o0"));
//		sb.append("?p0 a ?type0.\n");
		for(int i = 1; i < depth; i++){
			sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
			sb.append(createPropertyNamespacesFilter("?p" + i));
			sb.append(createObjectNamespacesFilter("?o" + i));
			sb.append(createPropertyFilter(Var.alloc("p" + i)));
		}
		if(withTypesForLeafs){
			sb.append("OPTIONAL{?o").append(lastIndex).append(" a ?type.}\n");
		}
		for(int i = 1; i < depth; i++){
			sb.append("}");
		}
		sb.append("}\n");
		return sb.toString();
	}
	
	private String createPropertyFilter(final Var var) {
		String filter = "";
		
		if(!propertyBlacklist.isEmpty()) {
			filter += "FILTER(";
					
			filter += Joiner.on(" && ").join(
					propertyBlacklist.stream()
							.map(input -> var.toString() + " != <" + input + ">")
							.collect(Collectors.toList())
					);
			filter += ")\n";
		}
		
		return filter;
	}
	
	private String createPropertyNamespacesFilter(String targetVar){
		String filter = "";
		if(allowedPropertyNamespaces != null && !allowedPropertyNamespaces.isEmpty()){
			filter += "FILTER(" + targetVar + " = rdf:type || ";
			for(Iterator<String> iter = allowedPropertyNamespaces.iterator(); iter.hasNext();){
				String ns = iter.next();
				filter += "(STRSTARTS(STR(" + targetVar + "),'" + ns + "'))";
				if(iter.hasNext()){
					filter += " || ";
				}
			}
			filter += ")\n";
		}
		return filter;
	}
	
	private String createObjectNamespacesFilter(String targetVar){
		String filter = "";
		if(allowedObjectNamespaces != null && !allowedObjectNamespaces.isEmpty()){
			filter += "FILTER(ISLITERAL(" + targetVar + ") || ";
			for(Iterator<String> iter = allowedObjectNamespaces.iterator(); iter.hasNext();){
				String ns = iter.next();
				filter += "STRSTARTS(STR(" + targetVar + "),'" + ns + "')";
				if(iter.hasNext()){
					filter += " || ";
				}
			}
			filter += ")\n";
		}
		return filter;
	}
	
	@Override
	public void addPropertiesToIgnore(Set<String> properties) {
		propertyBlacklist.addAll(properties);
	}
	
	public static void main(String[] args) {
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		cbdGen = new CachingConciseBoundedDescriptionGenerator(cbdGen);
//		cbdGen.setRestrictToNamespaces(Arrays.asList(new String[]{"http://dbpedia.org/ontology/", RDF.getURI(), RDFS.getURI()}));
		Model cbd = cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Leipzig", 3);
		System.out.println(cbd.size());
	}

	

}
