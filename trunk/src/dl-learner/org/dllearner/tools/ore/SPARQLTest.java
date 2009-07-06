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

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;

/**
 * Test class for SPARQL mode.
 * @author Lorenz Buehmann
 *
 */
public class SPARQLTest{
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		String example = "Angela_Merkel";
		ComponentManager cm = ComponentManager.getInstance();
	
		SparqlEndpoint endPoint = SparqlEndpoint.getEndpointDBpedia();
		
		SPARQLTasks task = new SPARQLTasks(endPoint);
	
		AutomaticPositiveExampleFinderSPARQL pos = new AutomaticPositiveExampleFinderSPARQL(task);
		pos.makePositiveExamplesFromConcept(example);
		SortedSet<String> posExamples = pos.getPosExamples();
		
		AutomaticNegativeExampleFinderSPARQL neg = new AutomaticNegativeExampleFinderSPARQL(posExamples, task, new TreeSet<String>());
		SortedSet<String> negExamples = neg.getNegativeExamples(20);
		System.out.println(negExamples);
		
		
		
		
		try {
			
			
			
			
			SparqlKnowledgeSource ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
			cm.applyConfigEntry(ks, "predefinedEndpoint", "DBPEDIA");
			ks.getConfigurator().setInstances(posExamples);
			ks.init();
			ReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
			reasoner.init();
			PosOnlyLP lp = cm.learningProblem(PosOnlyLP.class, reasoner);
			lp.getConfigurator().setPositiveExamples(posExamples);
			lp.init();
			LearningAlgorithm la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
			la.init();
					
			la.start();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LearningProblemUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
