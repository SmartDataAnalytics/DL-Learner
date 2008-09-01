/**
 * Copyright (C) 2007, Sebastian Hellmann
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
 *
 */
package org.dllearner.test;

import java.io.File;
import java.io.FileWriter;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.aquisitors.SparqlTupleAquisitorImproved;
import org.dllearner.kb.extraction.Configuration;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryMaker;
import org.dllearner.scripts.NT2RDF;
import org.dllearner.utilities.JamonMonitorLogger;

/**
 * Test class, uses the whole thing
 * 
 * @author Sebastian Hellmann
 * 
 */
public class SparqlExtractionTest {
	
	private static Logger logger = Logger.getRootLogger();
	

	public static void main(String[] args) {
		System.out.println("Start");
		
//		 create logger (a simple logger which outputs
		// its messages to the console)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.INFO);		
		Logger.getLogger(SparqlQuery.class).setLevel(Level.DEBUG);
		
		// String test2 = "http://www.extraction.org/config#dbpediatest";
		// String test = "http://www.extraction.org/config#localjoseki";
		try {
			// URI u = new URI(test);
			int recursionDepth=2;
			Manager m = new Manager();
			Configuration conf = new Configuration (
					new SparqlTupleAquisitorImproved(SparqlQueryMaker.getAllowYAGOFilter(),
							SPARQLTasks.getPredefinedSPARQLTasksWithCache("DBPEDIA"),recursionDepth),
					Manipulator.getDefaultManipulator(), 
					recursionDepth,
					true,
					true,
					false,
					200
					);
			m.useConfiguration(conf);

			String u2 = "http://dbpedia.org/resource/Angela_Merkel";
			
			String filename = "cache/"+System.currentTimeMillis() + ".nt";
			FileWriter fw = new FileWriter(new File(filename), true);
			fw.write(m.extract(u2));
			fw.flush();
			fw.close();
			
			NT2RDF.convertNT2RDF(filename);
			
			JamonMonitorLogger.printAllSortedByLabel();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
