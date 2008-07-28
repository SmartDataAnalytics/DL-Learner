/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.config.BooleanConfigOption;
import org.dllearner.core.config.CommonConfigOptions;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DoubleConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;

/**
 * The DBpedia Navigation suggestor takes a knowledge fragment extracted
 * from DBpedia, performs some preprocessing steps, invokes a learning
 * algorithm, and then performs postprocessing steps. It does not 
 * implement a completely new learning algorithm itself, but uses the
 * example based refinement operator learning algorithm.
 * 
 * @author Jens Lehmann
 *
 */
public class DBpediaNavigationSuggestor extends LearningAlgorithm {
	
//	private ReasoningService rs;
	private ExampleBasedROLComponent learner;
	private static String defaultSearchTreeFile = "log/searchTree.txt";
	
	public DBpediaNavigationSuggestor(LearningProblem learningProblem, ReasoningService rs) {
//		this.rs=rs;
		if(learningProblem instanceof PosNegLP) {
			PosNegLP lp = (PosNegLP) learningProblem;
			this.learner=new ExampleBasedROLComponent(lp, rs);
		} else if(learningProblem instanceof PosOnlyDefinitionLP) {
			PosOnlyDefinitionLP lp = (PosOnlyDefinitionLP) learningProblem;
			this.learner=new ExampleBasedROLComponent(lp, rs);
		}
	}
	
	public static Collection<Class<? extends LearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends LearningProblem>> problems = new LinkedList<Class<? extends LearningProblem>>();
		problems.add(LearningProblem.class);
		return problems;
	}
	
	public DBpediaNavigationSuggestor(PosOnlyDefinitionLP learningProblem, ReasoningService rs) {
		System.out.println("test1");
	}
	
	public DBpediaNavigationSuggestor(PosNegDefinitionLP learningProblem, ReasoningService rs) {
		System.out.println("test2");
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new BooleanConfigOption("writeSearchTree", "specifies whether to write a search tree", false));
		options.add(new StringConfigOption("searchTreeFile","file to use for the search tree", defaultSearchTreeFile));
		options.add(new BooleanConfigOption("replaceSearchTree","specifies whether to replace the search tree in the log file after each run or append the new search tree", false));
		StringConfigOption heuristicOption = new StringConfigOption("heuristic", "specifiy the heuristic to use", "lexicographic");
		heuristicOption.setAllowedValues(new String[] {"lexicographic", "flexible"});
		options.add(heuristicOption);
		options.add(new BooleanConfigOption("applyAllFilter", "usage of equivalence ALL R.C AND ALL R.D = ALL R.(C AND D)", true));
		options.add(new BooleanConfigOption("applyExistsFilter", "usage of equivalence EXISTS R.C OR EXISTS R.D = EXISTS R.(C OR D)", true));
		options.add(new BooleanConfigOption("useTooWeakList", "try to filter out too weak concepts without sending them to the reasoner", true));
		options.add(new BooleanConfigOption("useOverlyGeneralList", "try to find overly general concept without sending them to the reasoner", true));
		options.add(new BooleanConfigOption("useShortConceptConstruction", "shorten concept to see whether they already exist", true));
		DoubleConfigOption horizExp = new DoubleConfigOption("horizontalExpansionFactor", "horizontal expansion factor (see publication for description)", 0.6);
		horizExp.setLowerLimit(0.0);
		horizExp.setUpperLimit(1.0);
		options.add(horizExp);
		options.add(new BooleanConfigOption("improveSubsumptionHierarchy", "simplify subsumption hierarchy to reduce search space (see publication for description)", true));
		// allowed/ignored concepts/roles could also be a reasoner option (?)
		options.add(CommonConfigOptions.allowedConcepts());
		options.add(CommonConfigOptions.ignoredConcepts());
		options.add(CommonConfigOptions.allowedRoles());
		options.add(CommonConfigOptions.ignoredRoles());
		options.add(CommonConfigOptions.useAllConstructor());
		options.add(CommonConfigOptions.useExistsConstructor());
		options.add(CommonConfigOptions.useCardinalityRestrictions());
		options.add(CommonConfigOptions.useNegation());
		options.add(CommonConfigOptions.useBooleanDatatypes());
		options.add(CommonConfigOptions.maxExecutionTimeInSeconds());
		options.add(CommonConfigOptions.minExecutionTimeInSeconds());
		options.add(CommonConfigOptions.guaranteeXgoodDescriptions());
		options.add(CommonConfigOptions.getLogLevel());
		DoubleConfigOption noisePercentage = new DoubleConfigOption("noisePercentage", "the (approximated) percentage of noise within the examples");
		noisePercentage.setLowerLimit(0);
		noisePercentage.setUpperLimit(100);
		options.add(noisePercentage);
		options.add(new StringConfigOption("startClass", "the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class)"));
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		learner.applyConfigEntry(entry);
	}

	@Override
	public void init() throws ComponentInitException {
		learner.init();
	}
	
	@Override
	public void start() {
		learner.start();
	}

	@Override
	public void stop() {
		learner.stop();
	}	
	
	@Override
	public Description getCurrentlyBestDescription() {
		return learner.getCurrentlyBestDescription();
	}	
	
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return learner.getCurrentlyBestEvaluatedDescription();
	}

	@Override
	public Score getSolutionScore() {
		return learner.getSolutionScore();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#pause()
	 */
	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#resume()
	 */
	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#isRunning()
	 */
	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}
}
