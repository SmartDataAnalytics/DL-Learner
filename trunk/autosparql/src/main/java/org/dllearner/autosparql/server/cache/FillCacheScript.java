package org.dllearner.autosparql.server.cache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class FillCacheScript {

	public static void main(String[] args) {
		boolean useMySQL = args.length == 1 && args[0].equals("-mysql");
		DBModelCacheExtended cache = new DBModelCacheExtended("dbpedia_cache",
				SparqlEndpoint.getEndpointDBpediaLiveAKSW(), useMySQL);
		String databaseType;
		if (useMySQL) {
			databaseType = "mysql";
		} else {
			databaseType = "h2";
		}
		System.out.println("Using " + databaseType + " database");
		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			DailyRollingFileAppender fileAppender = new DailyRollingFileAppender(
					layout, "log/fillCache_" + databaseType + ".log",
					"'.'yyyy-MM-dd_HH");
			// FileAppender fileAppender = new FileAppender( layout,
			// "log/fillCache_" + databaseType + ".log", false );
			Logger logger = Logger.getRootLogger();
			logger.removeAllAppenders();
			logger.addAppender(consoleAppender);
			logger.addAppender(fileAppender);
			logger.setLevel(Level.INFO);
			Logger.getLogger(DBModelCacheExtended.class).setLevel(Level.INFO);
		} catch (IOException e) {
			e.printStackTrace();
		}
		cache.deleteCache();
		cache.createCache();
		Set<String> filters = new HashSet<String>();
		filters.add("http://dbpedia.org/resource/");
		cache.fillCache(filters);

	}

}
