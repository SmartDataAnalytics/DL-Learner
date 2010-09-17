package org.dllearner.test;

import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;

public class TestGetExampleBug {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
				Logger logger = Logger.getRootLogger();
				SimpleLayout layout = new SimpleLayout();
				ConsoleAppender consoleAppender = new ConsoleAppender(layout);
				logger.removeAllAppenders();
				logger.addAppender(consoleAppender);
				logger.setLevel(Level.TRACE);		
				//Logger.getLogger(SparqlQuery.class).setLevel(Level.DEBUG);
				
				try {
				String OntowikiUrl="http://localhost/ontowiki/service/sparql";

			
				
				SortedSet<String> positiveSet = new TreeSet<String>();
				positiveSet.add("http://3ba.se/conferences/JensLehmann");
				positiveSet.add("http://3ba.se/conferences/MuhammadAhtishamAslam");
				positiveSet.add("http://3ba.se/conferences/SebastianDietzold");
				positiveSet.add("http://3ba.se/conferences/ThomasRiechert");
				//positiveSet.add("http://3ba.se/conferences/FMILeipzig");

				SPARQLTasks st = new SPARQLTasks(new SparqlEndpoint(new URL(OntowikiUrl)));
				AutomaticNegativeExampleFinderSPARQL ane = 
					new AutomaticNegativeExampleFinderSPARQL(positiveSet,st, new TreeSet<String>());
				SortedSet<String> negExamples = new TreeSet<String>();
				int results=100;
				if(negExamples.isEmpty()){
					//ane.makeNegativeExamplesFromRelatedInstances(positiveSet, "http://3ba.se/conferences/");
					 negExamples = ane.getNegativeExamples(results);
					 if(negExamples.isEmpty()){
						 ane.makeNegativeExamplesFromSuperClassesOfInstances(positiveSet, 500);
						 negExamples = ane.getNegativeExamples(results);
						 if(negExamples.isEmpty()) {
							 ane.makeNegativeExamplesFromRandomInstances();
							 negExamples = ane.getNegativeExamples(results);
						 }
					 }
				}
				
				System.out.println(ane.getNegativeExamples(100));
				}catch (Exception e) {
					e.printStackTrace();
				}
				
				/*$negExamples=$client->getNegativeExamples($id,$ksID,$posExamples,count($posExamples),"http://localhost/ontowiki/service/sparql");
				$negExamples=$negExamples->item;
				$client->setLearningProblem($id, "posNegDefinition");
				$client->setPositiveExamples($id, $posExamples);
				$client->setNegativeExamples($id, $negExamples);

//				 choose refinement operator approach
				$client->setLearningAlgorithm($id, "refexamples");
//				 you can add the following to apply a config option to a component, e.g. ignore a concept
//				$client->applyConfigEntryStringArray($id, $la_id, "ignoredConcepts", array('http://example.com/father#male'));

				$client->initAll($id);

//				 learn concept
				echo 'start learning ... ';
//				 get only concept
//				 $concept = $client->learn($id, "manchester");
//				 get concept and additional information in JSON syntax
				$concept = $client->learnDescriptionsEvaluated($id, 5);
				echo 'OK <br />';
				echo 'solution: <pre>' . $concept . '</pre>';

				?>*/

		
		
	}

}
