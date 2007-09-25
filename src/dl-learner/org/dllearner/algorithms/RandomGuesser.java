package org.dllearner.algorithms;

import org.dllearner.LearningProblem;
import org.dllearner.Score;
import org.dllearner.algorithms.gp.Program;
import org.dllearner.algorithms.gp.GPUtilities;
import org.dllearner.core.dl.Concept;

public class RandomGuesser implements LearningAlgorithm {

    private Concept bestDefinition = null;
    private Score bestScore;
    private double bestFitness = Double.NEGATIVE_INFINITY;
    private LearningProblem learningProblem;
	private int numberOfTrees;
	private int maxDepth;
    
    
	/**
	 * Generiert zufaellig Loesungen.
	 * @param numberOfTrees Anzahl zu generierender Loesungen.
	 */
	public RandomGuesser(LearningProblem learningProblem, int numberOfTrees, int maxDepth) {
		this.learningProblem = learningProblem;
		this.numberOfTrees = numberOfTrees;
		this.maxDepth = maxDepth;
	}
	
	public void start() {
		// this.learningProblem = learningProblem;
		
		// indem man die Klasse GP.Program verwendet, kann man auch
		// alle Features z.B. ADC, Type-Guessing verwenden
		Program p;
		
		for(int i=0; i<numberOfTrees; i++) {
			p = GPUtilities.createGrowRandomProgram(learningProblem, maxDepth);
			if(p.getFitness()>bestFitness) {
				bestFitness = p.getFitness();
				bestScore = p.getScore();
				bestDefinition = p.getTree();
			}
		}
		
		System.out.print("Random-Guesser (" + numberOfTrees + " trials, ");
		System.out.println("maximum depth " + maxDepth + ")");
		System.out.println("best solution: " + bestDefinition);
		System.out.println("fitness: " + bestFitness);
		
		// System.out.println(bestScore);
	}

	public Score getSolutionScore() {
		return bestScore;
	}

	public Concept getBestSolution() {
		return bestDefinition;
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}
}
