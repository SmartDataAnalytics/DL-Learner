/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.ocel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.learningproblems.*;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.refinementoperators.*;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

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
@ComponentAnn(name = "OWL Class Expression Learner", shortName = "ocel", version = 1.2)
public class OCEL extends AbstractCELA {
	
//	private OCELConfigurator configurator;
//
//	public OCELConfigurator getConfigurator(){
//		return configurator;
//	}
	
	// actual algorithm
	private ROLearner2 algorithm;
	private static Logger logger = Logger.getLogger(OCEL.class);
	private String logLevel = CommonConfigOptions.logLevelDefault;
	
	// dependencies
	private LengthLimitedRefinementOperator operator;
	private ExampleBasedHeuristic heuristic;
	
	// configuration options
	private boolean writeSearchTree;
	private File searchTreeFile;
	private boolean replaceSearchTree = false;
	private static String defaultSearchTreeFile = "log/searchTree.txt";
//	private String heuristicStr = "multi";
	
//	private boolean applyAllFilter = true;
//	private boolean applyExistsFilter = true;
	private boolean useTooWeakList = true;
	private boolean useOverlyGeneralList = true;
	private boolean useShortConceptConstruction = true;
	private boolean improveSubsumptionHierarchy = true;
//	private boolean useAllConstructor = CommonConfigOptions.useAllConstructorDefault;
//	private boolean useExistsConstructor = CommonConfigOptions.useExistsConstructorDefault;
//	private boolean useHasValueConstructor = CommonConfigOptions.useHasValueConstructorDefault;
//	private int valueFrequencyThreshold = CommonConfigOptions.valueFrequencyThresholdDefault;
//	private boolean useCardinalityRestrictions = CommonConfigOptions.useCardinalityRestrictionsDefault;
//	private boolean useNegation = CommonConfigOptions.useNegationDefault;
//	private boolean useBooleanDatatypes = CommonConfigOptions.useBooleanDatatypesDefault;
	private static double noisePercentageDefault = 0.0;
	private double noisePercentage = noisePercentageDefault;
	private OWLClass startClass = null;
	private boolean useDataHasValueConstructor = false;
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
	
	private double negativeWeight = 1.0;
	private double startNodeBonus = 0.1; // 1.0;
	private double expansionPenaltyFactor = 0.02;
	private int negationPenalty = 0;
	private boolean terminateOnNoiseReached = true;
	
	// Variablen zur Einstellung der Protokollierung
	// boolean quiet = false;
	boolean showBenchmarkInformation = false;

	@ConfigOption(description = "adjust the weights of class expression length in refinement", defaultValue = "OCEL default metric")
	private OWLClassExpressionLengthMetric lengthMetric;
	// boolean createTreeString = false;
	// String searchTree = new String();
//	private int cardinalityLimit = 5;
//	private boolean useStringDatatypes = false;
//	private boolean instanceBasedDisjoints = true;

	// Konfiguration des Algorithmus
	// Faktor für horizontale Erweiterung (notwendig für completeness)
	// double horizontalExpansionFactor = 0.6;


    public OCEL(){}
    
	// soll später einen Operator und eine Heuristik entgegennehmen
	// public ROLearner(LearningProblem learningProblem, LearningProblem learningProblem2) {
	public OCEL(PosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}
	
	public OCEL(PosOnlyLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}
	
