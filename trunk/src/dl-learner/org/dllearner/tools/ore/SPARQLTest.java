package org.dllearner.tools.ore;

import java.util.SortedSet;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.dllearner.utilities.learn.LearnSPARQLConfiguration;
import org.dllearner.utilities.learn.LearnSparql;

public class SPARQLTest{
	
	public static void main(String[] args){
	
		SparqlEndpoint endPoint = SparqlEndpoint.getEndpointDBpedia();
		
		SPARQLTasks task = new SPARQLTasks(endPoint);
	
		AutomaticPositiveExampleFinderSPARQL pos = new AutomaticPositiveExampleFinderSPARQL(task);
		pos.makePositiveExamplesFromConcept("angela_merkel");
		SortedSet<String> posExamples = pos.getPosExamples();
		
		AutomaticNegativeExampleFinderSPARQL neg = new AutomaticNegativeExampleFinderSPARQL(posExamples, task);
		SortedSet<String> negExamples = neg.getNegativeExamples(20);
		
		LearnSPARQLConfiguration conf = new LearnSPARQLConfiguration();
		LearnSparql learn = new LearnSparql(conf);
		
		LearningAlgorithm la = null;
//		try {
//			la = learn.learn(posExamples, negExamples);
//		} catch (ComponentInitException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (LearningProblemUnsupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		la.start();
	}
}
