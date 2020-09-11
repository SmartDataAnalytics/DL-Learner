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

import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import org.apache.log4j.Level;
import org.dllearner.accuracymethods.AccMethodNoWeakness;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.refinementoperators.*;
import org.dllearner.utilities.*;
import org.dllearner.utilities.datastructures.SearchTreeNonWeak;
import org.dllearner.utilities.datastructures.SearchTreeNonWeakPartialSet;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.EvaluatedDescriptionPosNegComparator;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

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
@ComponentAnn(name = "OWL Class Expression Learner with Simulated Annealing", shortName = "ocelsa", version = 0.1)
public class OCELSA extends OCEL {

	// actual algorithm
	private static Logger logger = LoggerFactory.getLogger(OCELSA.class);
	private String logLevel = CommonConfigOptions.logLevelDefault;

	// often the learning problems needn't be accessed directly; instead
	// use the example sets below and the posonly variable
	private OWLClassExpression startDescription;
	private int nrOfExamples;
	private int nrOfPositiveExamples;
	private Set<OWLIndividual> positiveExamples;
	private int nrOfNegativeExamples;
	private Set<OWLIndividual> negativeExamples;

	private int allowedMisclassifications = 0;

	// search tree options
	@ConfigOption(defaultValue = "false", description = "specifies whether to write a search tree")
	private boolean writeSearchTree;
	@ConfigOption(defaultValue="log/searchTree.txt", description="file to use for the search tree")
	private File searchTreeFile;
	@ConfigOption(defaultValue="false", description="specifies whether to replace the search tree in the log file after each run or append the new search tree")
	private boolean replaceSearchTree = false;

	// constructs to improve performance
	@ConfigOption(defaultValue = "true", description = "exclude too weak concepts when they occur as sub concept")
	private boolean useTooWeakList = true;
	@ConfigOption(defaultValue = "true")
	private boolean useOverlyGeneralList = true;
	@ConfigOption(defaultValue = "true", description = "whether to shorten concepts to ignore identical refinement. " +
			"e.g. male AND male is shortened to male. ")
	private boolean useShortConceptConstruction = true;

	// extended Options
	private boolean maxExecutionTimeAlreadyReached = false;
	@ConfigOption(defaultValue = "0", description = "Minimum time the algorithm has to run before termination (even if solution " +
			"already found")
	private int minExecutionTimeInSeconds = CommonConfigOptions.minExecutionTimeInSecondsDefault;
	private boolean minExecutionTimeAlreadyReached = false;
	@ConfigOption(defaultValue = "1", description = "how many sufficient solutions must be found before termination, if " +
			"terminateOnNoiseReached is enabled")
	private int guaranteeXgoodDescriptions = CommonConfigOptions.guaranteeXgoodDescriptionsDefault;
	private boolean guaranteeXgoodAlreadyReached = false;
	@ConfigOption(defaultValue = "0", description = "The maximum number of candidate hypothesis the algorithm is " +
			"allowed to test (0 = no limit). The algorithm will stop afterwards")
	private int maxClassDescriptionTests = CommonConfigOptions.maxClassDescriptionTestsDefault;

	@ConfigOption(defaultValue = "false", description = "if set to false we do not test properness; this may seem wrong " +
			"but the disadvantage of properness testing are additional reasoner queries and a search bias towards " +
			"ALL r.something because ALL r.TOP is improper and automatically expanded further")
	private boolean usePropernessChecks = false;

	@ConfigOption(defaultValue = "false", description = "tree traversal means to run through the most promising " +
			"concepts and connect them in an intersection to find a solution (this is called irregularly e.g. " +
			"every 100 seconds)")
	private boolean useTreeTraversal = false;

	@ConfigOption(defaultValue = "true",
	              description = "if this variable is set to true, then the refinement operator is applied until all " +
			              "concept of equal length have been found e.g. TOP -> A1 -> A2 -> A3 is found in one loop; " +
			              "the disadvantage are potentially more method calls, but the advantage is that the algorithm " +
			              "is better in locating relevant concept in the subsumption hierarchy (otherwise, if the most " +
			              "general concept is not promising, it may never get expanded)")
	private boolean forceRefinementLengthIncrease = true;

	@ConfigOption(defaultValue = "true",
	              description = "candidate reduction: using this mechanism we can simulate the divide&conquer approach " +
			              "in many ILP programs using a clause by clause search; after a period of time the candidate " +
			              "set is reduced to focus CPU time on the most promising concepts")
	private boolean useCandidateReduction = true;
	@ConfigOption(defaultValue = "30", description = "maximum number of candidates to retain")
	private int candidatePostReductionSize = 30;

	// solution protocol
	private List<ExampleBasedNode> solutions = new LinkedList<>();

	@ConfigOption(defaultValue = "false", description = "specifies whether to compute and log benchmark information")
	private boolean computeBenchmarkInformation = false;

	// comparator used to maintain a stable ordering of nodes, i.e.
	// an ordering which does not change during the run of the algorithm
	private NodeComparatorStable nodeComparatorStable = new NodeComparatorStable();
	// node from which algorithm has started
	private SearchTreeNonWeak<ExampleBasedNode> searchTreeStable;
	private SearchTreeNonWeakPartialSet<ExampleBasedNode> searchTree;

	// evaluated descriptions

	// comparator for evaluated descriptions
	private EvaluatedDescriptionPosNegComparator edComparator = new EvaluatedDescriptionPosNegComparator();

	// utility variables
	private DecimalFormat df = new DecimalFormat();

	// all concepts which have been evaluated as being proper refinements
	private SortedSet<OWLClassExpression> properRefinements = new TreeSet<>();

	// blacklists
	private SortedSet<OWLClassExpression> tooWeakList = new TreeSet<>();
	private SortedSet<OWLClassExpression> overlyGeneralList = new TreeSet<>();

	// set of expanded nodes (TODO: better explanation)
	TreeSet<ExampleBasedNode> expandedNodes = new TreeSet<>(nodeComparatorStable);

	// statistic variables
	private int maxRecDepth = 0;
	private int maxNrOfRefinements = 0;
	private int maxNrOfChildren = 0;
	private int redundantConcepts = 0;
	private int propernessTestsReasoner = 0;
	private int propernessTestsAvoidedByShortConceptConstruction = 0;
	private int propernessTestsAvoidedByTooWeakList = 0;
	private int conceptTestsTooWeakList = 0;
	private int conceptTestsOverlyGeneralList = 0;
	private int conceptTestsReasoner = 0;

