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
package org.dllearner.algorithms;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.gp.Program;
import org.dllearner.algorithms.gp.GPUtilities;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.owl.Description;

public class RandomGuesser extends LearningAlgorithm {

    private Description bestDefinition = null;
    private Score bestScore;
    private double bestFitness = Double.NEGATIVE_INFINITY;
    private LearningProblem learningProblem;
	private int numberOfTrees;
	private int maxDepth;
    
	private static Logger logger = Logger.getLogger(RandomGuesser.class);		
	
	public RandomGuesser(LearningProblem learningProblem, ReasoningService rs) {
		this.learningProblem = learningProblem;
	}
	
	public static String getName() {
		return "random guesser learning algorithm";
	} 	
	
	public static Collection<Class<? extends LearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends LearningProblem>> problems = new LinkedList<Class<? extends LearningProblem>>();
		problems.add(LearningProblem.class);
		return problems;
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new IntegerConfigOption("numberOfTrees", "number of randomly generated concepts/trees"));
		options.add(new IntegerConfigOption("maxDepth", "maximum depth of generated concepts/trees"));
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
    	
	@Override
	public void start() {
		// this.learningProblem = learningProblem;
		
		// indem man die Klasse GP.Program verwendet, kann man auch
		// alle Features z.B. ADC, Type-Guessing verwenden
		Program p;
		
		for(int i=0; i<numberOfTrees; i++) {
			// p = GPUtilities.createGrowRandomProgram(learningProblem, maxDepth);
			p = GPUtilities.createGrowRandomProgram(learningProblem, maxDepth, false);
			if(p.getFitness()>bestFitness) {
				bestFitness = p.getFitness();
				bestScore = p.getScore();
				bestDefinition = p.getTree();
			}
		}
		
		logger.info("Random-Guesser (" + numberOfTrees + " trials, maximum depth " + maxDepth + ")");
		logger.info("best solution: " + bestDefinition);
		logger.info("fitness: " + bestFitness);
	}

	@Override
	public Score getSolutionScore() {
		return bestScore;
	}

	@Override
	public Description getBestSolution() {
		return bestDefinition;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
