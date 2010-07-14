/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
package org.dllearner.test.junit;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.KBFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.junit.Test;

/**
 * Tests for various heuristics employed in learning problems.
 * 
 * @author Jens Lehmann
 * 
 */
public class HeuristicTests {

	// when comparing heuristic values, this is the maximum allowed difference between actual and returned value
	// (there can always be precision errors, so cannot assume that actual and returned values are exactly equal)
	private static double delta = 0.000001;
	
	@Test
	public void classLearningTests() throws ComponentInitException, MalformedURLException {
		// create artificial ontology
		KB kb = new KB();
		String ns = "http://dl-learner.org/junit/";
		NamedClass[] nc = new NamedClass[5];
		for(int i=0; i<5; i++) {
			nc[i] = new NamedClass(ns + "A" + i);
		}
		Individual[] ind = new Individual[100];
		for(int i=0; i<100; i++) {
			ind[i] = new Individual(ns + "i" + i);
		}
		
		// A1 has instances i0 to i19 
		for(int i=0; i<20; i++) {
			kb.addAxiom(new ClassAssertionAxiom(nc[0],ind[i]));
		}
		
		// A2 has instances i10 to i29
		for(int i=10; i<30; i++) {
			kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[i]));
		}
		
		ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource ks = new KBFile(kb);
		ReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		ClassLearningProblem problem = cm.learningProblem(ClassLearningProblem.class, reasoner);
		ks.init();
		reasoner.init();

		// evaluate A2 wrt. A1 using Jaccard
		HeuristicTests.configureClassLP(problem, nc[0], "jaccard");
		// the value should be 10 (i10-i19) divided by 30 (i0-i29)
		assertEqualsClassLP(problem, nc[1], 1/(double)3);

	}
	
	// the class learning problem provides several ways to get the accuracy of a description, this method
	// tests all of those
	private static void assertEqualsClassLP(ClassLearningProblem problem, Description description, double accuracy) {
		assertEquals(accuracy, problem.getAccuracy(description), delta);
		assertEquals(accuracy, problem.getAccuracyOrTooWeak(description, 1.0), delta);
		assertEquals(accuracy, problem.computeScore(description).getAccuracy(), delta);
		assertEquals(accuracy, problem.evaluate(description).getAccuracy(), delta);
	}
	
	// convencience method to set the learning problem to a desired configuration (approximations disabled)
	private static void configureClassLP(ClassLearningProblem problem, NamedClass classToDescribe, String accuracyMethod) throws ComponentInitException {
		try {
			problem.getConfigurator().setClassToDescribe(new URL(classToDescribe.getName()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		problem.getConfigurator().setAccuracyMethod(accuracyMethod);
		problem.getConfigurator().setUseApproximations(false);
		problem.init();		
	}
	
	// convencience method to set the learning problem to a desired configuration
	private static void configureClassLP(ClassLearningProblem problem, NamedClass classToDescribe, String accuracyMethod, boolean useApproximations, double approxAccuracy) throws ComponentInitException {
		try {
			problem.getConfigurator().setClassToDescribe(new URL(classToDescribe.getName()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		problem.getConfigurator().setAccuracyMethod(accuracyMethod);
		problem.getConfigurator().setUseApproximations(useApproximations);
		problem.getConfigurator().setApproxAccuracy(approxAccuracy);
		problem.init();		
	}
	
}
