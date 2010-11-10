package org.dllearner.sparqlquerygenerator.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

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
	
	public enum Strategy{
		INCREMENTALLY,
		CHUNKS
	}
	
	public ModelGenerator(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
	}
	
	public ModelGenerator(SparqlEndpoint endpoint, ExtractionDBCache cache){
		this.endpoint = endpoint;
		this.cache = cache;
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
			return getModel(resource);
		}else if(strategy == null){
			return getModelOptional(resource);
		}
		return ModelFactory.createDefaultModel();
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param example The example resource for which a CONSTRUCT query is created.
	 * @return The JENA ARQ Query object.
	 */
	private Query makeConstructQuery(String resource, int limit, int offset){
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
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		
		sb.append("FILTER (!regex (?p0, \"http://dbpedia.org/property/wikilink\"))");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("FILTER (!regex (?p").append(i).append(", \"http://dbpedia.org/property/wikilink\"))");
		}
		sb.append("}\n");
		sb.append("ORDER BY ");
		for(int i = 0; i < recursionDepth; i++){
			sb.append("?p").append(i).append(" ").append("?o").append(i).append(" ");
		}
		sb.append("\n");
		sb.append("LIMIT ").append(limit).append("\n");
		sb.append("OFFSET ").append(offset);
		
		Query query = QueryFactory.create(sb.toString());
		
		return query;
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param example The example resource for which a CONSTRUCT query is created.
	 * @return The JENA ARQ Query object.
	 */
	private Query makeConstructQueryOptional(String resource, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}");
		sb.append("FILTER (!regex (?p0, \"http://dbpedia.org/property/wikilink\"))");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("FILTER (!regex (?p").append(i).append(", \"http://dbpedia.org/property/wikilink\"))");
		}
	
		sb.append("}\n");
		sb.append("ORDER BY ");
		for(int i = 0; i < recursionDepth; i++){
			sb.append("?p").append(i).append(" ").append("?o").append(i).append(" ");
		}
		sb.append("\n");
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
	private Query makeConstructQuery(String example){
		
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(example).append("> ").append("?p ").append("?o").append(".\n");
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(example).append("> ").append("?p ").append("?o").append(".\n");
		sb.append("FILTER (!regex (?p, \"http://dbpedia.org/property/wikilink\"))");
		sb.append("}\n");
		Query query = QueryFactory.create(sb.toString());
		
		return query;
	}
	
	
	private Model getModel(String resource){
		logger.debug("Resource: " + resource);
		Query query = makeConstructQuery(resource, CHUNK_SIZE, 0);
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
			query = makeConstructQuery(resource, CHUNK_SIZE, i * CHUNK_SIZE);
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
	
	private Model getModelOptional(String resource){
		logger.debug("Resource: " + resource);
		Query query = makeConstructQueryOptional(resource, CHUNK_SIZE, 0);
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
			query = makeConstructQueryOptional(resource, CHUNK_SIZE, i * CHUNK_SIZE);
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
	
	private Model getModelIncrementally(String resource){
		logger.debug("Resource: " + resource);
		Query query = makeConstructQuery(resource);
		logger.debug("Sending SPARQL query ...");
		logger.debug("Query:\n" + query.toString());
		queryMonitor.start();
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				endpoint.getURL().toString(),
				query,
				endpoint.getDefaultGraphURIs(),
				endpoint.getNamedGraphURIs());
		Model all = qexec.execConstruct();
		logger.debug("Got " + all.size() + " new triples:");
		Statement st = null;
		for(Iterator<Statement> i = all.listStatements();i.hasNext(); st = i.next()){
			logger.debug(st);
		}
		Model tmp = ModelFactory.createDefaultModel();
		Model model;
		for(Iterator<Statement> i = all.listStatements(); i.hasNext();){
			st = i.next();
			if(st.getObject().isURIResource()){
				logger.debug("Resource: " + st.getObject().toString());
				query = makeConstructQuery(st.getObject().toString());
				logger.debug("Sending SPARQL query ...");
				logger.debug("Query:\n" + query.toString());
				qexec = QueryExecutionFactory.sparqlService(
						endpoint.getURL().toString(),
						query,
						endpoint.getDefaultGraphURIs(),
						endpoint.getNamedGraphURIs());
				model = qexec.execConstruct();
				logger.debug("Got " + model.size() + " new triple");
				Statement s = null;
				for(Iterator<Statement> it = model.listStatements();it.hasNext(); s = it.next()){
					logger.debug(st);
				}
				tmp.add(model);
			}
		}
		all.add(tmp);
		return all;
	}
	
	private Model getModelIncrementallyRec(String resource, int depth){
		logger.debug("Resource: " + resource);
		Query query = makeConstructQuery(resource);
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
					tmp.add(getModelIncrementallyRec(st.getObject().toString(), depth++));
				}
			}
			model.add(tmp);
		}
		
		return model;
	}

}
