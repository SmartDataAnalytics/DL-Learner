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
	
//	@Test
//	public void fillCacheBatchedTest(){
//		try {
//			SimpleLayout layout = new SimpleLayout();
//			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
//			FileAppender fileAppender = new FileAppender( layout, "log/fillCacheBatched.log", false );
//			Logger logger = Logger.getRootLogger();
//			logger.removeAllAppenders();
//			logger.addAppender(consoleAppender);
//			logger.addAppender(fileAppender);
//			logger.setLevel(Level.INFO);		
//			Logger.getLogger(DBModelCacheExtended.class).setLevel(Level.INFO);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		DBModelCacheExtended cache = new DBModelCacheExtended("cache1", SparqlEndpoint.getEndpointDBpediaLiveAKSW());
//		cache.fillCacheBatched(1000);
//	}
	
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
		cache.fillCache(1000);
	}

}