	public static Collection<Class<? extends AbstractClassExpressionLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractClassExpressionLearningProblem>> problems = new LinkedList<>();
		problems.add(PosNegLP.class);
		problems.add(PosOnlyLP.class);
		return problems;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		
		// exit with a ComponentInitException if the reasoner is unsupported for this learning algorithm
		if(getReasoner().getReasonerType() == ReasonerType.DIG) {
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

		if(heuristic == null) {
			if(getLearningProblem() instanceof PosOnlyLP) {
				throw new RuntimeException("does not work with positive examples only yet");
//				heuristic = new MultiHeuristic(((PosOnlyLP) getLearningProblem()).getPositiveExamples().size(),0, negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
			} else {
				heuristic = new MultiHeuristic(((PosNegLP) getLearningProblem()).getPositiveExamples().size(),((PosNegLP) getLearningProblem()).getNegativeExamples().size(), negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
			}
			
			// OLD CODE below: in the new framework we assume that the
			// heuristic is always injected as object (not as string)
//			if(heuristicStr == "lexicographic")
//				heuristic = new LexicographicHeuristic();
//			else if(heuristicStr == "flexible") {
//				if(learningProblem instanceof PosOnlyLP) {
//					throw new RuntimeException("does not work with positive examples only yet");
//				}
//				heuristic = new FlexibleHeuristic(((PosNegLP) getLearningProblem()).getNegativeExamples().size(), ((PosNegLP) getLearningProblem()).getPercentPerLengthUnit());
//			} else {
//				if(getLearningProblem() instanceof PosOnlyLP) {
//					throw new RuntimeException("does not work with positive examples only yet");
//	//				heuristic = new MultiHeuristic(((PosOnlyLP) getLearningProblem()).getPositiveExamples().size(),0, negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
//				} else {
//					heuristic = new MultiHeuristic(((PosNegLP) getLearningProblem()).getPositiveExamples().size(),((PosNegLP) getLearningProblem()).getNegativeExamples().size(), negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
//				}
//			}
		} else {
			// we need to set some variables to make the heuristic work
			if(heuristic instanceof MultiHeuristic) {
				MultiHeuristic mh = ((MultiHeuristic)heuristic);
				if(mh.getNrOfNegativeExamples() == 0) {
					mh.setNrOfNegativeExamples(((PosNegLP) getLearningProblem()).getNegativeExamples().size());
				}
				int nrPosEx = ((PosNegLP) getLearningProblem()).getPositiveExamples().size();
				int nrNegEx = ((PosNegLP) getLearningProblem()).getNegativeExamples().size();
				if(mh.getNrOfExamples() == 0) {
					mh.setNrOfExamples(nrPosEx + nrNegEx);
				}
				if(mh.getNrOfNegativeExamples() == 0) {
					mh.setNrOfNegativeExamples(nrNegEx);
				}
			} else if (heuristic instanceof  FlexibleHeuristic) {
				FlexibleHeuristic h2 = (FlexibleHeuristic) heuristic;
				if(h2.getNrOfNegativeExamples() == 0) {
					h2.setNrOfNegativeExamples(((PosNegLP) getLearningProblem()).getNegativeExamples().size());
				}
			}

		}
		
		// warn the user if he/she sets any non-standard heuristic, because it will just be ignored
		if(learningProblem instanceof PosNegLPStandard) {
		}
		
		// compute used concepts/roles from allowed/ignored
		// concepts/roles
		
		// prepare subsumption and role hierarchies, because they are needed
		// during the run of the algorithm;
		// in contrast to before, the learning algorithms have to maintain their
		// own view on the class hierarchy
		ClassHierarchy classHierarchy = initClassHierarchy();
		ObjectPropertyHierarchy objectPropertyHierarchy = initObjectPropertyHierarchy();
		DatatypePropertyHierarchy datatypePropertyHierarchy = initDataPropertyHierarchy();
		if(improveSubsumptionHierarchy) {
			classHierarchy.thinOutSubsumptionHierarchy();
			objectPropertyHierarchy.thinOutSubsumptionHierarchy();
			datatypePropertyHierarchy.thinOutSubsumptionHierarchy();
		}
		
		
//		reasoner.prepareRoleHierarchy(usedRoles);
		// prepare datatype hierarchy only if necessary
//		if(reasoner.hasDatatypeSupport())
//			reasoner.prepareDatatypePropertyHierarchy();
		
		// create a refinement operator and pass all configuration
		// variables to it
		if(operator == null) {
			// we use a default operator and inject the class hierarchy for now
			operator = new RhoDRDown();
			if(operator instanceof CustomStartRefinementOperator) {
				((CustomStartRefinementOperator)operator).setStartClass(startClass);
			}
			if(operator instanceof ReasoningBasedRefinementOperator) {
				((ReasoningBasedRefinementOperator)operator).setReasoner(reasoner);
			}
			operator.init();
		}
		// TODO: find a better solution as this is quite difficult to debug
		if(operator instanceof CustomHierarchyRefinementOperator) {
			((CustomHierarchyRefinementOperator)operator).setClassHierarchy(classHierarchy);
			((CustomHierarchyRefinementOperator)operator).setObjectPropertyHierarchy(objectPropertyHierarchy);
			((CustomHierarchyRefinementOperator)operator).setDataPropertyHierarchy(datatypePropertyHierarchy);
		}

		if (lengthMetric == null) {
			lengthMetric = OWLClassExpressionLengthMetric.getOCELMetric();
		}
		if(operator instanceof LengthLimitedRefinementOperator) {
			((LengthLimitedRefinementOperator)operator).setLengthMetric(lengthMetric);
		}

		// create an algorithm object and pass all configuration
		// options to it
		algorithm = new ROLearner2(
//				configurator,
				learningProblem,
				reasoner,
				operator,
				heuristic,
				startClass,
				// usedConcepts,
				// usedRoles,
				noisePercentage/100,
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
				forceRefinementLengthIncrease,
				terminateOnNoiseReached,
				negativeWeight,
				startNodeBonus,
				expansionPenaltyFactor,
				negationPenalty,
				lengthMetric,
				renderer
		);
		// note: used concepts and roles do not need to be passed
		// as argument, because it is sufficient to prepare the
		// concept and role hierarchy accordingly
	}
	
	public static String getName() {
//		return "refinement operator based learning algorithm II";
		return "OCEL";
	}
	
	public static String getUsage() {
		return "algorithm = refexamples;";
	}
	
	@Override
	public void start() {
		algorithm.start();
	}
	
//	@Override
	public ScorePosNeg getSolutionScore() {
		return algorithm.getSolutionScore();
	}
	
	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		return algorithm.getBestSolution();
	}
	
