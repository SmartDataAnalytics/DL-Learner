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

import java.io.File;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;

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
		
		// get singleton instance of component manager
		ComponentManager cm = ComponentManager.getInstance();
		
		// create knowledge source
		AbstractKnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		String example = "examples/swore/swore.rdf";
		cm.applyConfigEntry(source, "url", new File(example).toURI().toURL());
		source.init();
		
		// create OWL API reasoning service with standard settings
		AbstractReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, source);
		reasoner.init();
		
		// create a learning problem and set positive and negative examples
		AbstractLearningProblem lp = cm.learningProblem(PosNegLPStandard.class, reasoner);
		Set<String> positiveExamples = new TreeSet<String>();
		positiveExamples.add("http://ns.softwiki.de/req/important");
		positiveExamples.add("http://ns.softwiki.de/req/very_important");
		Set<String> negativeExamples = new TreeSet<String>();
		negativeExamples.add("http://ns.softwiki.de/req/Topic");
		cm.applyConfigEntry(lp, "positiveExamples", positiveExamples);
		cm.applyConfigEntry(lp, "negativeExamples", negativeExamples);
		lp.init();
		
		// create the learning algorithm
		AbstractCELA la = null;
		try {
			la = cm.learningAlgorithm(OCEL.class, lp, reasoner);
			la.init();
		} catch (LearningProblemUnsupportedException e) {
			e.printStackTrace();
		}
	
		// start the algorithm and print the best concept found
		la.start();
		System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
	}

}
