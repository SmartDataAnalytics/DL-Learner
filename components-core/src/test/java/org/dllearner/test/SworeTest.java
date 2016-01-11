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

import java.net.MalformedURLException;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Test for learning on SWORE ontology.
 * 
 * @author Jens Lehmann
 * 
 */
public class SworeTest {

	/**
	 * @param args
	 * @throws ComponentInitException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws ComponentInitException, MalformedURLException {
		
		// create knowledge source
		String example = "examples/swore/swore.rdf";
		AbstractKnowledgeSource source = new OWLFile(example);
		
		// create OWL API reasoning service with standard settings
//		AbstractReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, source);
//		reasoner.init();
		
		// set up a closed-world reasoner
		AbstractReasonerComponent reasoner = new ClosedWorldReasoner(source);
		reasoner.init();
		
		// create a learning problem and set positive and negative examples
		PosNegLPStandard lp = new PosNegLPStandard(reasoner);
		Set<OWLIndividual> positiveExamples = new TreeSet<>();
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		positiveExamples.add(df.getOWLNamedIndividual(IRI.create("http://ns.softwiki.de/req/important")));
		positiveExamples.add(df.getOWLNamedIndividual(IRI.create("http://ns.softwiki.de/req/very_important")));
		Set<OWLIndividual> negativeExamples = new TreeSet<>();
		negativeExamples.add(df.getOWLNamedIndividual(IRI.create("http://ns.softwiki.de/req/Topic")));
		lp.setPositiveExamples(positiveExamples);
		lp.setNegativeExamples(negativeExamples);
		lp.init();
		
		// create the learning algorithm
		AbstractCELA la = null;
		la = new OCEL(lp, reasoner);
		la.init();
	
		// start the algorithm and print the best concept found
		la.start();
		System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
	}

}
