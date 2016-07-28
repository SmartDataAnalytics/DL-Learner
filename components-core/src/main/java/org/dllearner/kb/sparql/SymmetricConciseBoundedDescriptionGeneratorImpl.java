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

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class SymmetricConciseBoundedDescriptionGeneratorImpl implements ConciseBoundedDescriptionGenerator{
	
	private static final Logger logger = LoggerFactory.getLogger(SymmetricConciseBoundedDescriptionGeneratorImpl.class);
	
	private Model baseModel;
	private QueryExecutionFactory qef;
	
	private Set<String> namespaces;
	private int maxRecursionDepth = 1;
	
	public SymmetricConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, CacheFrontend cache) {
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
	
	public SymmetricConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint) {
		this(endpoint, null);
	}

	public SymmetricConciseBoundedDescriptionGeneratorImpl(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	public SymmetricConciseBoundedDescriptionGeneratorImpl(Model model) {
		this.baseModel = model;

		qef = new QueryExecutionFactoryModel(baseModel);
	}
	
	@Override
	public Model getConciseBoundedDescription(String resourceURI){
		return getConciseBoundedDescription(resourceURI, maxRecursionDepth);
	}
	
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth){
		logger.debug("computing CBD of depth {} for {} ...", resourceURI, depth);
		Model cbd = ModelFactory.createDefaultModel();
		cbd.add(getIncomingModel(resourceURI, depth));
		cbd.add(getOutgoingModel(resourceURI, depth));
		logger.debug("CBD size: {}", cbd.size());
		return cbd;
	}
	
	@Override
	public void addAllowedPropertyNamespaces(Set<String> namespaces) {
		this.namespaces = namespaces;
	}
	
	private Model getIncomingModel(String resource, int depth){
		String query = makeConstructQueryObject(resource, depth);
		logger.debug("computing incoming triples for {}\n{}", resource, query);
		try(QueryExecution qe = qef.createQueryExecution(query)) {
			Model model = qe.execConstruct();
			return model;
		} catch (Exception e) {
			logger.error("Failed to retrieve incoming CBD for " + resource + ".\nQuery:\n" + query, e);
		}
		return null;
	}
	
	private Model getOutgoingModel(String resource, int depth){
		String query = makeConstructQuerySubject(resource, depth);
		logger.debug("computing outgoing triples for {}\n{}", resource, query);
		try(QueryExecution qe = qef.createQueryExecution(query)) {
			Model model = qe.execConstruct();
			return model;
		} catch (Exception e) {
			logger.error("Failed to retrieve outgoing CBD for " + resource + ".\nQuery:\n" + query, e);
		}
		return null;
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given resource and recursion depth.
	 * @param resource The resource for which a CONSTRUCT query is created.
	 * @return The CONSTRUCT query
	 */
	private String makeConstructQuerySubject(String resource, int depth){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < depth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < depth; i++){
			sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		for(int i = 1; i < depth; i++){
			sb.append("}");
		}
		sb.append("}\n");

		return sb.toString();
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given resource and recursion depth.
	 * @param resource The resource for which a CONSTRUCT query is created.
	 * @return The CONSTRUCT query
	 */
	private String makeConstructQueryObject(String resource, int depth){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("?s0 ").append("?p0 ").append("<").append(resource).append(">").append(".\n");
		for(int i = 1; i < depth; i++){
			sb.append("?o").append(i).append(" ").append("?p").append(i).append(" ").append("?s").append(i-1).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("?s0 ").append("?p0 ").append("<").append(resource).append(">").append(".\n");
		for(int i = 1; i < depth; i++){
			sb.append("OPTIONAL{\n");
			sb.append("?o").append(i).append(" ").append("?p").append(i).append(" ").append("?s").append(i-1).append(".\n");
		}
		for(int i = 1; i < depth; i++){
			sb.append("}");
		}
		sb.append("}\n");

		return sb.toString();
	}

	@Override
	public void setRecursionDepth(int maxRecursionDepth) {
		this.maxRecursionDepth = maxRecursionDepth;
	}
	
	@Override
	public void addPropertiesToIgnore(Set<String> properties) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#addAllowedObjectNamespaces(java.util.Set)
	 */
	@Override
	public void addAllowedObjectNamespaces(Set<String> namespaces) {
	}

	public static void main(String[] args) {
		ConciseBoundedDescriptionGenerator cbdGen = new SymmetricConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint.getEndpointDBpedia());

		Resource res = ResourceFactory.createResource("http://dbpedia.org/resource/Santa_Clara,_California");

		Model cbd = cbdGen.getConciseBoundedDescription(res.getURI(), 1);
		System.out.println("#triples =\t" + cbd.size());

		System.out.println("#triples_out =\t" + cbd.listStatements(res, null, (RDFNode) null).toSet().size());
		cbd.listStatements(res, null, (RDFNode) null).toList().forEach(System.out::println);

		System.out.println("#triples_in =\t" + cbd.listStatements(null, null, res).toSet().size());
		cbd.listStatements(null, null, res).toList().forEach(System.out::println);

	}

}
