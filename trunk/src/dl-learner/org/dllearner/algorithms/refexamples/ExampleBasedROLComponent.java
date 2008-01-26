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

package org.dllearner.algorithms.refexamples;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithms.refinement.RhoDown;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.config.BooleanConfigOption;
import org.dllearner.core.config.CommonConfigMappings;
import org.dllearner.core.config.CommonConfigOptions;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DoubleConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Concept;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;

/**
 * The DL-Learner learning algorithm component for the example
 * based refinement operator approach. It handles all 
 * configuration options, creates the corresponding objects and
 * passes them to the actual refinement operator, heuristic, and
 * learning algorithm implementations.
 * 
 * Note: The component is not working yet.
 * 
 * Note: The options supported by the ROLearner component and this
 * one are not equal. Options that have been dropped for now:
 * - horizontal expansion factor: The goal of the algorithm will
 *     be to (hopefully) be able to learn long and complex concepts
 *     more efficiently.
 *     A horizontal expansion factor has its benefits, but limits
 *     the length of concepts learnable in reasonable time to
 *     about 15 with its default value of 0.6 and a small sized
 *     background knowledge base. We hope to get more fine-grained
 *     control of whether it makes sense to extend a node with
 *     more sophisticated heuristics.
 *     Dropping the horizontal expansion factor means that the
 *     completeness of the algorithm depends on the heuristic.
 * 
 * @author Jens Lehmann
 *
 */
public class ExampleBasedROLComponent extends LearningAlgorithm {
	
	// actual algorithm
	private ExampleBasedROLearner algorithm;
	
	// learning problem to solve and background knowledge
	private ReasoningService rs;
	private LearningProblem learningProblem;	
	
	// configuration options
	private boolean writeSearchTree;
	private File searchTreeFile;
	private boolean replaceSearchTree = false;
	private static String defaultSearchTreeFile = "log/searchTree.txt";
	private String heuristic = "lexicographic";
	Set<AtomicConcept> allowedConcepts;
	Set<AtomicRole> allowedRoles;
	Set<AtomicConcept> ignoredConcepts;
	Set<AtomicRole> ignoredRoles;
	// these are computed as the result of the previous four settings
	Set<AtomicConcept> usedConcepts;
	Set<AtomicRole> usedRoles;	
	private boolean applyAllFilter = true;
	private boolean applyExistsFilter = true;	
	private boolean useTooWeakList = true;
	private boolean useOverlyGeneralList = true;
	private boolean useShortConceptConstruction = true;
	private boolean improveSubsumptionHierarchy = true;
	private boolean useAllConstructor = true;
	private boolean useExistsConstructor = true;
	private boolean useNegation = true;	
	private double noisePercentage = 0.0;
	
	// Variablen zur Einstellung der Protokollierung
	// boolean quiet = false;
	boolean showBenchmarkInformation = false;
	// boolean createTreeString = false;
	// String searchTree = new String();

	// Konfiguration des Algorithmus
	// Faktor für horizontale Erweiterung (notwendig für completeness)
	// double horizontalExpansionFactor = 0.6;	

	// soll später einen Operator und eine Heuristik entgegennehmen
	// public ROLearner(LearningProblem learningProblem, LearningProblem learningProblem2) {
	public ExampleBasedROLComponent(PosNegLP learningProblem, ReasoningService rs) {
		this.learningProblem = learningProblem;
		this.rs = rs;
	}
	
	public ExampleBasedROLComponent(PosOnlyDefinitionLP learningProblem, ReasoningService rs) {
		this.learningProblem = learningProblem;
		this.rs = rs;
	}
	