	// time variables
	private long runtime;
	private long algorithmStartTime;
	private long propernessCalcTimeNs = 0;
	private long propernessCalcReasoningTimeNs = 0;
	private long childConceptsDeletionTimeNs = 0;
	private long refinementCalcTimeNs = 0;
	private long redundancyCheckTimeNs = 0;
	private long evaluateSetCreationTimeNs = 0;
	private long improperConceptsRemovalTimeNs = 0;

	@ConfigOption(defaultValue = "true", description = "specifies whether to terminate when noise criterion is met")
	private boolean terminateOnNoiseReached = true;

	@ConfigOption(defaultValue = "1.0", description = "(for the ExampleBasedNode.) weighting factor on the number of " +
			"true negatives (true positives are weigthed with 1)")
	private double negativeWeight = 1.0;

	@ConfigOption(defaultValue = "0.1", description = "(for the ExampleBasedNode.) the score value for the start node")
	private double startNodeBonus = 0.1;

	@ConfigOption(description = "For the MultiHeuristic: how much accuracy gain is worth an increase of horizontal " +
			"expansion by one (typical value: 0.01)",
	              defaultValue = "0.02")
	private double expansionPenaltyFactor = 0.02;

	@ConfigOption(defaultValue = "0", description = "(for the ExampleBasedNode.) penalty value to deduce for using a " +
			"negated class expression (complementOf)")
	private int negationPenalty = 0;

	@ConfigOption(description = "adjust the weights of class expression length in refinement",
	              defaultValue = "OCEL default metric")
	private OWLClassExpressionLengthMetric lengthMetric;

	// dependencies
	@ConfigOption(defaultValue = "RhoDRDown", description = "the refinement operator instance to use")
	private LengthLimitedRefinementOperator operator;
	@ConfigOption(description = "the heuristic to guide the search", defaultValue = "MultiHeuristic")
	private ExampleBasedHeuristic heuristic;

	// configuration options
	private static String defaultSearchTreeFile = "log/searchTree.txt";

	@ConfigOption(defaultValue = "true", description = "if enabled, modifies the subsumption hierarchy such that for " +
			"each class, there is only a single path to reach it via upward and downward refinement respectively.")
	private boolean improveSubsumptionHierarchy = true;

	private static double noisePercentageDefault = 0.0;
	@ConfigOption(defaultValue = "0.0", description = "noise regulates how many positives can be misclassified and when " +
			"the algorithm terminates")
	private double noisePercentage = noisePercentageDefault;
	@ConfigOption(
			defaultValue = "owl:Thing",
			description = "You can specify a start class for the algorithm",
			exampleValue = "ex:Male or http://example.org/ontology/Female")
	private OWLClassExpression startClass = null;

	// Variablen zur Einstellung der Protokollierung
	@ConfigOption(defaultValue = "false", description = "show additional timing info for benchmark purposes")
	boolean showBenchmarkInformation = false;

	@ConfigOption(defaultValue = "0.5", description = "TODO")
	private double saLinearityStretch = 0.5;

	@ConfigOption(defaultValue = "LINEAR", description = "One of LINEAR, SUPERLINEAR and SUBLINEAR")
	private Cooling cooling = Cooling.LINEAR;

	@ConfigOption(defaultValue = "STEPWISE", description = "One of STEPWISE and TIME_BASED")
	private CoolingStrategy coolingStrategy = CoolingStrategy.STEPWISE;

	@ConfigOption(defaultValue = "50.0", description = "TODO")
	private double startTemperature = 50.0;

	@ConfigOption(defaultValue = "false", description = "TODO")
	private boolean adaptiveAnnealing = false;

	private int noImprovementCounter = 0;

	@ConfigOption(defaultValue = "20.0", description = "TODO")
	private double reHeatThreshold = 20.0;

	private SimulatedAnnealingHeuristic<ExampleBasedNode> saHeuristic;

	public OCELSA() {
	}

	// soll später einen Operator und eine Heuristik entgegennehmen
	public OCELSA(PosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}

