package org.dllearner.sparqlquerygenerator;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class ModelCreationTest {
	
	private static final int RECURSION_DEPTH = 2;
	private static final String RESOURCE = "http://dbpedia.org/resource/Leipzig";
	
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
			
			ModelGenerator modelGen = new ModelGenerator(endpoint, predicateFilters, new ExtractionDBCache("testCache"));
			
			logger.debug("Using chunk strategy.");
			Model model1 = modelGen.createModel(RESOURCE, ModelGenerator.Strategy.CHUNKS, 3);
			logger.debug("Got overall " + model1.size() + " triple.");
			
			logger.debug("Using incremental strategy.");
			Model model2 = modelGen.createModel(RESOURCE, ModelGenerator.Strategy.INCREMENTALLY, RECURSION_DEPTH);
			logger.debug("Got overall " + model2.size() + " triple.");
			
			logger.debug("Using chunk with optional strategy.");
			Model model3 = modelGen.createModel(RESOURCE, null, 3);
			logger.debug("Got overall " + model3.size() + " triple.");
			
			Model diff = ModelFactory.createDefaultModel();
			if(model1.size() > model2.size()){
				logger.debug("Chunk strategy returned " + (model1.size() - model2.size()) + " more triple.");
				diff.add(model1.difference(model2));
			} else if(model2.size() > model1.size()){
				logger.debug("Incremental strategy returned " + (model2.size() - model1.size()) + " more triple.");
				diff.add(model2.difference(model1));
			} else {
				logger.debug("Both strategies returned the same number of triple.");
			}
			
			logger.debug("Difference : ");
			Statement st = null;
			for(Iterator<Statement> i = diff.listStatements();i.hasNext(); st = i.next()){
				logger.debug(st);
			}
			
			diff = model3.difference(model1);
			st = null;
			System.out.println("Difference between other");
			for(Iterator<Statement> i = diff.listStatements();i.hasNext(); st = i.next()){
				System.out.println(st);
			}
//			assertTrue(model1.size() == model2.size());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	@Test
//	public void multiThreadedModelCreationTest(){
//		BlockingQueue<Model> queue = new LinkedBlockingQueue<Model>();
//		
//		ModelConsumer consumer = new ModelConsumer(queue);
//		
//		int offset = 100;
//		for(int i = 0; i <= 5; i++){
//			new Thread(new ModelProducer(queue, i * offset)).start();
//			
//		}
//		new Thread(consumer).start();
//		
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
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
