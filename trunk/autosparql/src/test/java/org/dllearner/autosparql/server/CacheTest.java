package org.dllearner.autosparql.server;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;
import org.dllearner.autosparql.server.cache.DBModelCacheExtended;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.Files;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class CacheTest {
	
//	@Test
//	public void test1(){
//		SimpleLayout layout = new SimpleLayout();
//		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
//		Logger logger = Logger.getRootLogger();
//		logger.removeAllAppenders();
//		logger.addAppender(consoleAppender);
//		logger.setLevel(Level.ERROR);		
//		Logger.getLogger(DBModelCacheComplete.class).setLevel(Level.ERROR);
//		
//		DBModelCache cache = new DBModelCacheComplete("cache", SparqlEndpoint.getEndpointDBpediaAKSW(), 2);
//	}
//	
//	@Test
//	public void test2(){
//		SimpleLayout layout = new SimpleLayout();
//		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
//		Logger logger = Logger.getRootLogger();
//		logger.removeAllAppenders();
//		logger.addAppender(consoleAppender);
//		logger.setLevel(Level.ERROR);		
//		Logger.getLogger(DBModelCacheSingle.class).setLevel(Level.ERROR);
//		
//		DBModelCache cache = new DBModelCacheSingle("cache", SparqlEndpoint.getEndpointDBpediaAKSW());
//	}
	
//	@Test
	public void fillCacheBatchedTest(){
		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			FileAppender fileAppender = new FileAppender( layout, "log/fillCacheBatched.log", false );
			Logger logger = Logger.getRootLogger();
			logger.removeAllAppenders();
			logger.addAppender(consoleAppender);
			logger.addAppender(fileAppender);
			logger.setLevel(Level.INFO);		
			Logger.getLogger(DBModelCacheExtended.class).setLevel(Level.INFO);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DBModelCacheExtended cache = new DBModelCacheExtended("cacheBatched", SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		cache.deleteCache();
		cache.createCache();
		cache.fillCacheBatched(1000);
	}
	
	@Test
	public void fillCacheTest(){
		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			FileAppender fileAppender = new FileAppender( layout, "log/fillCache.log", false );
			Logger logger = Logger.getRootLogger();
			logger.removeAllAppenders();
			logger.addAppender(consoleAppender);
			logger.addAppender(fileAppender);
			logger.setLevel(Level.INFO);		
			Logger.getLogger(DBModelCacheExtended.class).setLevel(Level.INFO);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DBModelCacheExtended cache = new DBModelCacheExtended("cache2", SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		cache.deleteCache();
		cache.createCache();
		cache.fillCache(100);
	}
	
	@Test
	public void objectSerializationTest(){
		try {
			ModelGenerator modelGen = new ModelGenerator(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
			Model model = modelGen.createModel("http://dbpedia.org/resource/Berlin", Strategy.CHUNKS, 2);
			StringWriter sw = new StringWriter();
			model.write(sw, "TURTLE");
			sw.flush();
			String modelString = sw.toString();
			File f = new File("model.txt");
			Files.writeObjectToFile(modelString, f);
			long startTime = System.currentTimeMillis();
			modelString = (String) Files.readObjectfromFile(f);
			model = ModelFactory.createDefaultModel();
			model.read(new StringReader(modelString), null, "TURTLE");
			System.out.println(System.currentTimeMillis()-startTime);
			
			
			ExtractionDBCache cache = new ExtractionDBCache("cache");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
