package org.dllearner.algorithms;

import java.util.Collection;
import java.util.LinkedList;

import org.dllearner.LearningProblem;
import org.dllearner.Score;
import org.dllearner.algorithms.gp.Program;
import org.dllearner.algorithms.gp.GPUtilities;
import org.dllearner.core.ConfigEntry;
import org.dllearner.core.ConfigOption;
import org.dllearner.core.IntegerConfigOption;
import org.dllearner.core.InvalidConfigOptionValueException;
import org.dllearner.core.LearningAlgorithmNew;
import org.dllearner.core.LearningProblemNew;
import org.dllearner.core.dl.Concept;

public class RandomGuesser extends LearningAlgorithmNew implements LearningAlgorithm {

    private Concept bestDefinition = null;
    private Score bestScore;
    private double bestFitness = Double.NEGATIVE_INFINITY;
    private LearningProblem learningProblem;
	private int numberOfTrees;
	private int maxDepth;
    
	public RandomGuesser(LearningProblemNew lp) {
		
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new IntegerConfigOption("numberOfTrees"));
		options.add(new IntegerConfigOption("maxDepth"));
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if (name.equals("numberOfTrees"))
			numberOfTrees = (Integer) entry.getValue();
		else if(name.equals("maxDepth"))
			maxDepth = (Integer) entry.getValue();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}	
    
	/**
	 * Generiert zufaellig Loesungen.
	 * @param numberOfTrees Anzahl zu generierender Loesungen.
	 */
	public RandomGuesser(LearningProblem learningProblem, int numberOfTrees, int maxDepth) {
		this.learningProblem = learningProblem;
		this.numberOfTrees = numberOfTrees;
		this.maxDepth = maxDepth;
	}
	
	@Override
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

	@Override
	public Score getSolutionScore() {
		return bestScore;
	}

	@Override
	public Concept getBestSolution() {
		return bestDefinition;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}


}
