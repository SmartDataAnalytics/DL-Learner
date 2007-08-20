package org.dllearner.algorithms.gp;

import org.dllearner.Score;
import org.dllearner.dl.Concept;

/**
 * This class represents a program, i.e. an individual.
 * 
 * Um spaeter KAON2-Queries zu unterstuetzen, muesste man aus Program ein Interface
 * oder eine abstrakte Klasse machen. Diese Klasse wird zu OwnProgram o.ä. und
 * es wird eine zusaetzliche Klasse KAON2Program erstellt, die dann Queries an
 * KAON2 stellt und im Konstruktor eine KAON2-DL entgegennimmt. Programmiert
 * wird im GP-Algorithmus dann natuerlich nur gegen das Interface. Schwierig wird
 * lediglich, dass KAON2 eine andere Struktur hat, also ev. die Utility-Methoden
 * angepasst werden muessen. :-/
 * 
 * Neu: Das Ganze wurde jetzt ueber einen abstrahierten Reasoner und die Klasse
 * LearningProblem erledigt. TODO: Eventuell ist etwas wie "ProblemSolution" ein
 * besserer Name. Bei Program scheint unklar, warum das LearningProblem hier eine
 * Rolle spielt.
 * 
 * @author Jens Lehmann
 * 
 */
public class Program {

	// private static int fitnessEvaluations = 0;

	private Concept hypothesis;

	// private Concept extendedHypothesis;

	private Concept adc;

	private Score score;

	// private Score scoreAdc;
	
	// private LearningProblem learningProblem;

	private double fitness;

	/**
	 * Create a new program.
	 * 
	 * @param concept
	 *            The program tree.
	 */
	public Program(Score score, Concept hypothesis) {
		this(score, hypothesis, null);
	}

	public Program(Score score, Concept hypothesis, Concept adc) {
		// this.learningProblem = learningProblem;
		this.score = score;
		this.hypothesis = hypothesis;
		this.adc = adc;
		// TODO: es sind Prozent pro Längeneinheit, also ist hier die
		// Implementierung falsch !!
		// fitness = score.getScore() - hypothesis.getLength() * Config.percentPerLengthUnit;
		// => in getScore() ist jetzt schon der length penalty integriert
		fitness = score.getScore();

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
	public Concept getTree() {
		return hypothesis;
	}

	public Score getScore() {
		return score;
	}

	public Concept getAdc() {
		return adc;
	}
}
