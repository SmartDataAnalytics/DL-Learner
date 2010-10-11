package org.dllearner.sparqlquerygenerator;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class ModelCreationTest {
	
	private static final int RECURSION_DEPTH = 2;
	private static final String RESOURCE = "http://dbpedia.org/resource/Leipzig";
	
	@Test
	public void test1(){
		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			FileAppender fileAppender = new FileAppender( layout, "log/model_test.log", false );
			Logger logger = Logger.getRootLogger();
			logger.removeAllAppenders();
			logger.addAppender(consoleAppender);
			logger.addAppender(fileAppender);
			logger.setLevel(Level.INFO);		
			Logger.getLogger(ModelGenerator.class).setLevel(Level.DEBUG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ModelGenerator modelGen = new ModelGenerator(SparqlEndpoint.getEndpointDBpedia());
		
		Model model1 = modelGen.createModel(RESOURCE, ModelGenerator.Strategy.CHUNKS, RECURSION_DEPTH);
		System.out.println(model1.size());
		
		Model model2 = modelGen.createModel(RESOURCE, ModelGenerator.Strategy.INCREMENTALLY, RECURSION_DEPTH);
		System.out.println(model2.size());
		
		Model diff = ModelFactory.createDefaultModel();
		if(model1.size() > model2.size()){
			diff.add(model1.difference(model2));
		} else if(model2.size() > model1.size()){
			diff.add(model2.difference(model1));
		}
		
		Statement st = null;
		for(Iterator<Statement> i = diff.listStatements();i.hasNext(); st = i.next()){
			System.out.println(st);
		}
		assertTrue(model1.size() == model2.size());
	}

}
