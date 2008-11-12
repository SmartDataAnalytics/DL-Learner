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
import java.util.SortedSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.Score;
import org.dllearner.core.configurators.ExampleBasedROLComponentConfigurator;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.CommonConfigMappings;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.DoubleConfigOption;
import org.dllearner.core.options.IntegerConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;

/**
 * The DL-Learner learning algorithm component for the example
 * based refinement operator approach. It handles all 
 * configuration options, creates the corresponding objects and
 * passes them to the actual refinement operator, heuristic, and
 * learning algorithm implementations.
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
	
	private ExampleBasedROLComponentConfigurator configurator;
	@Override
	public ExampleBasedROLComponentConfigurator getConfigurator(){
		return configurator;
	}
	
	// actual algorithm
	private ExampleBasedROLearner algorithm;
	private static Logger logger = Logger
		.getLogger(ExampleBasedROLearner.class);
	private String logLevel = CommonConfigOptions.logLevelDefault;	
		
	// configuration options
	private boolean writeSearchTree;
	private File searchTreeFile;
	private boolean replaceSearchTree = false;
	private static String defaultSearchTreeFile = "log/searchTree.txt";
	private String heuristic = "multi";
	Set<NamedClass> allowedConcepts;
	Set<ObjectProperty> allowedRoles;
	Set<NamedClass> ignoredConcepts;
	Set<ObjectProperty> ignoredRoles;
	// these are computed as the result of the previous four settings
	Set<NamedClass> usedConcepts;
	Set<ObjectProperty> usedRoles;	
	private boolean applyAllFilter = true;
	private boolean applyExistsFilter = true;	
	private boolean useTooWeakList = true;
	private boolean useOverlyGeneralList = true;
	private boolean useShortConceptConstruction = true;
	private boolean improveSubsumptionHierarchy = true;
	private boolean useAllConstructor = CommonConfigOptions.useAllConstructorDefault;
	private boolean useExistsConstructor = CommonConfigOptions.useExistsConstructorDefault;
	private boolean useHasValueConstructor = CommonConfigOptions.useHasValueConstructorDefault;
	private int valueFrequencyThreshold = CommonConfigOptions.valueFrequencyThresholdDefault;
	private boolean useCardinalityRestrictions = CommonConfigOptions.useCardinalityRestrictionsDefault;
	private boolean useNegation = CommonConfigOptions.useNegationDefault;
	private boolean useBooleanDatatypes = CommonConfigOptions.useBooleanDatatypesDefault;
	private boolean useDoubleDatatypes = CommonConfigOptions.useDoubleDatatypesDefault;
	private static double noisePercentageDefault = 0.0;
	private double noisePercentage = noisePercentageDefault;
	private NamedClass startClass = null;
	//refactor this
	private static boolean usePropernessChecksDefault = false;
	private boolean usePropernessChecks = usePropernessChecksDefault;
	//	refactor this
	private static int maxPosOnlyExpansionDefault = 4;
	private int maxPosOnlyExpansion = maxPosOnlyExpansionDefault;
	private boolean forceRefinementLengthIncrease = true;
	//extended Options
	//in seconds
	private int maxExecutionTimeInSeconds = CommonConfigOptions.maxExecutionTimeInSecondsDefault;
	private int minExecutionTimeInSeconds = CommonConfigOptions.minExecutionTimeInSecondsDefault;
	private int guaranteeXgoodDescriptions = CommonConfigOptions.guaranteeXgoodDescriptionsDefault;
	private int maxClassDescriptionTests = CommonConfigOptions.maxClassDescriptionTestsDefault;
	
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
	public ExampleBasedROLComponent(PosNegLP learningProblem, ReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
		this.configurator = new ExampleBasedROLComponentConfigurator(this);
	}
	
	public ExampleBasedROLComponent(PosOnlyDefinitionLP learningProblem, ReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
		this.configurator = new ExampleBasedROLComponentConfigurator(this);
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
		options.add(CommonConfigOptions.useHasValueConstructor());
		options.add(CommonConfigOptions.valueFreqencyThreshold());
		options.add(CommonConfigOptions.useCardinalityRestrictions());
		options.add(CommonConfigOptions.useNegation());
		options.add(CommonConfigOptions.useBooleanDatatypes());
		options.add(CommonConfigOptions.useDoubleDatatypes());
		options.add(CommonConfigOptions.maxExecutionTimeInSeconds());
		options.add(CommonConfigOptions.minExecutionTimeInSeconds());
		options.add(CommonConfigOptions.guaranteeXgoodDescriptions());
		options.add(CommonConfigOptions.maxClassDescriptionTests());
		options.add(CommonConfigOptions.getLogLevel());
		options.add(new BooleanConfigOption("usePropernessChecks", "specifies whether to check for equivalence (i.e. discard equivalent refinements)",usePropernessChecksDefault));
		options.add(new IntegerConfigOption("maxPosOnlyExpansion", "specifies how often a node in the search tree of a posonly learning problem needs to be expanded before it is" +
				" considered as solution candidate",maxPosOnlyExpansionDefault));
		DoubleConfigOption noisePercentage = new DoubleConfigOption("noisePercentage", "the (approximated) percentage of noise within the examples",noisePercentageDefault);
		noisePercentage.setLowerLimit(0);
		noisePercentage.setUpperLimit(100);
		options.add(noisePercentage);
		options.add(new StringConfigOption("startClass", "the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class)"));
		options.add(new BooleanConfigOption("forceRefinementLengthIncrease", "specifies whether nodes should be expanded until only longer refinements are reached"));
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
		} else if(name.equals("useHasValueConstructor")) {
			useHasValueConstructor = (Boolean) entry.getValue();
		} else if(name.equals("valueFrequencyThreshold")) {
			valueFrequencyThreshold = (Integer) entry.getValue();
		} else if(name.equals("useCardinalityRestrictions")) {
			useCardinalityRestrictions = (Boolean) entry.getValue();
		} else if(name.equals("useNegation")) {
			useNegation = (Boolean) entry.getValue();
		} else if(name.equals("noisePercentage")) {
			noisePercentage = (Double) entry.getValue();
		} else if(name.equals("useBooleanDatatypes")) {
			useBooleanDatatypes = (Boolean) entry.getValue();
		} else if(name.equals("useDoubleDatatypes")) {
			useDoubleDatatypes = (Boolean) entry.getValue();
		} else if(name.equals("usePropernessChecks")) {
			usePropernessChecks = (Boolean) entry.getValue();
		} else if(name.equals("maxPosOnlyExpansion")) {
			maxPosOnlyExpansion = (Integer) entry.getValue();
		} else if(name.equals("startClass")) {
			startClass = new NamedClass((String)entry.getValue());
		}else if(name.equals("maxExecutionTimeInSeconds")) {
			maxExecutionTimeInSeconds = (Integer) entry.getValue();
		}else if(name.equals("minExecutionTimeInSeconds")) {
			minExecutionTimeInSeconds = (Integer) entry.getValue();
		}else if(name.equals("guaranteeXgoodDescriptions")) {
			guaranteeXgoodDescriptions =  (Integer) entry.getValue();
		} else if(name.equals("maxClassDescriptionTests")) {
			maxClassDescriptionTests =  (Integer) entry.getValue();
		} else if(name.equals("logLevel")) {
			logLevel = ((String)entry.getValue()).toUpperCase();
		} else if(name.equals("forceRefinementLengthIncrease")) {
			forceRefinementLengthIncrease = (Boolean) entry.getValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		
		// exit with a ComponentInitException if the reasoner is unsupported for this learning algorithm
		if(reasoner.getReasonerType() == ReasonerType.DIG) {
			throw new ComponentInitException("DIG does not support the inferences needed in the selected learning algorithm component: " + getName());
		}
		
		// set log level if the option has been set
		if(!logLevel.equals(CommonConfigOptions.logLevelDefault))
			logger.setLevel(Level.toLevel(logLevel,Level.toLevel(CommonConfigOptions.logLevelDefault)));
		
		if(searchTreeFile == null)
			searchTreeFile = new File(defaultSearchTreeFile);

		if(writeSearchTree)
			Files.clearFile(searchTreeFile);

		// adjust heuristic
		ExampleBasedHeuristic algHeuristic;
		
		if(heuristic == "lexicographic")
			algHeuristic = new LexicographicHeuristic();
		else if(heuristic == "flexible") {
			if(learningProblem instanceof PosOnlyDefinitionLP) {
				throw new RuntimeException("does not work with positive examples only yet");
			}
			algHeuristic = new FlexibleHeuristic(((PosNegLP)learningProblem).getNegativeExamples().size(), ((PosNegLP)learningProblem).getPercentPerLengthUnit());
		} else {
			if(learningProblem instanceof PosOnlyLP) {
//				throw new RuntimeException("does not work with positive examples only yet");
				algHeuristic = new MultiHeuristic(((PosOnlyLP)learningProblem).getPositiveExamples().size(),0);
			} else {
				algHeuristic = new MultiHeuristic(((PosNegLP)learningProblem).getPositiveExamples().size(),((PosNegLP)learningProblem).getNegativeExamples().size());
			}
		}
		
		// compute used concepts/roles from allowed/ignored
		// concepts/roles
		if(allowedConcepts != null) {
			// sanity check to control if no non-existing concepts are in the list
			Helper.checkConcepts(reasoner, allowedConcepts);
			usedConcepts = allowedConcepts;
		} else if(ignoredConcepts != null) {
			usedConcepts = Helper.computeConceptsUsingIgnoreList(reasoner, ignoredConcepts);
		} else {
			usedConcepts = Helper.computeConcepts(reasoner);
		}
		
		if(allowedRoles != null) {
			Helper.checkRoles(reasoner, allowedRoles);
			usedRoles = allowedRoles;
		} else if(ignoredRoles != null) {
			Helper.checkRoles(reasoner, ignoredRoles);
			usedRoles = Helper.difference(reasoner.getObjectProperties(), ignoredRoles);
		} else {
			usedRoles = reasoner.getObjectProperties();
		}
		
		// prepare subsumption and role hierarchies, because they are needed
		// during the run of the algorithm;
		// in contrast to before, the learning algorithms have to maintain their
		// own view on the class hierarchy
		ClassHierarchy classHierarchy = reasoner.getClassHierarchy().cloneAndRestrict(usedConcepts);
		if(improveSubsumptionHierarchy)
			classHierarchy.thinOutSubsumptionHierarchy();
		
//		reasoner.prepareRoleHierarchy(usedRoles);
		// prepare datatype hierarchy only if necessary
//		if(reasoner.hasDatatypeSupport())
//			reasoner.prepareDatatypePropertyHierarchy();
		
		// create a refinement operator and pass all configuration
		// variables to it
		RhoDRDown operator = new RhoDRDown(
				reasoner,
				classHierarchy,
					applyAllFilter,
					applyExistsFilter,
					useAllConstructor, 
					useExistsConstructor,
					useHasValueConstructor,
					valueFrequencyThreshold,
					useCardinalityRestrictions,
					useNegation,
					useBooleanDatatypes,
					useDoubleDatatypes,
					startClass
			);		
			
		// create an algorithm object and pass all configuration
		// options to it
		algorithm = new ExampleBasedROLearner(
				learningProblem,
				reasoner,
				operator,
				algHeuristic,
				startClass,
				// usedConcepts,
				// usedRoles,
				noisePercentage/(double)100,
				writeSearchTree,
				replaceSearchTree,
				searchTreeFile,
				useTooWeakList,
				useOverlyGeneralList,
				useShortConceptConstruction,
				usePropernessChecks,
				maxPosOnlyExpansion,
				maxExecutionTimeInSeconds,
				minExecutionTimeInSeconds,
				guaranteeXgoodDescriptions,
				maxClassDescriptionTests,
				forceRefinementLengthIncrease
		);
		// note: used concepts and roles do not need to be passed
		// as argument, because it is sufficient to prepare the
		// concept and role hierarchy accordingly
	}
	
	public static String getName() {
		return "example driven refinement operator based learning algorithm";
	}
	
	public static String getUsage() {
		return "algorithm = refexamples;";
	}
	
	@Override
	public void start() {
		algorithm.start();
	}
	
//	@Override
	public Score getSolutionScore() {
		return algorithm.getSolutionScore();
	}
	
	@Override
	public Description getCurrentlyBestDescription() {
		return algorithm.getBestSolution();
	}
	
	@Override
	public synchronized List<Description> getCurrentlyBestDescriptions() {
		return algorithm.getCurrentlyBestDescriptions();
	}	
	
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return new EvaluatedDescription(algorithm.getBestSolution(),algorithm.getSolutionScore());
	}
	
	@Override
	public synchronized SortedSet<EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions() {
		return algorithm.getCurrentlyBestEvaluatedDescriptions();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		algorithm.stop();
	}

	public ExampleBasedNode getStartNode() {
		return algorithm.getStartNode();
	}

	/** {@inheritDoc} */
	@Override
	public void pause() {
		// TODO: not implemented
	}

	/** {@inheritDoc} */
	@Override
	public void resume() {
		// TODO: not implemented
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#isRunning()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isRunning() {
		return algorithm.isRunning();
	}
	
}
