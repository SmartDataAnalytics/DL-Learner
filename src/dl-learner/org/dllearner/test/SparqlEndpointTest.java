package org.dllearner.test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.utilities.JenaResultSetConvenience;
import org.dllearner.utilities.SimpleClock;

import com.hp.hpl.jena.query.ResultSet;

public class SparqlEndpointTest {
	private static Logger logger = Logger.getRootLogger();
	static Set<String> working = new HashSet<String>();
	static Set<String> notworking = new HashSet<String>();
	
	public static void main(String[] args) {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.DEBUG);
		Logger.getLogger(SparqlKnowledgeSource.class).setLevel(Level.INFO);
		
		
		
		LinkedList<SparqlEndpoint> ll  = SparqlEndpoint.listEndpoints();
		
		
		
		int i=1;
		for (int j = 0; j < ll.size(); j++) {
			
			testEndPoint(ll.get(j));
			logger.info("finished "+i+" of "+ll.size());
			i++;
		}
		logger.info("**************");
		for (String str : working) {
			logger.info(str);
		}
		for (String str : notworking) {
			logger.info(str);

		}
		//set.add(SparqlEndpoint.);
		
	}
	
	
	public static void  testEndPoint(SparqlEndpoint se) {
		SimpleClock sc =  new SimpleClock(); 
		try{
		
		String query ="" +
			"SELECT DISTINCT ?c " +
			"WHERE {[] a ?c }" +
			"LIMIT 100";
		
		query ="SELECT DISTINCT ?c WHERE {[] a ?c }LIMIT 100";
		
		SparqlQuery s = new SparqlQuery(query,se);
	
		s.send();
		String result = s.getResult();
		ResultSet rs = SparqlQuery.JSONtoResultSet(result);
		
		JenaResultSetConvenience jsr = new JenaResultSetConvenience(rs);
		int i = jsr.getStringListForVariable("c").size();
		
		working.add(sc.getAndSet("endpoint working: "+se.getURL()+" ("+((i==100)?"more than 100 concepts":"about "+i+" concepts")+" )"));
		}catch (Exception e) {notworking.add(sc.getAndSet("endpoint NOT working: "+se.getURL()));}
	}
	
}
