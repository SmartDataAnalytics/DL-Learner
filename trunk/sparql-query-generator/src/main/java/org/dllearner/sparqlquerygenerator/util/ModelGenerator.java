package org.dllearner.sparqlquerygenerator.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ModelGenerator {
	
	private static final Logger logger = Logger.getLogger(ModelGenerator.class);
	private Monitor queryMonitor = MonitorFactory.getTimeMonitor("SPARQL Query monitor");
	
	private SparqlEndpoint endpoint;
	private int recursionDepth = 1;
	
	private static final int CHUNK_SIZE = 1000;
	
	private ExtractionDBCache cache;
	
	private Set<String> predicateFilters;
	
	public enum Strategy{
		INCREMENTALLY,
		CHUNKS
	}
	
	public ModelGenerator(SparqlEndpoint endpoint){
		this(endpoint, Collections.<String>emptySet());
	}
	
	public ModelGenerator(SparqlEndpoint endpoint, Set<String> predicateFilters){
		this.endpoint = endpoint;
		this.predicateFilters = predicateFilters;
	}
	
	public ModelGenerator(SparqlEndpoint endpoint, Set<String> predicateFilters, ExtractionDBCache cache){
		this.endpoint = endpoint;
		this.predicateFilters = predicateFilters;
		this.cache = cache;
	}
	
	public ModelGenerator(SparqlEndpoint endpoint, ExtractionDBCache cache){
		this(endpoint, Collections.<String>emptySet(), cache);
	}
	
	public ModelGenerator(String endpointURL){
		try {
			this.endpoint = new SparqlEndpoint(new URL(endpointURL));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public Model createModel(String resource, Strategy strategy, int recursionDepth){
		this.recursionDepth = recursionDepth;
		if(strategy == Strategy.INCREMENTALLY){
			return getModelIncrementallyRec(resource, 0);
		} else if(strategy == Strategy.CHUNKS){
			return getModelChunked(resource);
		}
		return ModelFactory.createDefaultModel();
	}
	
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param example The example resource for which a CONSTRUCT query is created.
	 * @return The JENA ARQ Query object.
	 */
	private Query makeConstructQueryOptional(String resource, int limit, int offset, Set<String> predicateFilter){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		for(int i = 1; i < recursionDepth; i++){
			sb.append("}");
		}
		
		
		for(int i = 0; i < recursionDepth; i++){
			for(String predicate : predicateFilter){
				sb.append("FILTER (!REGEX (?p").append(i).append(", \"").append(predicate).append("\"))");
			}
			
		}
	
		sb.append("}\n");
//		sb.append("ORDER BY ");
//		for(int i = 0; i < recursionDepth; i++){
//			sb.append("?p").append(i).append(" ").append("?o").append(i).append(" ");
//		}
//		sb.append("\n");
		sb.append("LIMIT ").append(limit).append("\n");
		sb.append("OFFSET ").append(offset);
		
		Query query = QueryFactory.create(sb.toString());
		
		return query;
	}
	
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example.
	 * @param example The example resource for which a CONSTRUCT query is created.
	 * @return The JENA ARQ Query object.
	 */
	private Query makeConstructQuery(String example, Set<String> predicateFilters){
		
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(example).append("> ").append("?p ").append("?o").append(".\n");
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(example).append("> ").append("?p ").append("?o").append(".\n");
		
		for(String predicate : predicateFilters){
			sb.append("FILTER (!REGEX (?p, \"").append(predicate).append("\"))");
		}
		
		sb.append("}\n");
		Query query = QueryFactory.create(sb.toString());
		
		return query;
	}
	
	
	
	private Model getModelChunked(String resource){
		logger.debug("Resource: " + resource);
		Query query = makeConstructQueryOptional(resource, CHUNK_SIZE, 0, predicateFilters);
		logger.debug("Sending SPARQL query ...");
		logger.debug("Query:\n" + query.toString());
		queryMonitor.start();
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				endpoint.getURL().toString(),
				query,
				endpoint.getDefaultGraphURIs(),
				endpoint.getNamedGraphURIs());
		Model all = ModelFactory.createDefaultModel();
		Model model = qexec.execConstruct();
		logger.debug("Got " + model.size() + " new triple");
		all.add(model);
		queryMonitor.stop();
		qexec.close();
		int i = 1;
		while(model.size() != 0){
			query = makeConstructQueryOptional(resource, CHUNK_SIZE, i * CHUNK_SIZE, predicateFilters);
			logger.debug("Sending SPARQL query ...");
			logger.debug("Query:\n" + query.toString());
			queryMonitor.start();
			qexec = QueryExecutionFactory.sparqlService(
					endpoint.getURL().toString(),
					query,
					endpoint.getDefaultGraphURIs(),
					endpoint.getNamedGraphURIs());
			model = qexec.execConstruct();
			logger.debug("Got " + model.size() + " new triple");
			all.add(model);
			queryMonitor.stop();
			qexec.close();
			i++;
		}
		return all;
	}
	
	private Model getModelIncrementallyRec(String resource, int depth){
		logger.debug("Resource: " + resource);
		Query query = makeConstructQuery(resource, predicateFilters);
		logger.debug("Sending SPARQL query ...");
		logger.debug("Query:\n" + query.toString());
		queryMonitor.start();
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				endpoint.getURL().toString(),
				query,
				endpoint.getDefaultGraphURIs(),
				endpoint.getNamedGraphURIs());
		Model model = qexec.execConstruct();
		logger.debug("Got " + model.size() + " new triples:");
		Statement st = null;
		for(Iterator<Statement> i = model.listStatements();i.hasNext(); st = i.next()){
			logger.debug(st);
		}
		if(depth < recursionDepth){
			Model tmp = ModelFactory.createDefaultModel();
			for(Iterator<Statement> i = model.listStatements(); i.hasNext();){
				st = i.next();
				if(st.getObject().isURIResource()){
					tmp.add(getModelIncrementallyRec(st.getObject().toString(), depth + 1));
				}
			}
			model.add(tmp);
		}
		
		return model;
	}
	

}
