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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.aquisitors.SparqlTupleAquisitor;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.extraction.Configuration;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.extraction.Node;
import org.dllearner.kb.extraction.OWLAPIOntologyCollector;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryMaker;

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
		Logger.getLogger(SparqlQuery.class).setLevel(Level.INFO);
		logger.warn("If you use a remote sparql endpoint over http, it will be very slow due to network latency");
		// String test2 = "http://www.extraction.org/config#dbpediatest";
		// String test = "http://www.extraction.org/config#localjoseki";
		try {
			// URI u = new URI(test);
			int recursionDepth=1;
			Manager m = new Manager();
			Manipulator manipulator = Manipulator.getDefaultManipulator();

			TupleAquisitor tupleAquisitor = 
				new SparqlTupleAquisitor(SparqlQueryMaker.getAllowYAGOFilter(),SPARQLTasks.getPredefinedSPARQLTasksWithCache("DBPEDIA"));
			
			boolean getAllSuperClasses = true;
			boolean closeAfterRecursion = true;
			boolean getPropertyInformation = false;
			boolean dissolveBlankNodes = false;
			int breakSuperClassesAfter = 1000;
			
			String ontologyURI = "http://www.fragment.org/fragment";
			String physicalURI= "fragmentOntology.owl";
			OWLAPIOntologyCollector collector= new OWLAPIOntologyCollector( ontologyURI,  physicalURI);
		
			
			Configuration conf = new Configuration (
					tupleAquisitor,
					manipulator, 
					recursionDepth,
					getAllSuperClasses,
					closeAfterRecursion,
					getPropertyInformation,
					breakSuperClassesAfter,
					dissolveBlankNodes,
					collector
					);
			

			
			m.useConfiguration(conf);
		
			String example = "http://dbpedia.org/resource/Angela_Merkel";
			
			Set<String> startingInstances = new TreeSet<String>();
			startingInstances.add(example);
			
			List<Node> seedNodes=new ArrayList<Node>();
			
			//if(!threaded){
			seedNodes = m.extract(startingInstances);
			
			
			boolean saveOntology = true;
			
			
			m.getOWLAPIOntologyForNodes(seedNodes, saveOntology);
		
			URL ontologyFragmentURL = m.getPhysicalOntologyURL();
			
			logger.info("the ontology has been saved at: "+ontologyFragmentURL);
			
			//JamonMonitorLogger.printAllSortedByLabel();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
