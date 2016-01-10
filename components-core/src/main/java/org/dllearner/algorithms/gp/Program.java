package org.dllearner.algorithms.gp;

import org.dllearner.learningproblems.ScorePosNeg;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * This class represents a program, i.e. an individual.
 * 
 * @author Jens Lehmann
 * 
 */
public class Program {

	// public static int fitnessEvaluations = 0;

	private OWLClassExpression hypothesis;

	// private Concept extendedHypothesis;

	private OWLClassExpression adc;

	private ScorePosNeg score;

	// private Score scoreAdc;
	
	// private LearningProblem learningProblem;

	private double fitness;
	
	/**
	 * Create a new program.
	 * 
	 */
	public Program(ScorePosNeg score, OWLClassExpression hypothesis) {
		this(score, hypothesis, null);
	}

	public Program(ScorePosNeg score, OWLClassExpression hypothesis, OWLClassExpression adc) {
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
	public OWLClassExpression getTree() {
		return hypothesis;
	}

	public ScorePosNeg getScore() {
		return score;
	}

	public OWLClassExpression getAdc() {
		return adc;
	}
}