	@Override
	public synchronized List<OWLClassExpression> getCurrentlyBestDescriptions() {
		return algorithm.getCurrentlyBestDescriptions();
	}
	
	@Override
	public EvaluatedDescriptionPosNeg getCurrentlyBestEvaluatedDescription() {
		return new EvaluatedDescriptionPosNeg(algorithm.getBestSolution(),algorithm.getSolutionScore());
	}
	
	@Override
	public synchronized TreeSet<EvaluatedDescriptionPosNeg> getCurrentlyBestEvaluatedDescriptions() {
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

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#isRunning()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isRunning() {
		return algorithm.isRunning();
	}
	
	public LengthLimitedRefinementOperator getRefinementOperator() {
		return operator;
	}

	public LengthLimitedRefinementOperator getOperator() {
		return operator;
	}

    @Autowired(required=false)
	public void setOperator(LengthLimitedRefinementOperator operator) {
		this.operator = operator;
	}

	public boolean isWriteSearchTree() {
		return writeSearchTree;
	}

	public void setWriteSearchTree(boolean writeSearchTree) {
		this.writeSearchTree = writeSearchTree;
	}

	public File getSearchTreeFile() {
		return searchTreeFile;
	}

	public void setSearchTreeFile(File searchTreeFile) {
		this.searchTreeFile = searchTreeFile;
	}

	public boolean isReplaceSearchTree() {
		return replaceSearchTree;
	}

	public void setReplaceSearchTree(boolean replaceSearchTree) {
		this.replaceSearchTree = replaceSearchTree;
	}

	public boolean isUseTooWeakList() {
		return useTooWeakList;
	}

	public void setUseTooWeakList(boolean useTooWeakList) {
		this.useTooWeakList = useTooWeakList;
	}

	public boolean isUseOverlyGeneralList() {
		return useOverlyGeneralList;
	}

	public void setUseOverlyGeneralList(boolean useOverlyGeneralList) {
		this.useOverlyGeneralList = useOverlyGeneralList;
	}

	public boolean isUseShortConceptConstruction() {
		return useShortConceptConstruction;
	}

	public void setUseShortConceptConstruction(boolean useShortConceptConstruction) {
		this.useShortConceptConstruction = useShortConceptConstruction;
	}

	public boolean isImproveSubsumptionHierarchy() {
		return improveSubsumptionHierarchy;
	}

	public void setImproveSubsumptionHierarchy(boolean improveSubsumptionHierarchy) {
		this.improveSubsumptionHierarchy = improveSubsumptionHierarchy;
	}

