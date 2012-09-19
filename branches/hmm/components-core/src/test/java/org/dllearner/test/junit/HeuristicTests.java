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

package org.dllearner.test.junit;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.KBFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Helper;
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
		AbstractKnowledgeSource ks = new KBFile(kb);
		AbstractReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		ClassLearningProblem problem = cm.learningProblem(ClassLearningProblem.class, reasoner);
		reasoner.init();
		
		//// equivalent classes, no noise, no approximations ////
		
		// evaluate A2 wrt. A1 using Jaccard
		HeuristicTests.configureClassLP(problem, nc[0], HeuristicType.JACCARD);
		// the value should be 10 (i10-i19) divided by 30 (i0-i29)
		assertEqualsClassLP(problem, nc[1], 1/(double)3);
		assertEqualsClassLP(problem, nc[2], 1/(double)5);
		
		HeuristicTests.configureClassLP(problem, nc[0], HeuristicType.PRED_ACC);
		// the value should be the sum of 10 (correct positives) and 970 (correct negatives) divided by 1000
		assertEqualsClassLP(problem, nc[1], (10+70)/(double)100);
		assertEqualsClassLP(problem, nc[2], (10+50)/(double)100);
		
		HeuristicTests.configureClassLP(problem, nc[0], HeuristicType.AMEASURE);
		assertEqualsClassLP(problem, nc[1], 0.5);
		assertEqualsClassLP(problem, nc[2], 0.375);
		
		HeuristicTests.configureClassLP(problem, nc[0], HeuristicType.FMEASURE);
		// recall = precision = F1-score = 0.5
		assertEqualsClassLP(problem, nc[1], 0.5);
		// recall = 0.5, precision = 0.25, F1-score = 0.33...
		assertEqualsClassLP(problem, nc[2], 1/(double)3);
		
		// TODO: generalised F-Measure
		
		//// super class learning ////
		
		// Jaccard
		HeuristicTests.configureClassLP(problem, nc[0], HeuristicType.JACCARD, false, false, 0.05);
		// the value should be 10 (i10-i19) divided by 30 (i0-i29)
		assertEqualsClassLP(problem, nc[1], 1/(double)3);
		assertEqualsClassLP(problem, nc[2], 1/(double)5);		
		
		HeuristicTests.configureClassLP(problem, nc[0], HeuristicType.PRED_ACC, false, false, 0.05);
		assertEqualsClassLP(problem, nc[1], 5/(double)7);
		assertEqualsClassLP(problem, nc[2], 4/(double)7);
		
		HeuristicTests.configureClassLP(problem, nc[0], HeuristicType.AMEASURE);
		assertEqualsClassLP(problem, nc[1], 0.5);
		assertEqualsClassLP(problem, nc[2], 0.4375);		
		
		HeuristicTests.configureClassLP(problem, nc[0], HeuristicType.FMEASURE, false, false, 0.05);
		// recall = precision = F1-score = 0.5
		assertEqualsClassLP(problem, nc[1], 0.5);
		// recall = 0.5, precision = 0.25, F1-score = 0.33...
		assertEqualsClassLP(problem, nc[2], 0.366025403784);		
		
		// TODO: generalised F-Measure
		
		//// noise tests ////
		
		HeuristicTests.configureClassLP(problem, nc[0], HeuristicType.FMEASURE, false, true, 0.05);
		assertEquals(problem.getAccuracyOrTooWeak(nc[3], 0.5),-1,delta);
		
		// TODO: test approximations

		

	}
	
	@Test
	public void posNegLPLearningTests() throws ComponentInitException {
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
		
		// A0
		kb.addAxiom(new ClassAssertionAxiom(nc[0],ind[0]));
		kb.addAxiom(new ClassAssertionAxiom(nc[0],ind[1]));
		kb.addAxiom(new ClassAssertionAxiom(nc[0],ind[5]));
		
		// A1
		kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[0]));
		kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[1]));
		kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[2]));
		kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[5]));
		
		ComponentManager cm = ComponentManager.getInstance();
		AbstractKnowledgeSource ks = new KBFile(kb);
		AbstractReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		PosNegLPStandard problem = cm.learningProblem(PosNegLPStandard.class, reasoner);
		reasoner.init();		
		
		Individual[] pos1 = new Individual[] {ind[0], ind[1], ind[2], ind[3], ind[4]};
		Individual[] neg1 = new Individual[] {ind[5], ind[6], ind[7], ind[8], ind[9]};
		
		// F-Measure and no approximations
		HeuristicTests.configurePosNegStandardLP(problem, pos1, neg1, "fmeasure", false);
		
		assertEqualsPosNegLPStandard(problem, nc[0], 0.5); // precision 2/3, recall 2/5
		assertEqualsPosNegLPStandard(problem, nc[1], 2/3d); // precision 3/4, recall 3/5
