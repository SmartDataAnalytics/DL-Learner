//package org.dllearner.common.index;
//
//import java.io.UnsupportedEncodingException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.sql.SQLException;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//import org.dllearner.kb.sparql.ExtractionDBCache;
//import org.dllearner.kb.sparql.SparqlEndpoint;
//
//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.rdf.model.Statement;
//import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
//import com.jamonapi.Monitor;
//import com.jamonapi.MonitorFactory;
//
//public class ModelGenerator {
//	
//	private static final Logger logger = Logger.getLogger(ModelGenerator.class);
//	private Monitor queryMonitor = MonitorFactory.getTimeMonitor("SPARQL Query monitor");
//	
//	private SparqlEndpoint endpoint;
//	private int recursionDepth = 1;
//	
//	private static final int CHUNK_SIZE = 1000;
//	
//	private ExtractionDBCache cache;
//	
//	private Set<String> predicateFilters;
//	
//	public enum Strategy{
//		INCREMENTALLY,
//		CHUNKS
//	}
//	
//	public ModelGenerator(SparqlEndpoint endpoint){
//		this(endpoint, Collections.<String>emptySet(), null);
//	}
//	
//	public ModelGenerator(SparqlEndpoint endpoint, Set<String> predicateFilters){
//		this(endpoint, predicateFilters, null);
//	}
//	
//	public ModelGenerator(SparqlEndpoint endpoint, Set<String> predicateFilters, ExtractionDBCache cache){
//		this.endpoint = endpoint;
//		this.predicateFilters = predicateFilters;
//		this.cache = cache;
//	}
//	
//	public ModelGenerator(SparqlEndpoint endpoint, ExtractionDBCache cache){
//		this(endpoint, Collections.<String>emptySet(), cache);
//	}
//	
//	public ModelGenerator(String endpointURL){
//		try {
//			this.endpoint = new SparqlEndpoint(new URL(endpointURL));
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public Model createModel(String resource, Strategy strategy, int recursionDepth){
//		this.recursionDepth = recursionDepth;
//		if(strategy == Strategy.INCREMENTALLY){
//			return getModelIncrementallyRec(resource, 0);
//		} else if(strategy == Strategy.CHUNKS){
//			return getModelChunked(resource);
//		}
//		return ModelFactory.createDefaultModel();
//	}
//	
//	public void setRecursionDepth(int recursionDepth){
//		this.recursionDepth = recursionDepth;
//	}
//	
//	
//	/**
//	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
//	 * @param example The example resource for which a CONSTRUCT query is created.
//	 * @return The JENA ARQ Query object.
//	 */
//	private String makeConstructQueryOptional(String resource, int limit, int offset, Set<String> predicateFilter){
//		StringBuilder sb = new StringBuilder();
//		sb.append("CONSTRUCT {\n");
//		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
//		for(int i = 1; i < recursionDepth; i++){
//			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
//		}
//		sb.append("}\n");
//		sb.append("WHERE {\n");
//		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
//		for(int i = 1; i < recursionDepth; i++){
//			sb.append("OPTIONAL{\n");
//			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
//		}
//		for(int i = 1; i < recursionDepth; i++){
//			sb.append("}");
//		}
//		
//		
//		for(int i = 0; i < recursionDepth; i++){
//			for(String predicate : predicateFilter){
//				sb.append("FILTER (!REGEX (?p").append(i).append(", \"").append(predicate).append("\"))");
//			}
//			
//		}
//	
//		sb.append("}\n");
////		sb.append("ORDER BY ");
////		for(int i = 0; i < recursionDepth; i++){
////			sb.append("?p").append(i).append(" ").append("?o").append(i).append(" ");
////		}
////		sb.append("\n");
//		sb.append("LIMIT ").append(limit).append("\n");
//		sb.append("OFFSET ").append(offset);
//		
//		Query query = QueryFactory.create(sb.toString());
//		
//		return sb.toString();
//	}
//	
//	
//	/**
//	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example.
//	 * @param example The example resource for which a CONSTRUCT query is created.
//	 * @return The JENA ARQ Query object.
//	 */
//	private String makeConstructQuery(String example, Set<String> predicateFilters){
//		
//		StringBuilder sb = new StringBuilder();
//		sb.append("CONSTRUCT {\n");
//		sb.append("<").append(example).append("> ").append("?p ").append("?o").append(".\n");
//		sb.append("}\n");
//		sb.append("WHERE {\n");
//		sb.append("<").append(example).append("> ").append("?p ").append("?o").append(".\n");
//		
//		for(String predicate : predicateFilters){
//			sb.append("FILTER (!REGEX (?p, \"").append(predicate).append("\"))");
//		}
//		
//		sb.append("}\n");
//		Query query = QueryFactory.create(sb.toString());
//		
//		return sb.toString();
//	}
//	
//	
//	
//	private Model getModelChunked(String resource){
////		logger.debug("Resource: " + resource);
//		String query = makeConstructQueryOptional(resource, CHUNK_SIZE, 0, predicateFilters);
////		logger.debug("Sending SPARQL query ...");
////		logger.debug("Query:\n" + query.toString());
//		queryMonitor.start();
//		Model all = ModelFactory.createDefaultModel();
//		try {
//			Model model;
//			if(cache == null){
//				model = getModel(query);
//			} else {
//				model = cache.executeConstructQuery(endpoint, query);
//			}
////			logger.debug("Got " + model.size() + " new triple in " + queryMonitor.getLastValue() + "ms.");
//			all.add(model);
//			queryMonitor.stop();
//			int i = 1;
//			while(model.size() != 0){
//				query = makeConstructQueryOptional(resource, CHUNK_SIZE, i * CHUNK_SIZE, predicateFilters);
////				logger.debug("Sending SPARQL query ...");
////				logger.debug("Query:\n" + query.toString());
//				queryMonitor.start();
//				if(cache == null){
//					model = getModel(query);
//				} else {
//					model = cache.executeConstructQuery(endpoint, query);
//				}
//				queryMonitor.stop();
////				logger.debug("Got " + model.size() + " new triple in " + queryMonitor.getLastValue() + "ms.");
//				all.add(model);
//				i++;
//			}
//		} catch (UnsupportedEncodingException e) {
//			logger.error(e);
//		} catch (SQLException e) {
//			logger.error(e);
//		}
//		return all;
//	}
//	
//	private Model getModelIncrementallyRec(String resource, int depth){
//		logger.debug("Resource: " + resource);
//		String query = makeConstructQuery(resource, predicateFilters);
//		logger.debug("Sending SPARQL query ...");
//		logger.debug("Query:\n" + query);
//		queryMonitor.start();
//		Model model = null;
//		try {
//			if(cache == null){
//				model = getModel(query);
//			} else {
//				model = cache.executeConstructQuery(endpoint, query);
//			}
//		} catch (UnsupportedEncodingException e) {
//			logger.error(e);
//		} catch (SQLException e) {
//			logger.error(e);
//		}
//		queryMonitor.stop();
//		logger.debug("Got " + model.size() + " new triples in " + queryMonitor.getLastValue() + "ms:");
//		Statement st = null;
//		for(Iterator<Statement> i = model.listStatements();i.hasNext(); st = i.next()){
//			logger.debug(st);
//		}
//		if(depth < recursionDepth){
//			Model tmp = ModelFactory.createDefaultModel();
//			for(Iterator<Statement> i = model.listStatements(); i.hasNext();){
//				st = i.next();
//				if(st.getObject().isURIResource()){
//					tmp.add(getModelIncrementallyRec(st.getObject().toString(), depth + 1));
//				}
//			}
//			model.add(tmp);
//		}
//		
//		return model;
//	}
//	
//	private Model getModel(String query){
//		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
//		for (String dgu : endpoint.getDefaultGraphURIs()) {
//			queryExecution.addDefaultGraph(dgu);
//		}
//		for (String ngu : endpoint.getNamedGraphURIs()) {
//			queryExecution.addNamedGraph(ngu);
//		}			
//		Model model = queryExecution.execConstruct();
//		return model;
//	}
//	
//
//}