	public double getNoisePercentage() {
		return noisePercentage;
	}

	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}

	public OWLClass getStartClass() {
		return startClass;
	}

	public void setStartClass(OWLClass startClass) {
		this.startClass = startClass;
	}

	public boolean isUsePropernessChecks() {
		return usePropernessChecks;
	}

	public void setUsePropernessChecks(boolean usePropernessChecks) {
		this.usePropernessChecks = usePropernessChecks;
	}

	public int getMaxPosOnlyExpansion() {
		return maxPosOnlyExpansion;
	}

	public void setMaxPosOnlyExpansion(int maxPosOnlyExpansion) {
		this.maxPosOnlyExpansion = maxPosOnlyExpansion;
	}

	public boolean isForceRefinementLengthIncrease() {
		return forceRefinementLengthIncrease;
	}

	public void setForceRefinementLengthIncrease(boolean forceRefinementLengthIncrease) {
		this.forceRefinementLengthIncrease = forceRefinementLengthIncrease;
	}

	@Override
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	@Override
	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public int getMinExecutionTimeInSeconds() {
		return minExecutionTimeInSeconds;
	}

	public void setMinExecutionTimeInSeconds(int minExecutionTimeInSeconds) {
		this.minExecutionTimeInSeconds = minExecutionTimeInSeconds;
	}

	public int getGuaranteeXgoodDescriptions() {
		return guaranteeXgoodDescriptions;
	}

	public void setGuaranteeXgoodDescriptions(int guaranteeXgoodDescriptions) {
		this.guaranteeXgoodDescriptions = guaranteeXgoodDescriptions;
	}

	public int getMaxClassDescriptionTests() {
		return maxClassDescriptionTests;
	}

	public void setMaxClassDescriptionTests(int maxClassDescriptionTests) {
		this.maxClassDescriptionTests = maxClassDescriptionTests;
	}

	public boolean isShowBenchmarkInformation() {
		return showBenchmarkInformation;
	}

	public void setShowBenchmarkInformation(boolean showBenchmarkInformation) {
		this.showBenchmarkInformation = showBenchmarkInformation;
	}

	public double getNegativeWeight() {
		return negativeWeight;
	}

	public void setNegativeWeight(double negativeWeight) {
		this.negativeWeight = negativeWeight;
	}

	public double getStartNodeBonus() {
		return startNodeBonus;
	}

	public void setStartNodeBonus(double startNodeBonus) {
		this.startNodeBonus = startNodeBonus;
	}

	public double getExpansionPenaltyFactor() {
		return expansionPenaltyFactor;
	}

	public void setExpansionPenaltyFactor(double expansionPenaltyFactor) {
		this.expansionPenaltyFactor = expansionPenaltyFactor;
	}

	public double getNegationPenalty() {
		return negationPenalty;
	}

	public void setNegationPenalty(int negationPenalty) {
		this.negationPenalty = negationPenalty;
	}

	public boolean isUseDataHasValueConstructor() {
		return useDataHasValueConstructor;
	}

	public void setUseDataHasValueConstructor(boolean useDataHasValueConstructor) {
		this.useDataHasValueConstructor = useDataHasValueConstructor;
	}

	public boolean isTerminateOnNoiseReached() {
		return terminateOnNoiseReached;
	}

	public void setTerminateOnNoiseReached(boolean terminateOnNoiseReached) {
		this.terminateOnNoiseReached = terminateOnNoiseReached;
	}

	public OWLClassExpressionLengthMetric getLengthMetric() {
		return lengthMetric;
	}

	@Autowired(required = false)
	public void setLengthMetric(OWLClassExpressionLengthMetric lengthMetric) {
		this.lengthMetric = lengthMetric;
		if (operator != null && operator instanceof LengthLimitedRefinementOperator) {
			((LengthLimitedRefinementOperator)operator).setLengthMetric(lengthMetric);
		}
	}

    @Autowired(required=false)
	public void setHeuristic(ExampleBasedHeuristic heuristic) {
		this.heuristic = heuristic;
	}

    public ExampleBasedHeuristic getHeuristic() {
        return heuristic;
    }
}
