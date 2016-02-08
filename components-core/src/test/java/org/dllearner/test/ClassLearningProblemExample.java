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

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.*;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Simple test class that learns a description of a given class.
 * @author Lorenz Buehmann
 *
 */
public class ClassLearningProblemExample {
	
	public static void main(String[] args) throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		
		// load a knowledge base
		String ontologyPath = "../examples/swore/swore.rdf";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(ontologyPath));
		AbstractKnowledgeSource source = new OWLAPIOntology(ontology);
		source.init();
		
		// set up a closed-world reasoner
		AbstractReasonerComponent reasoner = new ClosedWorldReasoner(source);
		reasoner.init();
		
		// create a learning problem and set the class to describe
		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/CustomerRequirement")));
		lp.init();
		
		// create the learning algorithm
		final AbstractCELA la = new CELOE(lp, reasoner);
		la.init();
	
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			int progress = 0;
			List<EvaluatedDescriptionClass> result;
			@Override
			public void run() {
				if(la.isRunning()){
					System.out.println(la.getCurrentlyBestEvaluatedDescriptions());
				}
			}
			
		}, 1000, 500);
		
		// start the algorithm and print the best concept found
		la.start();
		timer.cancel();
		List<? extends EvaluatedDescription> currentlyBestEvaluatedDescriptions = la.getCurrentlyBestEvaluatedDescriptions(0.8);
		System.out.println(currentlyBestEvaluatedDescriptions);
	}

}
