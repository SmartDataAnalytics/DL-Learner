/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.statistics.SimpleClock;

public class SparqlEndpointTest {
	private static Logger logger = Logger.getRootLogger();
	static Set<String> working = new HashSet<String>();
	static Set<String> notworking = new HashSet<String>();
	
	public static void main(String[] args) {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		FileAppender fileAppender =null; ;
		try{
			fileAppender = new FileAppender(layout,"endpoints.txt",false);
		}catch (Exception e) {e.printStackTrace();}
		logger.removeAllAppenders();
		logger.addAppender(fileAppender);
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.TRACE);
		Logger.getLogger(SparqlKnowledgeSource.class).setLevel(Level.WARN);
		//SELECT DISTINCT ?c WHERE {[] a ?c }LIMIT 100
		
		
		List<SparqlEndpoint> ll  = SparqlEndpoint.listEndpoints();
		
		
		
		int i=1;
		for (int j = 0; j < ll.size(); j++) {
			
			testEndPoint(ll.get(j));
			if(i==3)break;
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
		JamonMonitorLogger.printAllSortedByLabel();
	}
	
	
	public static void  testEndPoint(SparqlEndpoint se) {
		SimpleClock sc =  new SimpleClock(); 
		try{
		
		String SPARQLquery ="" +
			"SELECT DISTINCT ?c " +
			"WHERE {[] a ?c }" +
			"LIMIT 100";
		
		SPARQLquery ="SELECT DISTINCT ?c WHERE {[] a ?c }LIMIT 100";
		SortedSet<String> tmp = new SPARQLTasks(se).queryAsSet(SPARQLquery, "c");
		int i =tmp.size();
	
		
		working.add(sc.getAndSet("endpoint working: "+se.getURL()+" ("+((i==100)?"more than 100 concepts":"about "+i+" concepts")+" )"));
		}catch (Exception e) {notworking.add(sc.getAndSet("endpoint NOT working: "+se.getURL()));}
	}
	
}
