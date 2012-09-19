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

package org.dllearner.algorithms.gp;

import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.ScorePosNeg;

/**
 * This class represents a program, i.e. an individual.
 * 
 * @author Jens Lehmann
 * 
 */
public class Program {

	// public static int fitnessEvaluations = 0;

	private Description hypothesis;

	// private Concept extendedHypothesis;

	private Description adc;

	private ScorePosNeg score;

	// private Score scoreAdc;
	
	// private LearningProblem learningProblem;

	private double fitness;
	
	/**
	 * Create a new program.
	 * 
	 */
	public Program(ScorePosNeg score, Description hypothesis) {
		this(score, hypothesis, null);
	}

	public Program(ScorePosNeg score, Description hypothesis, Description adc) {
		// this.learningProblem = learningProblem;
		this.score = score;
		this.hypothesis = hypothesis;
		this.adc = adc;
		// TODO: es sind Prozent pro Längeneinheit, also ist hier die
		// Implementierung falsch !!
		// fitness = score.getScore() - hypothesis.getLength() * Config.percentPerLengthUnit;
		// => in getScore() ist jetzt schon der length penalty integriert
		fitness = score.getScoreValue();
		// fitnessEvaluations++;
		
		// System.out.println("new program: " + hypothesis);
		
		/*
		// falls R�ckgabetyp spezifiziert ist, dann muss hier der Baum
		// entsprechend ver�ndert werden
		if (!Config.returnType.equals("")) {
			// newRoot.addChild(new AtomicConcept(Config.returnType));
			// newRoot.addChild(hypothesis);
			Concept newRoot = new Conjunction(new AtomicConcept(Config.returnType),hypothesis);			
			// parent wieder auf null setzen, damit es nicht inkonsistent wird
			// TODO: ist nicht wirklich elegant und auch inkompatibel mit
			// dem Hill-Climbing-Operator
			hypothesis.setParent(null);
			extendedHypothesis = newRoot;
		} else
			extendedHypothesis = hypothesis;

		// fitness evaluation on program creation
		calculateFitness();
		*/
	}

	// nur aufrufen, wenn Programm ver�ndert wird und deshalb die Fitness neu
	// berechnet werden muss
	/*
	public void calculateFitness() {
		if(Config.GP.adc)
			score = learningProblem.computeScore(extendedHypothesis, adc);
		else
			score = learningProblem.computeScore(extendedHypothesis);

		fitness = score.getScore() - 0.1 * hypothesis.getConceptLength();
		
		if (Config.GP.adc)
			fitness -= 0.1 * adc.getConceptLength();

		// zus�tzliche Bestrafung f�r sehr lange Definitionen
		if(hypothesis.getNumberOfNodes()>50)
			fitness -= 10;
		fitnessEvaluations++;
	}
	*/

	/**
	 * Returns the previously calculated fitness of the program.
	 * 
	 * @return The fitness of the program.
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * Returns the program tree corresponding to this program.
	 * 
	 * @return The program tree.
	 */
	public Description getTree() {
		return hypothesis;
	}

	public ScorePosNeg getScore() {
		return score;
	}

	public Description getAdc() {
		return adc;
	}
}
