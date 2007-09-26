/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.core;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.DefinitionLPTwoValued;
import org.dllearner.reasoning.DIGReasonerNew;

/**
 * Test for component based design.
 * 
 * @author Jens Lehmann
 * 
 */
public class ComponentTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String example = null;
		try {
			example = new File("examples/father.owl").toURI().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(0);
		}		
		
		// get singleton instance of component manager
		ComponentManager cm = ComponentManager.getInstance();
		
		// create knowledge source
		KnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		cm.applyConfigEntry(source, "url", example);
		source.init();
		
		ReasoningService rs = cm.reasoningService(DIGReasonerNew.class, source);
		rs.init();
		
		Set<String> positiveExamples = new TreeSet<String>();
		positiveExamples.add("http://example.com/father#stefan");
		positiveExamples.add("http://example.com/father#markus");
		positiveExamples.add("http://example.com/father#martin");
		Set<String> negativeExamples = new TreeSet<String>();
		negativeExamples.add("http://example.com/father#heinz");
		negativeExamples.add("http://example.com/father#anna");
		negativeExamples.add("http://example.com/father#michelle");
		
		LearningProblemNew lp = cm.learningProblem(DefinitionLPTwoValued.class, rs);
		cm.applyConfigEntry(lp, "positiveExamples", positiveExamples);
		cm.applyConfigEntry(lp, "negativeExamples", negativeExamples);
		lp.init();
		
		LearningAlgorithmNew la = cm.learningAlgorithm(RandomGuesser.class, lp);
		cm.applyConfigEntry(la, "numberOfTrees", 100);
		cm.applyConfigEntry(la, "maxDepth", 5);
		la.init();
		
		la.start();
		
		System.out.println(la.getBestSolution());
	}

}