	public OCELSA(PosOnlyLP learningProblem, AbstractReasonerComponent reasoningService) {
		super(learningProblem, reasoningService);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		// exit with a ComponentInitException if the reasoner is unsupported for this learning algorithm
		if (getReasoner().getReasonerType() == ReasonerType.DIG) {
			throw new ComponentInitException("DIG does not support the inferences needed in the selected learning algorithm component: " + AnnComponentManager.getName(this));
		}

		// set log level if the option has been set
		if (!logLevel.equals(CommonConfigOptions.logLevelDefault))
			org.apache.log4j.Logger.getLogger(OCELSA.class).setLevel(Level.toLevel(logLevel, Level.toLevel(CommonConfigOptions.logLevelDefault)));

		if (searchTreeFile == null)
			searchTreeFile = new File(defaultSearchTreeFile);

		if (writeSearchTree)
			Files.clearFile(searchTreeFile);

		// adjust heuristic

		if (heuristic == null) {
			if (getLearningProblem() instanceof PosOnlyLP) {
				throw new RuntimeException("does not work with positive examples only yet");
			} else {
				heuristic = new MultiHeuristic(((PosNegLP) getLearningProblem()).getPositiveExamples().size(), ((PosNegLP) getLearningProblem()).getNegativeExamples().size(), negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
			}
		} else {
			// we need to set some variables to make the heuristic work
			if (heuristic instanceof MultiHeuristic) {
				MultiHeuristic mh = ((MultiHeuristic) heuristic);
				if (mh.getNrOfNegativeExamples() == 0) {
					mh.setNrOfNegativeExamples(((PosNegLP) getLearningProblem()).getNegativeExamples().size());
				}
				int nrPosEx = ((PosNegLP) getLearningProblem()).getPositiveExamples().size();
				int nrNegEx = ((PosNegLP) getLearningProblem()).getNegativeExamples().size();
				if (mh.getNrOfExamples() == 0) {
					mh.setNrOfExamples(nrPosEx + nrNegEx);
				}
				if (mh.getNrOfNegativeExamples() == 0) {
					mh.setNrOfNegativeExamples(nrNegEx);
				}
			} else if (heuristic instanceof FlexibleHeuristic) {
				FlexibleHeuristic h2 = (FlexibleHeuristic) heuristic;
				if (h2.getNrOfNegativeExamples() == 0) {
					h2.setNrOfNegativeExamples(((PosNegLP) getLearningProblem()).getNegativeExamples().size());
				}
			}

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
		if (improveSubsumptionHierarchy) {
			classHierarchy.thinOutSubsumptionHierarchy();
			objectPropertyHierarchy.thinOutSubsumptionHierarchy();
			datatypePropertyHierarchy.thinOutSubsumptionHierarchy();
		}

		// create a refinement operator and pass all configuration
		// variables to it
		if (operator == null) {
			// we use a default operator and inject the class hierarchy for now
			operator = new RhoDRDown();
			if (operator instanceof CustomStartRefinementOperator) {
				((CustomStartRefinementOperator) operator).setStartClass(startClass);
			}
			if (operator instanceof ReasoningBasedRefinementOperator) {
				((ReasoningBasedRefinementOperator) operator).setReasoner(reasoner);
			}
			operator.init();
		}
		// TODO: find a better solution as this is quite difficult to debug
		if (operator instanceof CustomHierarchyRefinementOperator) {
			((CustomHierarchyRefinementOperator) operator).setClassHierarchy(classHierarchy);
			((CustomHierarchyRefinementOperator) operator).setObjectPropertyHierarchy(objectPropertyHierarchy);
			((CustomHierarchyRefinementOperator) operator).setDataPropertyHierarchy(datatypePropertyHierarchy);
		}

		if (lengthMetric == null) {
			lengthMetric = OWLClassExpressionLengthMetric.getOCELMetric();
		}
		operator.setLengthMetric(lengthMetric);

		// create an algorithm object and pass all configuration
		// options to it

		positiveExamples = ((PosNegLP) learningProblem).getPositiveExamples();
		negativeExamples = ((PosNegLP) learningProblem).getNegativeExamples();
		nrOfPositiveExamples = positiveExamples.size();
		nrOfNegativeExamples = negativeExamples.size();

		nrOfExamples = nrOfPositiveExamples + nrOfNegativeExamples;
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();
		// note: used concepts and roles do not need to be passed
		// as argument, because it is sufficient to prepare the
		// concept and role hierarchy accordingly

		saHeuristic =
				new SimulatedAnnealingHeuristic<>(startTemperature, cooling);

		initialized = true;
	}

	@Override
	public void start() {
		stop = false;
		isRunning = true;
		runtime = System.currentTimeMillis();

		// reset values (algorithms may be started several times)
		searchTree = new SearchTreeNonWeakPartialSet<>(heuristic);
		searchTreeStable = new SearchTreeNonWeak<>(nodeComparatorStable);
		solutions.clear();
		maxExecutionTimeAlreadyReached = false;
		minExecutionTimeAlreadyReached = false;
		guaranteeXgoodAlreadyReached = false;
		propernessTestsReasoner = 0;
		propernessTestsAvoidedByShortConceptConstruction = 0;
		propernessTestsAvoidedByTooWeakList = 0;
		conceptTestsTooWeakList = 0;
		conceptTestsOverlyGeneralList = 0;
		propernessCalcTimeNs = 0;
		propernessCalcReasoningTimeNs = 0;
		childConceptsDeletionTimeNs = 0;
		refinementCalcTimeNs = 0;
		redundancyCheckTimeNs = 0;
		evaluateSetCreationTimeNs = 0;
		improperConceptsRemovalTimeNs = 0;

		Monitor totalLearningTime = JamonMonitorLogger.getTimeMonitor(OCELSA.class, "totalLearningTime").start();
		// TODO: write a JUnit test for this problem (long-lasting or infinite loops because
		// redundant children of a node are called recursively when a node is extended twice)
		/*
		 */

		// calculate quality threshold required for a solution
		allowedMisclassifications = (int) Math.round(noisePercentage * nrOfExamples / 100);

		// start search with start class
		ExampleBasedNode startNode;
		if (startDescription == null) {
			startNode = new ExampleBasedNode(dataFactory.getOWLThing(), (OCEL) this);
			startNode.setCoveredExamples(positiveExamples, negativeExamples);
		} else {
			startNode = new ExampleBasedNode(startDescription, (OCEL) this);
			Set<OWLIndividual> coveredNegatives = reasoner.hasType(startDescription, negativeExamples);
			Set<OWLIndividual> coveredPositives = reasoner.hasType(startDescription, positiveExamples);
			startNode.setCoveredExamples(coveredPositives, coveredNegatives);
		}

		searchTree.addNode(null, startNode);
		searchTreeStable.addNode(null, startNode);

		ExampleBasedNode previousBestNode = startNode;
		ExampleBasedNode bestNode = startNode;
		ExampleBasedNode bestNodeStable = startNode;

		logger.info("starting top down refinement with: " + renderer.render(startNode.getConcept()) + " (" + df.format(100 * startNode.getAccuracy()) + "% accuracy)");

		int loop = 0;

		algorithmStartTime = System.nanoTime();
		long lastPrintTime = 0;
		long lastTreeTraversalTime = System.nanoTime();
		long lastReductionTime = System.nanoTime();
		// try a traversal after x seconds
		long traversalInterval = 300L * 1000000000L;
		long reductionInterval = 300L * 1000000000L;
		long currentTime;

		while (!isTerminationCriteriaReached()) {
			// print statistics at most once a second
			currentTime = System.nanoTime();
			if (currentTime - lastPrintTime > 3000000000L) {
				printStatistics(false);
				lastPrintTime = currentTime;
				logger.debug("--- loop " + loop + " started ---");
			}

			// traverse the current search tree to find a solution
			if (useTreeTraversal && (currentTime - lastTreeTraversalTime > traversalInterval)) {
				traverseTree();
				lastTreeTraversalTime = System.nanoTime();
			}

			// reduce candidates to focus on promising concepts
			if (useCandidateReduction && (currentTime - lastReductionTime > reductionInterval)) {
				reduceCandidates();
				lastReductionTime = System.nanoTime();
				// Logger.getRootLogger().setLevel(Level.TRACE);
			}

			// we record when a more accurate node is found and log it
			if (bestNodeStable.getCovPosMinusCovNeg() < searchTreeStable.best()
					.getCovPosMinusCovNeg()) {
				String acc = new DecimalFormat(".00%").format((searchTreeStable.best().getAccuracy()));
				// no handling needed, it will just look ugly in the output
				logger.info("more accurate (" + acc + ") class expression found: " + renderer.render(searchTreeStable.best().getConcept()));
				if (logger.isTraceEnabled()) {
					logger.trace(Sets.difference(positiveExamples, bestNodeStable.getCoveredNegatives()).toString());
					logger.trace(Sets.difference(negativeExamples, bestNodeStable.getCoveredNegatives()).toString());
				}
				printBestSolutions(5);
				printStatistics(false);
				bestNodeStable = searchTreeStable.best();
			}

			// chose best node according to heuristics
			bestNode = saHeuristic.pickNode(searchTree);

			// best node is removed temporarily, because extending it can
			// change its evaluation
			searchTree.updatePrepare(bestNode);
			extendNodeProper(bestNode, bestNode.getHorizontalExpansion() + 1);
			searchTree.updateDone(bestNode);

			if (adaptiveAnnealing && searchTree.best().getAccuracy() <= previousBestNode.getAccuracy()) {
				// No improvement
				noImprovementCounter++;

				if (noImprovementCounter == reHeatThreshold) {
					saHeuristic.heat();
					noImprovementCounter = 0;
				}

			} else {
				// Improvement, or adaptive simulated annealing switched off
				noImprovementCounter = 0;
				saHeuristic.cool();
			}
			previousBestNode = bestNode;

			if (writeSearchTree) {
				// String treeString = "";
				StringBuilder treeString = new StringBuilder("best node: " + bestNode + "\n");
				if (expandedNodes.size() > 1) {
					treeString.append("all expanded nodes:\n");
					for (ExampleBasedNode n : expandedNodes) {
						treeString.append("   ").append(n).append("\n");
					}
				}
				expandedNodes.clear();
				treeString.append(TreeUtils.toTreeString(startNode, heuristic));
				treeString.append("\n");

				if (replaceSearchTree)
					Files.createFile(searchTreeFile, treeString.toString());
				else
					Files.appendToFile(searchTreeFile, treeString.toString());
			}

			// Anzahl Schleifendurchläufe
			loop++;
		}// end while

		if (solutions.size() > 0) {
			int solutionLimit = 20;
			// we do not need to print the best node if we display the top 20 solutions below anyway
			logger.info("solutions (at most " + solutionLimit + " are shown):");
			int show = 1;
			for (ExampleBasedNode c : solutions) {
				logger.info(show + ": " + renderer.render(c.getConcept())
						+ " (accuracy " + df.format(100 * c.getAccuracy()) + "%, length "
						+ OWLClassExpressionUtils.getLength(c.getConcept())
						+ ", depth " + OWLClassExpressionUtils.getDepth(c.getConcept()) + ")");
				if (show >= solutionLimit) {
					break;
				}
				show++;
			}
		} else {
			logger.info("no appropriate solutions found (try increasing the noisePercentage parameter to what was reported as most accurate expression found above)");
		}

		logger.debug("size of candidate set: " + searchTree.size());
		printBestSolutions(20);

		printStatistics(true);

		int conceptTests = conceptTestsReasoner + conceptTestsTooWeakList + conceptTestsOverlyGeneralList;
		if (stop) {
			logger.info("Algorithm stopped (" + conceptTests + " descriptions tested).\n");
		} else {
			logger.info("Algorithm terminated successfully (" + conceptTests + " descriptions tested).\n");
			logger.info(reasoner.toString());
		}

		totalLearningTime.stop();
		isRunning = false;
	}

	// we apply the operator recursively until all proper refinements up
	// to the maxmimum length are reached
	private void extendNodeProper(ExampleBasedNode node, int maxLength) {
		long propCalcNsStart = System.nanoTime();

		if (writeSearchTree)
			expandedNodes.add(node);

		if (node.getChildren().size() > maxNrOfChildren)
			maxNrOfChildren = node.getChildren().size();

		extendNodeProper(node, node.getConcept(), maxLength, 0);
		node.setHorizontalExpansion(maxLength);

		propernessCalcTimeNs += (System.nanoTime() - propCalcNsStart);
	}

	// for all refinements of concept up to max length, we check whether they are proper
	// and call the method recursively if not
	// recDepth is used only for statistics
	private void extendNodeProper(ExampleBasedNode node, OWLClassExpression concept, int maxLength,
	                              int recDepth) {

		// do not execute methods if algorithm has been stopped (this means that the algorithm
		// will terminate without further reasoning queries)
		if (stop)
			return;

		if (recDepth > maxRecDepth)
			maxRecDepth = recDepth;

		// compute refinements => we must not delete refinements with low horizontal expansion,
		// because they are used in recursive calls of this method later on
		long refinementCalcTimeNsStart = System.nanoTime();
		Set<OWLClassExpression> refinements = operator.refine(concept, maxLength, null);
		refinementCalcTimeNs += System.nanoTime() - refinementCalcTimeNsStart;

		if (refinements.size() > maxNrOfRefinements)
			maxNrOfRefinements = refinements.size();

		// remove all refinements that are already children of the node
		long childConceptsDeletionTimeNsStart = System.nanoTime();
		refinements.removeAll(node.getChildConcepts());
		childConceptsDeletionTimeNs += System.nanoTime() - childConceptsDeletionTimeNsStart;

		// evaluate all concepts whose length is bigger than the horizontal expansion of the node
		long evaluateSetCreationTimeNsStart = System.nanoTime();
		Set<OWLClassExpression> toEvaluateConcepts = new TreeSet<>();
		Iterator<OWLClassExpression> it = refinements.iterator();

		while (it.hasNext()) {

			OWLClassExpression refinement = it.next();
			if (OWLClassExpressionUtils.getLength(refinement, lengthMetric) > node.getHorizontalExpansion()) {
				// TRUE means that improperness was detected, but FALSE does not mean that the refinement is proper
				boolean impropernessDetected = false;

				// 1. short concept construction
				if (useShortConceptConstruction) {
					OWLClassExpression shortConcept = ConceptTransformation.getShortConcept(refinement);
					// compare with original concept
					int n = shortConcept.compareTo(concept);

					// concepts are equal, i.e. refinement is improper
					if (n == 0) {
						propernessTestsAvoidedByShortConceptConstruction++;
						impropernessDetected = true;
					}
				}

				// 2. too weak test
				if (!impropernessDetected && useTooWeakList) {
					if (refinement instanceof OWLObjectIntersectionOf) {
						boolean tooWeakElement = containsTooWeakElement((OWLObjectIntersectionOf) refinement);
						if (tooWeakElement) {
							propernessTestsAvoidedByTooWeakList++;
							conceptTestsTooWeakList++;
							impropernessDetected = true;
							// tooWeakList.add(refinement);

							// Knoten wird direkt erzeugt (es ist buganfällig zwei Plätze
							// zu haben, an denen Knoten erzeugt werden, aber es erscheint
							// hier am sinnvollsten)
							properRefinements.add(refinement);
							tooWeakList.add(refinement);

							ExampleBasedNode newNode = new ExampleBasedNode(refinement, this);
							newNode.setHorizontalExpansion(OWLClassExpressionUtils.getLength(refinement, lengthMetric) - 1);
							newNode.setTooWeak(true);
							newNode.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.TOO_WEAK_LIST);
							node.addChild(newNode);

							// Refinement muss gelöscht werden, da es proper ist
							it.remove();
						}
					}
				}

				// properness konnte nicht vorher ermittelt werden
				if (!impropernessDetected) {
					toEvaluateConcepts.add(refinement);
				}

			}

		}
		evaluateSetCreationTimeNs += System.nanoTime() - evaluateSetCreationTimeNsStart;

		Set<OWLClassExpression> improperConcepts = null;
		if (toEvaluateConcepts.size() > 0) {
			// Test aller Konzepte auf properness (mit DIG in nur einer Anfrage)
			if (usePropernessChecks) {
				long propCalcReasoningStart = System.nanoTime();
				improperConcepts = reasoner.isSuperClassOf(toEvaluateConcepts, concept);

				propernessTestsReasoner += toEvaluateConcepts.size();
				propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart;
			}
		}

		long improperConceptsRemovalTimeNsStart = System.nanoTime();
		// die improper Konzepte werden von den auszuwertenden gelöscht, d.h.
		// alle proper concepts bleiben übrig (einfache Umbenennung)
		if (improperConcepts != null)
			toEvaluateConcepts.removeAll(improperConcepts);
		Set<OWLClassExpression> properConcepts = toEvaluateConcepts;
		// alle proper concepts von refinements löschen
		refinements.removeAll(properConcepts);
		improperConceptsRemovalTimeNs += System.nanoTime() - improperConceptsRemovalTimeNsStart;

		for (OWLClassExpression refinement : properConcepts) {
			long redundancyCheckTimeNsStart = System.nanoTime();
			boolean nonRedundant = properRefinements.add(refinement);
			redundancyCheckTimeNs += System.nanoTime() - redundancyCheckTimeNsStart;

			if (!nonRedundant)
				redundantConcepts++;

			// es wird nur ein neuer Knoten erzeugt, falls das Konzept nicht
			// schon existiert
			if (nonRedundant) {

				// newly created node
				ExampleBasedNode newNode = new ExampleBasedNode(refinement, this);
				// die -1 ist wichtig, da sonst keine gleich langen Refinements
				// für den neuen Knoten erlaubt wären z.B. person => male
				newNode.setHorizontalExpansion(OWLClassExpressionUtils.getLength(refinement, lengthMetric) - 1);

				boolean qualityKnown = false;
				int quality = -2;

				// overly general list verwenden
				if (useOverlyGeneralList && refinement instanceof OWLObjectUnionOf) {
					if (containsOverlyGeneralElement((OWLObjectUnionOf) refinement)) {
						conceptTestsOverlyGeneralList++;
						quality = nrOfNegativeExamples;
						qualityKnown = true;
						newNode.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.OVERLY_GENERAL_LIST);
						newNode.setCoveredExamples(positiveExamples, negativeExamples);
					}

				}

				// Qualität des Knotens auswerten
				if (!qualityKnown) { // -> quality == -2
					long propCalcReasoningStart2 = System.nanoTime();
					conceptTestsReasoner++;

					// determine individuals which have not been covered yet
					// (more efficient than full retrieval)
					Set<OWLIndividual> coveredPositives = node.getCoveredPositives();
					Set<OWLIndividual> newlyCoveredPositives = new HashSet<>();

					// calculate how many pos. examples are not covered by the
					// parent node of the refinement
					int misclassifiedPositives = nrOfPositiveExamples - coveredPositives.size();

					// iterate through all covered examples (examples which are not
					// covered do not need to be tested, because they remain uncovered);
					// DIG will be slow if we send each reasoner request individually
					// (however if we send everything in one request, too many instance checks
					// are performed => rely on fast instance checker)
					for (OWLIndividual i : coveredPositives) {
						// TODO: move code to a separate function
						if (quality != -1) {
							boolean covered = reasoner.hasType(refinement, i);
							if (!covered)
								misclassifiedPositives++;
							else
								newlyCoveredPositives.add(i);

							if (misclassifiedPositives > allowedMisclassifications)
								quality = -1;

						}
					}

					Set<OWLIndividual> newlyCoveredNegatives = null;
					if (quality != -1) {
						Set<OWLIndividual> coveredNegatives = node.getCoveredNegatives();
						newlyCoveredNegatives = new HashSet<>();

						for (OWLIndividual i : coveredNegatives) {
							boolean covered = reasoner.hasType(refinement, i);
							if (covered)
								newlyCoveredNegatives.add(i);
						}
					}

					propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart2;
					newNode.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.REASONER);
					if (quality != -1 && !(((PosNegLP) learningProblem).getAccuracyMethod() instanceof AccMethodNoWeakness) &&
							((PosNegLP) learningProblem).getAccuracyMethod().getAccOrTooWeak2(
									newlyCoveredPositives.size(), nrOfPositiveExamples - newlyCoveredPositives.size(),
									newlyCoveredNegatives.size(), nrOfNegativeExamples - newlyCoveredNegatives.size(),
									1) == -1)
						quality = -1;

					if (quality != -1) {
						// quality is the number of misclassifications (if it is
						// not too weak)
						quality = (nrOfPositiveExamples - newlyCoveredPositives.size())
								+ newlyCoveredNegatives.size();
						newNode.setCoveredExamples(newlyCoveredPositives, newlyCoveredNegatives);
					}

				}

				if (quality == -1) {
					newNode.setTooWeak(true);
					// Blacklist für too weak concepts
					tooWeakList.add(refinement);
				} else {
					// Lösung gefunden
					if (quality >= 0 && quality <= allowedMisclassifications) {
						solutions.add(newNode);
					}

					// we need to make sure that all positives are covered
					// before adding something to the overly general list
					if ((newNode.getCoveredPositives().size() == nrOfPositiveExamples)
							&& quality == nrOfNegativeExamples)
						overlyGeneralList.add(refinement);

				}

				node.addChild(newNode);

				// it is often useful to continue expanding until a longer node is
				// reached (to replace atomic concepts with more specific ones)
				if (forceRefinementLengthIncrease && !newNode.isTooWeak()) {
					// extend node again if its concept has the same length
					if (OWLClassExpressionUtils.getLength(node.getConcept(), lengthMetric) == OWLClassExpressionUtils.getLength(newNode.getConcept(), lengthMetric)) {
						extendNodeProper(newNode, refinement, maxLength, recDepth + 1);
					}
				}

			}
		}

