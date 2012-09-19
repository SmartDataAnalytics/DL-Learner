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

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;

/**
 * Test for component based design.
 * 
 * @author Jens Lehmann
 * 
 */
public class ComponentTest {

	/**
	 * @param args
	 * @throws ComponentInitException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws ComponentInitException, MalformedURLException {
		
		// create knowledge source
		String example = "../examples/family/uncle.owl";
		KnowledgeSource source = new OWLFile(example);
		
		// create OWL API reasoning service with standard settings
		AbstractReasonerComponent reasoner = new OWLAPIReasoner(Collections.singleton(source));
		reasoner.init();
		
		// create a learning problem and set positive and negative examples
		PosNegLPStandard lp = new PosNegLPStandard(reasoner);
		Set<Individual> positiveExamples = new TreeSet<Individual>();
		positiveExamples.add(new Individual("http://localhost/foo#heinz"));
		positiveExamples.add(new Individual("http://localhost/foo#alex"));
		Set<Individual> negativeExamples = new TreeSet<Individual>();
		negativeExamples.add(new Individual("http://localhost/foo#jan"));
		negativeExamples.add(new Individual("http://localhost/foo#anna"));
		negativeExamples.add(new Individual("http://localhost/foo#hanna"));
		lp.setPositiveExamples(positiveExamples);
		lp.setNegativeExamples(negativeExamples);
		lp.init();
		
		// create the learning algorithm
		AbstractCELA la = new OCEL(lp, reasoner);
		la.init();
	
		// start the algorithm and print the best concept found
		la.start();
		System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
	}

}
