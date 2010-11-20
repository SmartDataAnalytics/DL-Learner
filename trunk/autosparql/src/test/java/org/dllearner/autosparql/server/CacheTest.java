package org.dllearner.autosparql.server;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.autosparql.server.cache.DBModelCache;
import org.dllearner.autosparql.server.cache.DBModelCacheComplete;
import org.dllearner.autosparql.server.cache.DBModelCacheExtended;
import org.dllearner.autosparql.server.cache.DBModelCacheSingle;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

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
	
	@Test
	public void test3(){
		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			FileAppender fileAppender = new FileAppender( layout, "log/dbpedia_cache_creation.log", false );
			Logger logger = Logger.getRootLogger();
			logger.removeAllAppenders();
//			logger.addAppender(consoleAppender);
			logger.addAppender(fileAppender);
			logger.setLevel(Level.INFO);		
			Logger.getLogger(DBModelCacheExtended.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DBModelCache cache = new DBModelCacheExtended("cache", SparqlEndpoint.getEndpointDBpediaLiveAKSW());
	}

}
