package org.dllearner.sparqlquerygenerator;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class ModelCreationTest {
	
	private static final int RECURSION_DEPTH = 2;
	private static final String RESOURCE = "http://dbpedia.org/resource/Leipzig";
	
	private static final Logger logger = Logger.getLogger(ModelCreationTest.class);
	
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
			logger.setLevel(Level.INFO);
			Logger.getLogger(ModelGenerator.class).setLevel(Level.DEBUG);
			Logger.getLogger(ModelCreationTest.class).setLevel(Level.DEBUG);
			
			
			URL url = new URL("http://dbpedia.aksw.org:8890/sparql");
			SparqlEndpoint endpoint = new SparqlEndpoint(url, Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
			ModelGenerator modelGen = new ModelGenerator(endpoint);
			
			logger.debug("Using chunk strategy.");
			Model model1 = modelGen.createModel(RESOURCE, ModelGenerator.Strategy.CHUNKS, RECURSION_DEPTH);
			logger.debug("Got overall " + model1.size() + " triple.");
			
			logger.debug("Using incremental strategy.");
			Model model2 = modelGen.createModel(RESOURCE, ModelGenerator.Strategy.INCREMENTALLY, RECURSION_DEPTH);
			logger.debug("Got overall " + model2.size() + " triple.");
			
			logger.debug("Using chunk with optional strategy.");
			Model model3 = modelGen.createModel(RESOURCE, null, RECURSION_DEPTH);
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

}
