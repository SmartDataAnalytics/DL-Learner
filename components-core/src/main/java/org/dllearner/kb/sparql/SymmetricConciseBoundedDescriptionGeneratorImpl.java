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

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class SymmetricConciseBoundedDescriptionGeneratorImpl implements ConciseBoundedDescriptionGenerator{
	
	private static final Logger logger = Logger.getLogger(SymmetricConciseBoundedDescriptionGeneratorImpl.class);
	
	private static final int CHUNK_SIZE = 1000;
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	
	private Set<String> namespaces;
	private int maxRecursionDepth = 1;
	
	public SymmetricConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
	public SymmetricConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint) {
		this(endpoint, null);
	}
	
	@Override
	public Model getConciseBoundedDescription(String resourceURI){
		return getConciseBoundedDescription(resourceURI, maxRecursionDepth);
	}
	
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth){
		Model cbd = ModelFactory.createDefaultModel();
		cbd.add(getModelChunkedResourceIsObject(resourceURI, depth));
		cbd.add(getModelChunkedResourceIsSubject(resourceURI, depth));
		return cbd;
	}
	
	@Override
	public void addAllowedPropertyNamespaces(Set<String> namespaces) {
		this.namespaces = namespaces;
	}
	
	private Model getModelChunkedResourceIsObject(String resource, int depth){
		String query = makeConstructQueryObject(resource, CHUNK_SIZE, 0, depth);
		Model all = ModelFactory.createDefaultModel();
		try {
			Model model;
			if(cache == null){
				model = getModel(query);
			} else {
				model = cache.executeConstructQuery(endpoint, query);
			}
			all.add(model);
			int i = 1;
			while(model.size() != 0){
//			while(model.size() == CHUNK_SIZE){
				query = makeConstructQueryObject(resource, CHUNK_SIZE, i * CHUNK_SIZE, depth);
				if(cache == null){
					model = getModel(query);
				} else {
					model = cache.executeConstructQuery(endpoint, query);
				}
				all.add(model);
				i++;
			}
		} catch (UnsupportedEncodingException | SQLException e) {
			logger.error(e);
		}
		return all;
	}
	
	private Model getModelChunkedResourceIsSubject(String resource, int depth){
		String query = makeConstructQuerySubject(resource, CHUNK_SIZE, 0, depth);
		Model all = ModelFactory.createDefaultModel();
		try {
			Model model;
			if(cache == null){
				model = getModel(query);
			} else {
				model = cache.executeConstructQuery(endpoint, query);
			}
			all.add(model);
			int i = 1;
			while(model.size() != 0){
//			while(model.size() == CHUNK_SIZE){
				query = makeConstructQuerySubject(resource, CHUNK_SIZE, i * CHUNK_SIZE, depth);
				if(cache == null){
					model = getModel(query);
				} else {
					model = cache.executeConstructQuery(endpoint, query);
				}
				all.add(model);
				i++;
			}
		} catch (UnsupportedEncodingException | SQLException e) {
			logger.error(e);
		}
		return all;
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given resource and recursion depth.
	 * @param resource The resource for which a CONSTRUCT query is created.
	 * @return The CONSTRUCT query
	 */
	private String makeConstructQuerySubject(String resource, int limit, int offset, int depth){
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
		sb.append("LIMIT ").append(limit).append("\n");
		sb.append("OFFSET ").append(offset);
		
		return sb.toString();
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given resource and recursion depth.
	 * @param resource The resource for which a CONSTRUCT query is created.
	 * @return The CONSTRUCT query
	 */
	private String makeConstructQueryObject(String resource, int limit, int offset, int depth){
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
		sb.append("LIMIT ").append(limit).append("\n");
		sb.append("OFFSET ").append(offset);
		
		return sb.toString();
	}
	
	private Model getModel(String query) throws UnsupportedEncodingException, SQLException{
		if(logger.isDebugEnabled()){
			logger.debug("Sending SPARQL query ...");
			logger.debug("Query:\n" + query);
		}

		Model model;
		if(cache == null){
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			for (String dgu : endpoint.getDefaultGraphURIs()) {
				queryExecution.addDefaultGraph(dgu);
			}
			for (String ngu : endpoint.getNamedGraphURIs()) {
				queryExecution.addNamedGraph(ngu);
			}			
			model = queryExecution.execConstruct();
		} else {
			model = cache.executeConstructQuery(endpoint, query);
		}
		if(logger.isDebugEnabled()){
			logger.debug("Got " + model.size() + " new triples in.");
		}
		return model;
	}
	
	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		ConciseBoundedDescriptionGenerator cbdGen = new SymmetricConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint.getEndpointDBpedia());
		cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Leipzig", 1);
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

}
