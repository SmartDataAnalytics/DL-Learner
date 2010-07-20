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
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.KBFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.Heuristics;
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
		
		// assert individuals to owl:Thing (such that they exist in the knowledge base)
		for(int i=0; i<100; i++) {
			kb.addAxiom(new ClassAssertionAxiom(Thing.instance,ind[i]));
		}
		
		// A0 has 20 instances (i0 to i19) 
		for(int i=0; i<20; i++) {
			kb.addAxiom(new ClassAssertionAxiom(nc[0],ind[i]));
		}
		
		// A1 has 20 instances (i10 to i29)
		for(int i=10; i<30; i++) {
			kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[i]));
		}
		
		// A2 has 40 instances (i10 to i49)
		for(int i=10; i<50; i++) {
			kb.addAxiom(new ClassAssertionAxiom(nc[2],ind[i]));
		}		
		
		// A3 has 5 instances (i8 to i12)
		for(int i=8; i<13; i++) {
			kb.addAxiom(new ClassAssertionAxiom(nc[3],ind[i]));
		}
		
		ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource ks = new KBFile(kb);
		ReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		ClassLearningProblem problem = cm.learningProblem(ClassLearningProblem.class, reasoner);
		ks.init();
		reasoner.init();
		
		//// equivalent classes, no noise, no approximations ////
		
		// evaluate A2 wrt. A1 using Jaccard
		HeuristicTests.configureClassLP(problem, nc[0], "jaccard");
		// the value should be 10 (i10-i19) divided by 30 (i0-i29)
		assertEqualsClassLP(problem, nc[1], 1/(double)3);
		assertEqualsClassLP(problem, nc[2], 1/(double)5);
		
		HeuristicTests.configureClassLP(problem, nc[0], "pred_acc");
		// the value should be the sum of 10 (correct positives) and 970 (correct negatives) divided by 1000
		assertEqualsClassLP(problem, nc[1], (10+70)/(double)100);
		assertEqualsClassLP(problem, nc[2], (10+50)/(double)100);
		
		HeuristicTests.configureClassLP(problem, nc[0], "standard");
		assertEqualsClassLP(problem, nc[1], 0.5);
		assertEqualsClassLP(problem, nc[2], 0.375);
		
		HeuristicTests.configureClassLP(problem, nc[0], "fmeasure");
		// recall = precision = F1-score = 0.5
		assertEqualsClassLP(problem, nc[1], 0.5);
		// recall = 0.5, precision = 0.25, F1-score = 0.33...
		assertEqualsClassLP(problem, nc[2], 1/(double)3);
		
		// TODO: generalised F-Measure
		
		//// super class learning ////
		
		// Jaccard
		HeuristicTests.configureClassLP(problem, nc[0], "jaccard", false, false, 0.05);
		// the value should be 10 (i10-i19) divided by 30 (i0-i29)
		assertEqualsClassLP(problem, nc[1], 1/(double)3);
		assertEqualsClassLP(problem, nc[2], 1/(double)5);		
		
		HeuristicTests.configureClassLP(problem, nc[0], "pred_acc", false, false, 0.05);
		assertEqualsClassLP(problem, nc[1], 5/(double)7);
		assertEqualsClassLP(problem, nc[2], 4/(double)7);
		
		HeuristicTests.configureClassLP(problem, nc[0], "standard");
		assertEqualsClassLP(problem, nc[1], 0.5);
		assertEqualsClassLP(problem, nc[2], 0.4375);		
		
		HeuristicTests.configureClassLP(problem, nc[0], "fmeasure", false, false, 0.05);
		// recall = precision = F1-score = 0.5
		assertEqualsClassLP(problem, nc[1], 0.5);
		// recall = 0.5, precision = 0.25, F1-score = 0.33...
		assertEqualsClassLP(problem, nc[2], 0.366025403784);		
		
		// TODO: generalised F-Measure
		
		//// noise tests ////
		
		HeuristicTests.configureClassLP(problem, nc[0], "fmeasure", false, true, 0.05);
		assertEquals(problem.getAccuracyOrTooWeak(nc[3], 0.5),-1,delta);
		
		// TODO: test approximations

		

	}
	
	@Test
	public void approximationTests() {
		// perform F-Measure example in ontology engineering paper, which was computed on paper
		// TODO: compute again, because unit tests fails (probably rounding errors)
		double[] approx1 = Heuristics.getFMeasureApproximation(800, 0.8, 1, 10000, 41, 31);
		assertEquals(0.0505, approx1[1], delta);
		double[] approx2 = Heuristics.getFMeasureApproximation(800, 0.8, 1, 10000, 42, 32);
		assertEquals(0.1699, approx2[0], delta);
		assertEquals(0.0489, approx2[1], delta);
		
		// perform A-Measure example in ontology engineering paper
		// setup: 1000 class instances, 10000 relevant instances, delta=0.10
		// input1: 90 out of 95 tests => no success para 1, 91 out of 96 => success
		// input2: using estimation from input 1, 32 out of 64 => success
		// overall accuracy: 64%
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
	private static void configureClassLP(ClassLearningProblem problem, NamedClass classToDescribe, String accuracyMethod, boolean equivalenceLearning, boolean useApproximations, double approxAccuracy) throws ComponentInitException {
		try {
			problem.getConfigurator().setClassToDescribe(new URL(classToDescribe.getName()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		problem.getConfigurator().setType("superClass");
		problem.getConfigurator().setAccuracyMethod(accuracyMethod);
		problem.getConfigurator().setUseApproximations(useApproximations);
		problem.getConfigurator().setApproxAccuracy(approxAccuracy);
		problem.init();		
	}
	
}
