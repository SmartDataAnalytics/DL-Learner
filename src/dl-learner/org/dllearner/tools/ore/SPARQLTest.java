/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.tools.ore;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.LearningAlgorithm;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.dllearner.utilities.learn.LearnSPARQLConfiguration;

/**
 * Test class for SPARQL mode.
 * @author Lorenz Buehmann
 *
 */
public class SPARQLTest{
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
	
		SparqlEndpoint endPoint = SparqlEndpoint.getEndpointDBpedia();
		
		SPARQLTasks task = new SPARQLTasks(endPoint);
	
		AutomaticPositiveExampleFinderSPARQL pos = new AutomaticPositiveExampleFinderSPARQL(task);
		pos.makePositiveExamplesFromConcept("angela_merkel");
		SortedSet<String> posExamples = pos.getPosExamples();
		
		AutomaticNegativeExampleFinderSPARQL neg = new AutomaticNegativeExampleFinderSPARQL(posExamples, task, new TreeSet<String>());
		SortedSet<String> negExamples = neg.getNegativeExamples(20);
		
		LearnSPARQLConfiguration conf = new LearnSPARQLConfiguration();
		
		// TODO Please update class to either use ComponentManager or 
		// add a convenience constructor to org.dllearner.utilities.components.ComponentCombo 
		
//		LearnSparql learn = new LearnSparql(conf);
		
		LearningAlgorithm la = null;
		
//			try {
		//la = learn.learn(posExamples, negExamples, OWLAPIReasoner.class);
//			} catch (ComponentInitException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (LearningProblemUnsupportedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		
		la.start();
	}
}
