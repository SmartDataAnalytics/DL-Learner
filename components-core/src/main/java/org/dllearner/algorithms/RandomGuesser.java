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

package org.dllearner.algorithms;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.gp.GPUtilities;
import org.dllearner.core.*;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.IntegerConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.owl.Description;

/**
 * This learning algorithm provides a random guessing technique to solve
 * learning problems in description logics/OWL. Solutions of such problems
 * are concepts, which can be viewed as trees. The algorithm takes as input
 * the number of guesses (how many concepts to generate) and the maximum depth
 * of the concepts/trees. Using this, it randomly creates trees by calling the
 * "grow" method of a genetic programming algorithm. The target language is
 * currently ALC.
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "Random Guesser", shortName = "randomGuesser", version = 0.8)
public class RandomGuesser extends AbstractCELA {

//	private RandomGuesserConfigurator configurator;
//	@Override
//	public RandomGuesserConfigurator getConfigurator(){
//		return configurator;
//	}
	
    private Description bestDefinition = null;
    private Score bestScore;
    private double bestFitness = Double.NEGATIVE_INFINITY;
    private boolean isRunning = false;

    private double lengthPenalty = 0.02;
	private int numberOfTrees = 100;
	private int maxDepth = 5;
    
	private static Logger logger = Logger.getLogger(RandomGuesser.class);
	
	public RandomGuesser(AbstractLearningProblem learningProblem, AbstractReasonerComponent rs) {
	   	super(learningProblem, rs);
//		this.configurator = new RandomGuesserConfigurator(this);
	}
	
	public static String getName() {
		return "random guesser learning algorithm";
	} 	
	
	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractLearningProblem>> problems = new LinkedList<Class<? extends AbstractLearningProblem>>();
		problems.add(AbstractLearningProblem.class);
		return problems;
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new IntegerConfigOption("numberOfGuesses", "number of randomly generated concepts/trees", 100));
		options.add(new IntegerConfigOption("maxDepth", "maximum depth of generated concepts/trees", 5));
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if (name.equals("numberOfGuesses")) {
			numberOfTrees = (Integer) entry.getValue();
		} else if(name.equals("maxDepth")) {
			maxDepth = (Integer) entry.getValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {

	}	
    	
	@Override
	public void start() {
		isRunning = true;
		
		Description d;
		
		for(int i=0; i<numberOfTrees; i++) {
//			p = GPUtilities.createGrowRandomProgram(learningProblem, reasoner, maxDepth, false);
			d = GPUtilities.createGrowRandomTree(learningProblem, reasoner, maxDepth, false);
			
			double acc = learningProblem.getAccuracy(d);
			double fitness = acc - lengthPenalty * d.getLength();
			
			if(fitness>bestFitness) {
				bestFitness = fitness;
				bestScore = learningProblem.computeScore(d);
				bestDefinition = d; // p.getTree();
			}
		}
		
		logger.info("Random-Guesser (" + numberOfTrees + " trials, maximum depth " + maxDepth + ")");
		logger.info("best solution: " + bestDefinition);
		logger.info("fitness: " + bestFitness);
		
		isRunning = false;
	}

//	@Override
	public Score getSolutionScore() {
		return bestScore;
	}

	@Override
	public Description getCurrentlyBestDescription() {
		return bestDefinition;
	}	
	
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return learningProblem.evaluate(bestDefinition);
//		return new EvaluatedDescriptionPosNeg(bestDefinition,bestScore);
	}

	@Override
	public void stop() {
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return isRunning;
	}

}