	public static Collection<Class<? extends LearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends LearningProblem>> problems = new LinkedList<Class<? extends LearningProblem>>();
		problems.add(PosNegLP.class);
		problems.add(PosOnlyDefinitionLP.class);
		return problems;
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
		options.add(CommonConfigOptions.useNegation());
		DoubleConfigOption noisePercentage = new DoubleConfigOption("noisePercentage", "the (approximated) percentage of noise within the examples");
		noisePercentage.setLowerLimit(0.0);
		noisePercentage.setUpperLimit(1.0);
		options.add(noisePercentage);
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	@SuppressWarnings({"unchecked"})
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if(name.equals("writeSearchTree"))
			writeSearchTree = (Boolean) entry.getValue();
		else if(name.equals("searchTreeFile"))
			searchTreeFile = new File((String)entry.getValue());
		else if(name.equals("replaceSearchTree"))
			replaceSearchTree = (Boolean) entry.getValue();
		else if(name.equals("heuristic")) {
			String value = (String) entry.getValue();
			if(value.equals("lexicographic"))
				heuristic = "lexicographic";
			else
				heuristic = "flexible";
		} else if(name.equals("allowedConcepts")) {
			allowedConcepts = CommonConfigMappings.getAtomicConceptSet((Set<String>)entry.getValue());
		} else if(name.equals("allowedRoles")) {
			allowedRoles = CommonConfigMappings.getAtomicRoleSet((Set<String>)entry.getValue());
		} else if(name.equals("ignoredConcepts")) {
			ignoredConcepts = CommonConfigMappings.getAtomicConceptSet((Set<String>)entry.getValue());
		} else if(name.equals("ignoredRoles")) {
			ignoredRoles = CommonConfigMappings.getAtomicRoleSet((Set<String>)entry.getValue());
		} else if(name.equals("applyAllFilter")) {
			applyAllFilter = (Boolean) entry.getValue();
		} else if(name.equals("applyExistsFilter")) {
			applyExistsFilter = (Boolean) entry.getValue();
		} else if(name.equals("useTooWeakList")) {
			useTooWeakList = (Boolean) entry.getValue();
		} else if(name.equals("useOverlyGeneralList")) {
			useOverlyGeneralList = (Boolean) entry.getValue();
		} else if(name.equals("useShortConceptConstruction")) {
			useShortConceptConstruction = (Boolean) entry.getValue();
		} else if(name.equals("improveSubsumptionHierarchy")) {
			improveSubsumptionHierarchy = (Boolean) entry.getValue();
		} else if(name.equals("useAllConstructor")) {
			useAllConstructor = (Boolean) entry.getValue();
		} else if(name.equals("useExistsConstructor")) {
			useExistsConstructor = (Boolean) entry.getValue();
		} else if(name.equals("useNegation")) {
			useNegation = (Boolean) entry.getValue();
		} else if(name.equals("noisePercentage")) {
			noisePercentage = (Double) entry.getValue();
		}
			
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		if(searchTreeFile == null)
			searchTreeFile = new File(defaultSearchTreeFile);

		if(writeSearchTree)
			Files.clearFile(searchTreeFile);
		
		// adjust heuristic
		ExampleBasedHeuristic algHeuristic;
		
		if(heuristic == "lexicographic")
			algHeuristic = new LexicographicHeuristic();
		else {
			if(learningProblem instanceof PosOnlyDefinitionLP) {
				throw new RuntimeException("does not work with positive examples only yet");
			}
			algHeuristic = null;
			// algHeuristic = new FlexibleHeuristic(learningProblem.getNegativeExamples().size(), learningProblem.getPercentPerLengthUnit());
		}
		
		// compute used concepts/roles from allowed/ignored
		// concepts/roles
		if(allowedConcepts != null) {
			// sanity check to control if no non-existing concepts are in the list
			Helper.checkConcepts(rs, allowedConcepts);
			usedConcepts = allowedConcepts;
		} else if(ignoredConcepts != null) {
			usedConcepts = Helper.computeConceptsUsingIgnoreList(rs, ignoredConcepts);
		} else {
			usedConcepts = Helper.computeConcepts(rs);
		}
		
		if(allowedRoles != null) {
			Helper.checkRoles(rs, allowedRoles);
			usedRoles = allowedRoles;
		} else if(ignoredRoles != null) {
			Helper.checkRoles(rs, ignoredRoles);
			usedRoles = Helper.difference(rs.getAtomicRoles(), ignoredRoles);
		} else {
			usedRoles = rs.getAtomicRoles();
		}
		
		// prepare subsumption and role hierarchies, because they are needed
		// during the run of the algorithm
		rs.prepareSubsumptionHierarchy(usedConcepts);
		if(improveSubsumptionHierarchy)
			rs.getSubsumptionHierarchy().improveSubsumptionHierarchy();
		rs.prepareRoleHierarchy(usedRoles);
		
		// create a refinement operator and pass all configuration
		// variables to it
		RhoDown operator = new RhoDown(
				rs,
				applyAllFilter,
				applyExistsFilter,
				useAllConstructor,
				useExistsConstructor,
				useNegation
		);
		
		// create an algorithm object and pass all configuration
		// options to it
		algorithm = new ExampleBasedROLearner(
				learningProblem,
				rs,
				operator,
				algHeuristic,
				// usedConcepts,
				// usedRoles,
				noisePercentage,
				writeSearchTree,
				replaceSearchTree,
				searchTreeFile,
				useTooWeakList,
				useOverlyGeneralList,
				useShortConceptConstruction
		);		
		// note: used concepts and roles do not need to be passed
		// as argument, because it is sufficient to prepare the
		// concept and role hierarchy accordingly
	}
	
	public static String getName() {
		return "example driven refinement operator based learning algorithm";
	}
	
	@Override
	public void start() {
		algorithm.start();
	}
	
	@Override
	public Score getSolutionScore() {
		return algorithm.getSolutionScore();
	}
	
	@Override
	public Concept getBestSolution() {
		return algorithm.getBestSolution();
	}
	
	@Override
	public synchronized List<Concept> getBestSolutions(int nrOfSolutions) {
		return algorithm.getBestSolutions(nrOfSolutions);
	}	

	@Override
	public void stop() {
		algorithm.stop();
	}

}