//		System.out.println(problem.getFMeasureOrTooWeakExact(nc[0], 1));
//		System.out.println(problem.getFMeasureOrTooWeakExact(nc[1], 1));
		
		// F-Measure and approximations
		HeuristicTests.configurePosNegStandardLP(problem, pos1, neg1, "fmeasure", true);
		
		assertEqualsPosNegLPStandard(problem, nc[0], 0.5); // precision 2/3, recall 2/5
		assertEqualsPosNegLPStandard(problem, nc[1], 2/3d); // precision 3/4, recall 3/5
	}
	
	
	@Test
	public void approximationTests() {
		// perform F-Measure example in ontology engineering paper, which was computed on paper
		double[] approx1 = Heuristics.getFScoreApproximation(800, 0.8, 1, 10000, 41, 31);
		// smaller delta, because of rounding errors
		assertEquals(0.050517, approx1[1], 0.001);
		double[] approx2 = Heuristics.getFScoreApproximation(800, 0.8, 1, 10000, 42, 32);
		// 0.1699 in the paper is just current precision divided by  multiplied by relevant instances
		// 0.1778 is the center of the interval
		assertEquals(0.178091, approx2[0], 0.001);
		assertEquals(0.048933, approx2[1], 0.001);
		
		// perform A-Measure example in ontology engineering paper
		// setup: 1000 class instances, 10000 relevant instances, delta=0.10
		// input1: 90 out of 95 tests => no success para 1, 91 out of 96 => success
		// input2: using estimation from input 1, 32 out of 64 => success
		// overall accuracy: 64%
		double[] approx1Step1 = Heuristics.getAScoreApproximationStep1(1, 1000, 90, 95);
		assertEquals(0.10006, approx1Step1[1], 0.001);
		// on paper, it works with 91 out of 96; but in the implementation only with
		// 92 out of 97 (probably rounding errors)
		double[] approx2Step1 = Heuristics.getAScoreApproximationStep1(1, 1000, 92, 97);
		assertTrue(approx2Step1[1] < 0.1);
		
		// double[] approxStep2 = Heuristics.getAScoreApproximationStep2(800, new double[] {approx2Step1[0]-0.5*approx2Step1[1], approx2Step1[0]+0.5*approx2Step1[1]}, 1, 10000, 64, 32);
		// example computed by hand (note that it differs from the paper example in that
		// we do not use the square root)
		double[] approxStep2 = Heuristics.getAScoreApproximationStep2(800, approx2Step1, 1, 10000, 64, 32);
		assertEquals(0.49822461, approxStep2[0]-0.5*approxStep2[1], 0.001);
		assertEquals(0.5771179, approxStep2[0]+0.5*approxStep2[1], 0.001);
//		System.out.println(approxStep2[0] + " " + approxStep2[1]);
	}
	
	// the class learning problem provides several ways to get the accuracy of a description, this method
	// tests all of those
	private static void assertEqualsClassLP(ClassLearningProblem problem, Description description, double accuracy) {
		assertEquals(accuracy, problem.getAccuracy(description), delta);
		assertEquals(accuracy, problem.getAccuracyOrTooWeak(description, 1.0), delta);
		assertEquals(accuracy, problem.computeScore(description).getAccuracy(), delta);
		assertEquals(accuracy, problem.evaluate(description).getAccuracy(), delta);
	}
	
	private static void assertEqualsPosNegLPStandard(PosNegLPStandard problem, Description description, double accuracy) {
		assertEquals(accuracy, problem.getAccuracy(description), delta);
		assertEquals(accuracy, problem.getAccuracyOrTooWeak(description, 1.0), delta);
		assertEquals(accuracy, problem.computeScore(description).getAccuracy(), delta);
		assertEquals(accuracy, problem.evaluate(description).getAccuracy(), delta);
	}
	
	// convencience method to set the learning problem to a desired configuration (approximations disabled)
	private static void configureClassLP(ClassLearningProblem problem, NamedClass classToDescribe, HeuristicType accuracyMethod) throws ComponentInitException {
		problem.setClassToDescribe(classToDescribe);
		problem.setHeuristic(accuracyMethod);
		problem.setUseApproximations(false);
		problem.init();	
		
	}
	
	// convencience method to set the learning problem to a desired configuration
	private static void configureClassLP(ClassLearningProblem problem, NamedClass classToDescribe, HeuristicType accuracyMethod, boolean equivalenceLearning, boolean useApproximations, double approxAccuracy) throws ComponentInitException {
		problem.setClassToDescribe(classToDescribe);
//		problem.getConfigurator().setType("superClass");
		problem.setEquivalence(false);
		problem.setHeuristic(accuracyMethod);
		problem.setUseApproximations(useApproximations);
		problem.setApproxDelta(approxAccuracy);
		problem.init();		
	}
	
//	@SuppressWarnings("unchecked")
	private static void configurePosNegStandardLP(PosNegLPStandard problem, Individual[] positiveExamples, Individual[] negativeExamples, String accuracyMethod, boolean useApproximations) throws ComponentInitException {
		Set<Individual> s1 = new TreeSet<Individual>(Arrays.asList(positiveExamples));
		Set<Individual> s2 = new TreeSet<Individual>(Arrays.asList(negativeExamples));
		HeuristicTests.configurePosNegStandardLP(problem, s1, s2, accuracyMethod, useApproximations);
	}
	
	// convencience method to set the learning problem to a desired configuration (approximations disabled)
	private static void configurePosNegStandardLP(PosNegLPStandard problem, Set<Individual> positiveExamples, Set<Individual> negativeExamples, String accuracyMethod, boolean useApproximations) throws ComponentInitException {
		problem.setPositiveExamples(positiveExamples);
		problem.setNegativeExamples(negativeExamples);
		problem.setAccuracyMethod(accuracyMethod);
		problem.setUseApproximations(useApproximations);
		problem.init();		
	}	
}
