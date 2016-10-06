/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * Check different reasoner implementations.
 * @author Lorenz Buehmann
 *
 */
public class ReasonerTest {
	
	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.OFF);
		Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);
		
		ReasonerImplementation[] reasonerImplementations = new ReasonerImplementation[]{
				ReasonerImplementation.ELK,
//				ReasonerImplementation.TROWL,
				ReasonerImplementation.JFACT,
				ReasonerImplementation.HERMIT,
				ReasonerImplementation.PELLET};
		
		int maxExecutionTimeInSeconds = 10;
		
		String ontologyURL = "../examples/swore/swore.rdf";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(ontologyURL));
		
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/CustomerRequirement"));
		
		for (ReasonerImplementation reasonerImpl : reasonerImplementations) {
			System.out.println("Testing " + reasonerImpl);
			
			try {
				OWLAPIReasoner reasoner = new OWLAPIReasoner(ks);
				reasoner.setReasonerImplementation(reasonerImpl);
				reasoner.setUseFallbackReasoner(true);
				reasoner.init();
				Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);
				
				ClosedWorldReasoner closedWorldReasoner = new ClosedWorldReasoner(ks);
				closedWorldReasoner.setReasonerComponent(reasoner);
				closedWorldReasoner.init();
				
				ClassLearningProblem lp = new ClassLearningProblem(closedWorldReasoner);
				lp.setClassToDescribe(classToDescribe);
				lp.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
				lp.init();
				
				CELOE la = new CELOE(lp, closedWorldReasoner);
				la.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
				la.init();
				
				la.start();
			} catch (Exception | Error e) {
				e.printStackTrace();
			}
		}
		
	}

}