		// es sind jetzt noch alle Konzepte übrig, die improper refinements sind
		// auf jedem dieser Konzepte wird die Funktion erneut aufgerufen, da
		// sich proper refinements ergeben könnten
		for (OWLClassExpression refinement : refinements) {
			// check for redundancy (otherwise we may run into very
			// time-intensive loops, see planned JUnit test case $x)

			long redundancyCheckTimeNsStart = System.nanoTime();
			boolean redundant = properRefinements.contains(refinement);
			redundancyCheckTimeNs += System.nanoTime() - redundancyCheckTimeNsStart;

			if (!redundant) {
				extendNodeProper(node, refinement, maxLength, recDepth + 1);
			}
		}
	}

	private void printStatistics(boolean finalStats) {
		// TODO: viele Tests haben ergeben, dass man nie 100% mit der Zeitmessung abdecken
		// kann (zum einen weil Stringausgabe verzögert erfolgt und zum anderen weil
		// Funktionsaufrufe, garbage collection, Zeitmessung selbst auch Zeit benötigt);
		// es empfiehlt sich folgendes Vorgehen:
		// - Messung der Zeit eines Loops im Algorithmus
		// - Messung der Zeit für alle node extensions innerhalb eines Loops
		// => als Normalisierungsbasis empfehlen sich dann die Loopzeit statt
		// Algorithmuslaufzeit
		// ... momentan kann es aber auch erstmal so lassen

		long algorithmRuntime = System.nanoTime() - algorithmStartTime;

		if (!finalStats) {

			ExampleBasedNode bestNode = searchTreeStable.best();
			logger.debug("start node: "
					+ searchTreeStable.getRoot().getShortDescription());
			String bestNodeString = "currently best node: "
					+ bestNode.getShortDescription();

			logger.debug(bestNodeString);
			logger.trace(bestNode.getStats());
			if (bestNode.getCoveredNegatives().size() <= 5)
				logger.trace("covered negs: " + bestNode.getCoveredNegatives());
			String expandedNodeString = "next expanded node: "
					+ searchTree.best().getShortDescription();
			logger.debug(expandedNodeString);
			logger.debug("algorithm runtime " + Helper.prettyPrintNanoSeconds(algorithmRuntime));
			logger.debug("size of candidate set: " + searchTree.size());
			logger.debug("subsumption time: " + Helper.prettyPrintNanoSeconds(reasoner.getSubsumptionReasoningTimeNs()));
			logger.debug("instance check time: " + Helper.prettyPrintNanoSeconds(reasoner.getInstanceCheckReasoningTimeNs()));
			logger.debug("retrieval time: " + Helper.prettyPrintNanoSeconds(reasoner.getRetrievalReasoningTimeNs()));
		}

		if (computeBenchmarkInformation) {

			long reasoningTime = reasoner.getOverallReasoningTimeNs();
			double reasoningPercentage = 100 * reasoningTime / (double) algorithmRuntime;
			long propWithoutReasoning = propernessCalcTimeNs - propernessCalcReasoningTimeNs;
			double propPercentage = 100 * propWithoutReasoning / (double) algorithmRuntime;
			double deletionPercentage = 100 * childConceptsDeletionTimeNs / (double) algorithmRuntime;
			long subTime = reasoner.getSubsumptionReasoningTimeNs();
			double subPercentage = 100 * subTime / (double) algorithmRuntime;
			double refinementPercentage = 100 * refinementCalcTimeNs / (double) algorithmRuntime;
			double redundancyCheckPercentage = 100 * redundancyCheckTimeNs / (double) algorithmRuntime;
			double evaluateSetCreationTimePercentage = 100 * evaluateSetCreationTimeNs / (double) algorithmRuntime;
			double improperConceptsRemovalTimePercentage = 100 * improperConceptsRemovalTimeNs / (double) algorithmRuntime;

			logger.debug("reasoning percentage: " + df.format(reasoningPercentage) + "%");
			logger.debug("   subsumption check time: " + df.format(subPercentage) + "%");
			logger.debug("proper calculation percentage (wo. reasoning): " + df.format(propPercentage) + "%");
			logger.debug("   deletion time percentage: " + df.format(deletionPercentage) + "%");
			logger.debug("   refinement calculation percentage: " + df.format(refinementPercentage) + "%");

			if (operator instanceof RhoDRDown) {
				double mComputationTimePercentage = 100 * ((RhoDRDown) operator).mComputationTimeNs / (double) algorithmRuntime;
				double topComputationTimePercentage = 100 * ((RhoDRDown) operator).topComputationTimeNs / (double) algorithmRuntime;
				logger.debug("      m calculation percentage: " + df.format(mComputationTimePercentage) + "%");
				logger.debug("      top calculation percentage: " + df.format(topComputationTimePercentage) + "%");
			}

			double cleanTimePercentage = 100 * ConceptTransformation.cleaningTimeNs / (double) algorithmRuntime;
			double onnfTimePercentage = 100 * ConceptTransformation.onnfTimeNs / (double) algorithmRuntime;
			double shorteningTimePercentage = 100 * ConceptTransformation.shorteningTimeNs / (double) algorithmRuntime;

			logger.debug("   redundancy check percentage: " + df.format(redundancyCheckPercentage) + "%");
			logger.debug("   evaluate set creation time percentage: " + df.format(evaluateSetCreationTimePercentage) + "%");
			logger.debug("   improper concepts removal time percentage: " + df.format(improperConceptsRemovalTimePercentage) + "%");
			logger.debug("clean time percentage: " + df.format(cleanTimePercentage) + "%");
			logger.debug("onnf time percentage: " + df.format(onnfTimePercentage) + "%");
			logger.debug("shortening time percentage: " + df.format(shorteningTimePercentage) + "%");
		}

		logger.debug("properness tests (reasoner/short concept/too weak list): "
				+ propernessTestsReasoner + "/" + propernessTestsAvoidedByShortConceptConstruction
				+ "/" + propernessTestsAvoidedByTooWeakList);
		logger.debug("concept tests (reasoner/too weak list/overly general list/redundant concepts): "
				+ conceptTestsReasoner + "/" + conceptTestsTooWeakList + "/" + conceptTestsOverlyGeneralList + "/" + redundantConcepts);
	}

	private boolean containsTooWeakElement(OWLObjectIntersectionOf mc) {
		for (OWLClassExpression child : mc.getOperands()) {
			if (tooWeakList.contains(child))
				return true;
		}
		return false;
	}

	private boolean containsOverlyGeneralElement(OWLObjectUnionOf md) {
		for (OWLClassExpression child : md.getOperands()) {
			if (overlyGeneralList.contains(child))
				return true;
		}
		return false;
	}

	// TODO: investigate whether it makes sense not to store all individuals
	// in the nodes, but instead perform instance checks in tree traversal
	// (it is only run in large intervals so it shouldn't be too expensive)
	private void traverseTree() {
		ExampleBasedNode startNode = findBestTraversalStartNode();
		OWLClassExpression currentDescription = startNode.getConcept();
		Set<OWLIndividual> currentCoveredPos = startNode.getCoveredPositives();
		Set<OWLIndividual> currentCoveredNeg = startNode.getCoveredNegatives();
		double currentAccuracy = startNode.getAccuracy();

		int currentMisclassifications = nrOfPositiveExamples - currentCoveredPos.size()
				+ currentCoveredNeg.size();

		logger.debug("tree traversal start node " + startNode.getShortDescription());
		logger.debug("tree traversal start accuracy: " + currentAccuracy);
		int i = 0;

		// start from the most promising nodes
		SortedSet<ExampleBasedNode> reverseView = searchTreeStable.descendingSet();
		for (ExampleBasedNode currNode : reverseView) {
			// compute covered positives and negatives
			SortedSet<OWLIndividual> newCoveredPositives = new TreeSet<>(currentCoveredPos);
			newCoveredPositives.retainAll(currNode.getCoveredPositives());

			SortedSet<OWLIndividual> newCoveredNegatives = new TreeSet<>(currentCoveredNeg);
			newCoveredNegatives.retainAll(currNode.getCoveredNegatives());

			// compute the accuracy we would get by adding this node
			double accuracy = (newCoveredPositives.size() + nrOfNegativeExamples - newCoveredNegatives.size())
					/ (double) (nrOfPositiveExamples + nrOfNegativeExamples);
			int misclassifications = nrOfPositiveExamples - newCoveredPositives.size() + newCoveredNegatives.size();
			int misclassifiedPositives = nrOfPositiveExamples - newCoveredPositives.size();

			int lostPositives = currentCoveredPos.size() - newCoveredPositives.size();

			// TODO: maybe we should also consider a minimum improvement when adding something
			// otherwise we could overfit
			// we give double weith to lost positives, i.e. when one positive is
			// lost at least two negatives need to be uncovered
			boolean consider = (misclassifications + lostPositives < currentMisclassifications)
					&& (misclassifiedPositives <= allowedMisclassifications);

			// concept has been chosen, so construct it
			if (consider) {

				// construct a new concept as intersection of both
				OWLClassExpression mc = dataFactory.getOWLObjectIntersectionOf(currentDescription, currNode.getConcept());

				mc = ConceptTransformation.cleanConceptNonRecursive(mc);
				mc = mc.getNNF();

				logger.debug("misclassifications: " + misclassifications);
				logger.debug("misclassified positives: " + misclassifiedPositives);
				logger.debug("accuracy: " + accuracy);

				// update variables
				currentDescription = mc;
				currentCoveredPos = newCoveredPositives;
				currentCoveredNeg = newCoveredNegatives;
				currentMisclassifications = misclassifications;
				//noinspection UnusedAssignment
				currentAccuracy = accuracy;

				if (accuracy > 1 - (noisePercentage / 100)) {
					logger.info("traversal found " + mc);
					logger.info("accuracy: " + accuracy);
					System.exit(0);
				}
			}

			i++;
			if (i == 1000)
				break;
		}

	}

	// we look for a node covering many positives and hopefully
	// few negatives; we give a strong penalty on uncovered positives
	private ExampleBasedNode findBestTraversalStartNode() {
		// 2 points for each covered pos + 1 point for each uncovered neg
		int currScore = 0;
		int i = 0;

		ExampleBasedNode currNode = null;
		SortedSet<ExampleBasedNode> reverseView = searchTreeStable.descendingSet();
		for (ExampleBasedNode node : reverseView) {
			int score = 2 * node.getCoveredPositives().size()
					+ (nrOfNegativeExamples - node.getCoveredNegatives().size());
			if (score > currScore) {
				currScore = score;
				currNode = node;
			}
			i++;
			// limit search because stable candidate set can grow very large
			if (i == 10000)
				break;
		}
		return currNode;
	}

	private void reduceCandidates() {
		Iterator<ExampleBasedNode> it = searchTreeStable.descendingIterator();
		Set<ExampleBasedNode> promisingNodes = new HashSet<>();
		int i = 0;
		while (it.hasNext() && promisingNodes.size() < candidatePostReductionSize) {
			ExampleBasedNode node = it.next();
			// first criterion: the considered node should have an accuracy gain over its parent
			// (avoids to use only the most promising node + all its refinements with equal accuracy)
			boolean hasAccuracyGain = (node.getParent() == null)
					|| (node.getCoveredPositives().size() != node.getParent().getCoveredPositives().size())
					|| (node.getCoveredNegatives().size() != node.getParent().getCoveredNegatives().size());
			// second criterion: uncovered positives; it does not make much sense to pick nodes with
			// low potential for reaching a solution (already at the limit of misclassified positives)
			int misclassifiedPositives = nrOfPositiveExamples - node.getCoveredPositives().size();
			boolean hasRefinementPotential = (misclassifiedPositives <= Math.floor(0.65d * allowedMisclassifications));
			boolean keep = hasAccuracyGain && hasRefinementPotential;
			if (keep) {
				promisingNodes.add(node);
			}
			i++;
		}
		searchTree.retainAll(promisingNodes);
		logger.debug("searched " + i + " nodes and picked the following promising descriptions:");
		if (logger.isDebugEnabled()) {
			for (ExampleBasedNode node : promisingNodes)
				logger.debug(node.getShortDescription());
		}
	}

	public OWLClassExpression getBestSolution() {
		return searchTreeStable.best().getConcept();
	}

	public List<OWLClassExpression> getCurrentlyBestDescriptions() {
		List<OWLClassExpression> best = new LinkedList<>();
		int i = 0;
		int nrOfSolutions = 200;
		for (ExampleBasedNode n : searchTreeStable.descendingSet()) {
			best.add(n.getConcept());
			if (i == nrOfSolutions)
				return best;
			i++;
		}
		return best;
	}

	public TreeSet<EvaluatedDescriptionPosNeg> getCurrentlyBestEvaluatedDescriptions() {
		Iterator<ExampleBasedNode> it = searchTreeStable.descendingIterator();
		int count = 0;
		TreeSet<EvaluatedDescriptionPosNeg> cbd = new TreeSet<>(edComparator);
		while (it.hasNext()) {
			ExampleBasedNode eb = it.next();
			cbd.add(new EvaluatedDescriptionPosNeg(eb.getConcept(), getScore(eb.getConcept())));
			// return a maximum of 200 elements (we need a maximum, because the
			// candidate set can be very large)
			if (count > 200)
				return cbd;
			count++;
		}
		return cbd;
	}

	public void printBestSolutions(int nrOfSolutions) {
		// QUALITY: could be optimized
		if (!logger.isTraceEnabled()) {
			return;
		}
		if (nrOfSolutions == 0)
			nrOfSolutions = searchTreeStable.size();
		int i = 0;
		for (ExampleBasedNode n : searchTreeStable.descendingSet()) {
			if (n.getAccuracy() < 1)
				break;
			logger.trace("best: "
					+ n.getShortDescription());
			if (i == nrOfSolutions)
				break;
			i++;
		}

	}

	public ScorePosNeg getSolutionScore() {
		return ((PosNegLP) learningProblem).computeScore(getBestSolution());
	}

	private ScorePosNeg getScore(OWLClassExpression d) {
		return ((PosNegLP) learningProblem).computeScore(d);
	}

	public ExampleBasedNode getStartNode() {
		return searchTreeStable.getRoot();
	}

	/**
	 * In this function it is calculated whether the algorithm should stop.
	 * This is not always depends whether an actual solution was found
	 * The algorithm stops if:
	 * 1. the object attribute stop is set to true (possibly by an outside source)
	 * 2. the maximimum execution time is reached
	 * 3. the maximum number of class description tests is reached
	 *
	 * Continuation criteria and result improvement
	 * The algorithm continues (although it would normally stop) if
	 * 1. Minimum execution time is not reached (default 0)
	 * 2. not enough good solutions are found (default 1)
	 * otherwise it stops
	 *
	 * @return true if the algorithm should stop, this is mostly indepent of the question if a solution was found
	 */
	private boolean isTerminationCriteriaReached() {
		// algorithm was stopped from outside
		if (this.stop) {
			return true;
		}

		long totalTimeNeeded = System.currentTimeMillis() - this.runtime;
		long maxMilliSeconds = maxExecutionTimeInSeconds * 1000;
		long minMilliSeconds = minExecutionTimeInSeconds * 1000;
		int conceptTests = conceptTestsReasoner + conceptTestsTooWeakList + conceptTestsOverlyGeneralList;
		boolean result = false;

		//ignore default
		if (maxExecutionTimeInSeconds == 0)
			result = false;
			//alreadyReached
		else if (maxExecutionTimeAlreadyReached)
			return true;
			//test
		else if (maxMilliSeconds < totalTimeNeeded) {
			this.stop();
			logger.info("Maximum time (" + maxExecutionTimeInSeconds
					+ " seconds) reached, stopping now...");
			maxExecutionTimeAlreadyReached = true;
			return true;
		}

		//ignore default
		if (maxClassDescriptionTests == 0)
			result = false;
			//test
		else if (conceptTests >= maxClassDescriptionTests) {
			logger.info("Maximum Class OWLClassExpression tests (" + maxClassDescriptionTests
					+ " tests [actual: " + conceptTests + "]) reached, stopping now...");
			return true;
		}

		// we stop if sufficiently many solutions (concepts fitting the noise parameter) have been
		// reached - unless this termination criterion is switched off using terminateOnNoiseReached = false
		if (guaranteeXgoodAlreadyReached) {
			result = true;
		} else if (solutions.size() >= guaranteeXgoodDescriptions && terminateOnNoiseReached) {
			if (guaranteeXgoodDescriptions != 1) {
				logger.info("Minimum number (" + guaranteeXgoodDescriptions + ") of good descriptions reached.");

				guaranteeXgoodAlreadyReached = true;
				result = true;
			}
		}

		if (minExecutionTimeAlreadyReached) {
			result = result && true;
		} else if (minMilliSeconds < totalTimeNeeded) {
			if (minExecutionTimeInSeconds != 0) {
				logger.info("Minimum time (" + minExecutionTimeInSeconds + " seconds) reached.");
			}
			minExecutionTimeAlreadyReached = true;
			result = result && true;
		} else {
			result = false;
		}

		return result;

	}

	public boolean isUseTreeTraversal() {
		return useTreeTraversal;
	}

	public void setUseTreeTraversal(boolean useTreeTraversal) {
		this.useTreeTraversal = useTreeTraversal;
	}

	public boolean isUseCandidateReduction() {
		return useCandidateReduction;
	}

	public void setUseCandidateReduction(boolean useCandidateReduction) {
		this.useCandidateReduction = useCandidateReduction;
	}

	public int getCandidatePostReductionSize() {
		return candidatePostReductionSize;
	}

	public void setCandidatePostReductionSize(int candidatePostReductionSize) {
		this.candidatePostReductionSize = candidatePostReductionSize;
	}

	public boolean isComputeBenchmarkInformation() {
		return computeBenchmarkInformation;
	}

	public void setComputeBenchmarkInformation(boolean computeBenchmarkInformation) {
		this.computeBenchmarkInformation = computeBenchmarkInformation;
	}

	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		return getBestSolution();
	}

	@Override
	public EvaluatedDescriptionPosNeg getCurrentlyBestEvaluatedDescription() {
		return new EvaluatedDescriptionPosNeg(getBestSolution(), getSolutionScore());
	}

	public LengthLimitedRefinementOperator getRefinementOperator() {
		return operator;
	}

	public LengthLimitedRefinementOperator getOperator() {
		return operator;
	}

	@Autowired(required = false)
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

	public OWLClassExpression getStartClass() {
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

	public boolean isForceRefinementLengthIncrease() {
		return forceRefinementLengthIncrease;
	}

	public void setForceRefinementLengthIncrease(boolean forceRefinementLengthIncrease) {
		this.forceRefinementLengthIncrease = forceRefinementLengthIncrease;
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

	public int getNegationPenalty() {
		return negationPenalty;
	}

	public void setNegationPenalty(int negationPenalty) {
		this.negationPenalty = negationPenalty;
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
		if (operator != null) {
			operator.setLengthMetric(lengthMetric);
		}
	}

	@Autowired(required = false)
	public void setHeuristic(ExampleBasedHeuristic heuristic) {
		this.heuristic = heuristic;
	}

	public ExampleBasedHeuristic getHeuristic() {
		return heuristic;
	}

	public void setSaLinearityStretch(double saLinearityStretch) {
		this.saLinearityStretch = saLinearityStretch;
	}

	public void setCooling(String cooling) {
		if (cooling.toLowerCase().equals("linear")) {
			this.cooling = Cooling.LINEAR;
		} else if (cooling.toLowerCase().equals("sublinear")) {
			this.cooling = Cooling.SUBLINEAR;
		} else if (cooling.toLowerCase().equals("superlinear")) {
			this.cooling = Cooling.SUPERLINEAR;
		}
	}

	public void setCoolingStrategy(String coolingStrategy) {
		if (coolingStrategy.toLowerCase().equals("stepwise")) {
			this.coolingStrategy = CoolingStrategy.STEPWISE;
		} else if (coolingStrategy.toLowerCase().startsWith("time")) {
			this.coolingStrategy = CoolingStrategy.TIME_BASED;
		}
	}

	public void setStartTemperature(double startTemperature) {
		this.startTemperature = startTemperature;
	}

	public void setAdaptiveAnnealing(boolean adaptiveAnnealing) {
		this.adaptiveAnnealing = adaptiveAnnealing;
	}

	public void setReHeatThreshold(double reHeatThreshold) {
		this.reHeatThreshold = reHeatThreshold;
	}
}
