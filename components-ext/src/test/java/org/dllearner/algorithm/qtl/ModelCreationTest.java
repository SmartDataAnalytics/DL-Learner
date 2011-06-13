package org.dllearner.algorithm.qtl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.qtl.datastructures.QueryTree;
import org.dllearner.algorithm.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.JamonMonitorLogger;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ModelCreationTest {
	
	private static final int RECURSION_DEPTH = 2;
	private static final String RESOURCE = "http://dbpedia.org/resource/Dresden";
	
	private static final Logger logger = Logger.getLogger(ModelCreationTest.class);
	
	private static final SparqlEndpoint ENDPOINT = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
	
	@Test
	public void test1(){
		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			FileAppender fileAppender = new FileAppender(layout,
					"log/model_test.log", false);
			logger.removeAllAppenders();
			logger.addAppender(consoleAppender);
			logger.addAppender(fileAppender);
			logger.setLevel(Level.DEBUG);
			Logger.getLogger(ModelGenerator.class).setLevel(Level.DEBUG);
			Logger.getLogger(ModelCreationTest.class).setLevel(Level.DEBUG);
			
			
			URL url = new URL("http://lod.openlinksw.com/sparql/");
			SparqlEndpoint endpoint = new SparqlEndpoint(url, Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
			Set<String> predicateFilters = new HashSet<String>();
			predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
			predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
			
			ModelGenerator modelGen = new ModelGenerator(endpoint, predicateFilters, new ExtractionDBCache("construct-cache"));
			
//			logger.debug("Using chunk strategy.");
//			Model model1 = modelGen.createModel(RESOURCE, ModelGenerator.Strategy.CHUNKS, 2);
//			logger.debug("Got overall " + model1.size() + " triple.");
//			
//			logger.debug("Using incremental strategy.");
//			Model model2 = modelGen.createModel(RESOURCE, ModelGenerator.Strategy.INCREMENTALLY, RECURSION_DEPTH);
//			logger.debug("Got overall " + model2.size() + " triple.");
			
			logger.debug("Using chunk with optional strategy.");
			Model model3 = modelGen.createModel(RESOURCE, ModelGenerator.Strategy.CHUNKS, 2);System.out.println(model3.size());
			logger.debug("Got overall " + model3.size() + " triple.");
			QueryTreeFactory<String> f = new QueryTreeFactoryImpl();
			QueryTree<String> t = f.getQueryTree(RESOURCE, model3);
			System.out.println(t.getStringRepresentation());
			
//			Model diff = ModelFactory.createDefaultModel();
//			if(model1.size() > model2.size()){
//				logger.debug("Chunk strategy returned " + (model1.size() - model2.size()) + " more triple.");
//				diff.add(model1.difference(model2));
//			} else if(model2.size() > model1.size()){
//				logger.debug("Incremental strategy returned " + (model2.size() - model1.size()) + " more triple.");
//				diff.add(model2.difference(model1));
//			} else {
//				logger.debug("Both strategies returned the same number of triple.");
//			}
//			
//			logger.debug("Difference : ");
//			Statement st = null;
//			for(Iterator<Statement> i = diff.listStatements();i.hasNext(); st = i.next()){
//				logger.debug(st);
//			}
//			
//			diff = model3.difference(model1);
//			st = null;
//			System.out.println("Difference between other");
//			for(Iterator<Statement> i = diff.listStatements();i.hasNext(); st = i.next()){
//				System.out.println(st);
//			}
//			assertTrue(model1.size() == model2.size());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void multiThreadedModelCreationTest(){
		String resource = "http://dbpedia.org/resource/Munich";
		
		Model model = ModelFactory.createDefaultModel();
		
		int proCnt = Runtime.getRuntime().availableProcessors();
		logger.info("Number of processor: " + proCnt);
		Future<Model>[] ret = new Future[proCnt];
		List<String> queries =  createSearchQueries("Hamburg", "Vienna", "Stuttgart", "Frankfurt", "Kiel");;//createQueries(resource, proCnt);
		
		ExecutorService es = Executors.newFixedThreadPool(proCnt);
		for(int i = 0; i < 5; i++){
			ret[i] = es.submit(new ModelRetrievalTask(queries.get(i)));
		}
		
		for (int i = 0; i < proCnt; i++) {
            try {
                model.add(ret[i].get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
            	e.printStackTrace();
            }
        }
		
		es.shutdown();
		System.out.println(model.size());
		
		Model singleThreadedModel = ModelFactory.createDefaultModel();
		Monitor mon = MonitorFactory.getTimeMonitor("singleThreaded").start();
		queries = createSearchQueries("Leipzig", "Berlin", "Dresden", "Munich", "Dortmund");
		for(String query : queries){
			singleThreadedModel.add(getModel(query));
		}
		mon.stop();
		System.out.println("Single threaded: " + mon.getTotal());
		
	}
	
	private class ModelRetrievalTask implements Callable<Model>{
		
		private String query;
		
		public ModelRetrievalTask(String query){
			this.query = query;
		}

		@Override
		public Model call() throws Exception {
			System.out.println(query);
			Monitor mon = MonitorFactory.getTimeMonitor("query").start();
			JamonMonitorLogger.getTimeMonitor(ModelCreationTest.class, "time").start();
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(ENDPOINT.getURL().toString(), query);
			for (String dgu : ENDPOINT.getDefaultGraphURIs()) {
				queryExecution.addDefaultGraph(dgu);
			}
			for (String ngu : ENDPOINT.getNamedGraphURIs()) {
				queryExecution.addNamedGraph(ngu);
			}			
			Model model = queryExecution.execConstruct();
			mon.stop();
			System.out.println(mon.getLastValue());
			return model;
		}
		
	}
	
	private Model getModel(String query){
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(ENDPOINT.getURL().toString(), query);
		for (String dgu : ENDPOINT.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : ENDPOINT.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		Model model = queryExecution.execConstruct();
		
		return model;
	}
	
	private List<String> createQueries(String resource, int cnt){
		List<String> queries = new ArrayList<String>(cnt);
		for(int i = 0; i < cnt; i++){
			queries.add(createConstructQuery(resource, 50, i * 50));
		}
		
		return queries;
	}
	
	private String createConstructQuery(String resource, int limit, int offset){
		
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(resource).append("> ").append("?p ").append("?o").append(".\n");
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p ").append("?o").append(".\n");
		
		sb.append("}\n");
		sb.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
		
		return sb.toString();
	}
	
	private List<String> createSearchQueries(String ... searchTerms){
		List<String> queries = new ArrayList<String>();
		for(String term : searchTerms){
			queries.add(createSearchQuery(term));
		}
		return queries;
	}
	
	private String createSearchQuery(String searchTerm){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT");
		sb.append("{?s ?p ?o.}");
		sb.append("WHERE");
		sb.append("{?s ?p ?o.");
		sb.append("?s rdfs:label ?label.");
		sb.append("?label bif:contains '").append(searchTerm).append("'.} LIMIT 1000");
		
//		sb.append("SELECT ?s ?label WHERE {?s rdfs:label ?label. ?label bif:contains '").append(searchTerm).append("'.} limit 500");
		return sb.toString();
	}
	
//	
//	class ModelProducer implements Runnable{
//		
//		protected BlockingQueue<Model> queue;
//		private int offset;
//
//		public ModelProducer(BlockingQueue<Model> queue, int offset) {
//			this.queue = queue;
//			this.offset = offset;
//		}
//
//		@Override
//		public void run() {
//			Model model = getModel(offset);System.out.println(offset + " -> " + model.size());
//			try {
//				queue.put(model);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		private Model getModel(int offset){
//			Query query = QueryFactory.create(
//					"CONSTRUCT " +
//					"{<http://dbpedia.org/resource/Dresden> ?p1 ?o1. ?o1 ?p2 ?o2.}" +
//					" WHERE " +
//					"{<http://dbpedia.org/resource/Dresden> ?p1 ?o1. ?o1 ?p2 ?o2.}" +
//					"LIMIT 100 OFFSET " + offset);
//			
//			QueryExecution qexec = QueryExecutionFactory.sparqlService(
//					ENDPOINT.getURL().toString(),
//					query,
//					ENDPOINT.getDefaultGraphURIs(),
//					ENDPOINT.getNamedGraphURIs());
//			return qexec.execConstruct();
//		}
//		
//	}
//	
//	class ModelConsumer implements Runnable{
//		
//		protected BlockingQueue<Model> queue;
//		private Model completeModel;
//
//		public ModelConsumer(BlockingQueue<Model> queue) {
//			this.queue = queue; 
//			completeModel = ModelFactory.createDefaultModel();
//		}
//
//		@Override
//		public void run() {
//			while(true){
//				try {
//					Model m = queue.take();
//					completeModel.add(m);
//					System.out.println(completeModel.size());
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//	}

}
