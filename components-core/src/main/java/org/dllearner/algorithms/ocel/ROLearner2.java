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

package org.dllearner.algorithms.ocel;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.EvaluatedDescriptionPosNegComparator;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.jamonapi.Monitor;

/**
 * Implements the 2nd version of the refinement operator based learning approach.
 * 
 * @author Jens Lehmann
 * 
 */
public class ROLearner2 {

	private static Logger logger = Logger.getLogger(ROLearner2.class);
//	private OCELConfigurator configurator;

	// basic setup: learning problem and reasoning service
	private AbstractReasonerComponent rs;
	// often the learning problems needn't be accessed directly; instead
	// use the example sets below and the posonly variable
	private PosNegLP learningProblem;
	private OWLClassExpression startDescription;
	private int nrOfExamples;
	private int nrOfPositiveExamples;
	private Set<OWLIndividual> positiveExamples;
	private int nrOfNegativeExamples;
	private Set<OWLIndividual> negativeExamples;

	// noise regulates how many positives can be misclassified and when the
	// algorithm terminates
	private double noise = 0.0;
	private int allowedMisclassifications = 0;

	// search tree options
	private boolean writeSearchTree;
	private File searchTreeFile;
	private boolean replaceSearchTree = false;

	// constructs to improve performance
	private boolean useTooWeakList = true;
	private boolean useOverlyGeneralList = true;
	private boolean useShortConceptConstruction = true;

	// extended Options
	private long maxExecutionTimeInSeconds;
	private boolean maxExecutionTimeAlreadyReached = false;
	private long minExecutionTimeInSeconds = 0;
	private boolean minExecutionTimeAlreadyReached = false;
	private int guaranteeXgoodDescriptions = 1;
	private boolean guaranteeXgoodAlreadyReached = false;
	private int maxClassDescriptionTests;

	// if set to false we do not test properness; this may seem wrong
	// but the disadvantage of properness testing are additional reasoner
	// queries and a search bias towards ALL r.something because
	// ALL r.TOP is improper and automatically expanded further
	private boolean usePropernessChecks = false;

	// tree traversal means to run through the most promising concepts
	// and connect them in an intersection to find a solution
	// (this is called irregularly e.g. every 100 seconds)
	private boolean useTreeTraversal = false;

	// if this variable is set to true, then the refinement operator
	// is applied until all concept of equal length have been found
	// e.g. TOP -> A1 -> A2 -> A3 is found in one loop; the disadvantage
	// are potentially more method calls, but the advantage is that 
	// the algorithm is better in locating relevant concept in the 
	// subsumption hierarchy (otherwise, if the most general concept
	// is not promising, it may never get expanded)
	private boolean forceRefinementLengthIncrease;
	
	// candidate reduction: using this mechanism we can simulate
	// the divide&conquer approach in many ILP programs using a
	// clause by clause search; after a period of time the candidate
	// set is reduced to focus CPU time on the most promising concepts
	private boolean useCandidateReduction = true;
	private int candidatePostReductionSize = 30;

	// setting to true gracefully stops the algorithm
	private boolean stop = false;
	private boolean isRunning = false;

	// node from which algorithm has started
	private ExampleBasedNode startNode;

	// solution protocol
	private List<ExampleBasedNode> solutions = new LinkedList<ExampleBasedNode>();

	// used refinement operator and heuristic (exchangeable)
	private RhoDRDown operator;
	// private RefinementOperator operator;
	// private ExampleBasedHeuristic heuristic;

	// specifies whether to compute and log benchmark information
	private boolean computeBenchmarkInformation = false;

	// comparator used to maintain a stable ordering of nodes, i.e.
	// an ordering which does not change during the run of the algorithm
	private NodeComparatorStable nodeComparatorStable = new NodeComparatorStable();
	// stable candidate set; it has no functional part in the algorithm,
	// but is a list of the currently best concepts found;
	// it is very important to use a concurrent set here as other threads will
	// access it (usual iterating is likely to throw a ConcurrentModificationException)
	private NavigableSet<ExampleBasedNode> candidatesStable = new ConcurrentSkipListSet<ExampleBasedNode>(
			nodeComparatorStable);
	// evaluated descriptions
//	private EvaluatedDescriptionSet evaluatedDescriptions = new EvaluatedDescriptionSet(LearningAlgorithm.MAX_NR_OF_RESULTS);

	// comparator for evaluated descriptions
	private EvaluatedDescriptionPosNegComparator edComparator = new EvaluatedDescriptionPosNegComparator();

	// utility variables
	private DecimalFormat df = new DecimalFormat();

	// candidates for refinement (used for directly accessing
	// nodes in the search tree)
	private TreeSet<ExampleBasedNode> candidates;

	// new nodes found during a run of the algorithm
	private List<ExampleBasedNode> newCandidates = new LinkedList<ExampleBasedNode>();

	// all concepts which have been evaluated as being proper refinements
	private SortedSet<OWLClassExpression> properRefinements = new TreeSet<OWLClassExpression>();

	// blacklists
	private SortedSet<OWLClassExpression> tooWeakList = new TreeSet<OWLClassExpression>();
	private SortedSet<OWLClassExpression> overlyGeneralList = new TreeSet<OWLClassExpression>();

	// set of expanded nodes (TODO: better explanation)
	TreeSet<ExampleBasedNode> expandedNodes = new TreeSet<ExampleBasedNode>(nodeComparatorStable);

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

	// prefixes
	private String baseURI;
	private Map<String, String> prefixes;

	private boolean terminateOnNoiseReached;

	private double negativeWeight;

	private double startNodeBonus;

	private double expansionPenaltyFactor;

	private int negationPenalty;
	
	private OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	public ROLearner2(
//			OCELConfigurator configurator,
			AbstractClassExpressionLearningProblem learningProblem,
			AbstractReasonerComponent rs,
			RefinementOperator operator,
			ExampleBasedHeuristic heuristic,
			OWLClassExpression startDescription,
			// Set<AtomicConcept> allowedConcepts,
			// Set<AtomicRole> allowedRoles,
			double noise, boolean writeSearchTree, boolean replaceSearchTree, File searchTreeFile,
			boolean useTooWeakList, boolean useOverlyGeneralList,
			boolean useShortConceptConstruction, boolean usePropernessChecks,
			int maxPosOnlyExpansion, int maxExecutionTimeInSeconds, int minExecutionTimeInSeconds,
			int guaranteeXgoodDescriptions, int maxClassDescriptionTests, boolean forceRefinementLengthIncrease,
			boolean terminateOnNoiseReached,
			double negativeWeight, double startNodeBonus, double expansionPenaltyFactor, int negationPenalty) {

		
			PosNegLP lp = (PosNegLP) learningProblem;
			this.learningProblem = lp;
			positiveExamples = lp.getPositiveExamples();
			negativeExamples = lp.getNegativeExamples();
			nrOfPositiveExamples = positiveExamples.size();
			nrOfNegativeExamples = negativeExamples.size();

			this.negativeWeight = negativeWeight;
			this.startNodeBonus = startNodeBonus;
			this.expansionPenaltyFactor = expansionPenaltyFactor;
			this.negationPenalty = negationPenalty;
			
			// System.out.println(nrOfPositiveExamples);
			// System.out.println(nrOfNegativeExamples);
			// System.exit(0);

//		this.configurator = configurator;
		this.terminateOnNoiseReached = terminateOnNoiseReached;
		nrOfExamples = nrOfPositiveExamples + nrOfNegativeExamples;
		this.rs = rs;
		this.operator = (RhoDRDown) operator;
		this.startDescription = startDescription;
		// initialise candidate set with heuristic as ordering
		candidates = new TreeSet<ExampleBasedNode>(heuristic);
		this.noise = noise;
		this.writeSearchTree = writeSearchTree;
		this.replaceSearchTree = replaceSearchTree;
		this.searchTreeFile = searchTreeFile;
		this.useTooWeakList = useTooWeakList;
		this.useOverlyGeneralList = useOverlyGeneralList;
		this.useShortConceptConstruction = useShortConceptConstruction;
		this.usePropernessChecks = usePropernessChecks;
		baseURI = rs.getBaseURI();
		prefixes = rs.getPrefixes();
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
		this.minExecutionTimeInSeconds = minExecutionTimeInSeconds;
		this.guaranteeXgoodDescriptions = guaranteeXgoodDescriptions;
		this.maxClassDescriptionTests = maxClassDescriptionTests;
		this.forceRefinementLengthIncrease = forceRefinementLengthIncrease;

		// logger.setLevel(Level.DEBUG);
	}

	public void start() {
		stop = false;
		isRunning = true;
		runtime = System.currentTimeMillis();
		
		// reset values (algorithms may be started several times)
		candidates.clear();
		candidatesStable.clear();
		newCandidates.clear();
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
		
		Monitor totalLearningTime = JamonMonitorLogger.getTimeMonitor(OCEL.class, "totalLearningTime")
				.start();
		// TODO: write a JUnit test for this problem (long-lasting or infinite
		// loops because
		// redundant children of a node are called recursively when a node is
		// extended
		// twice)
		/*
		 * // String conceptStr =
		 * "(\"http://dl-learner.org/carcinogenesis#Compound\" AND (>= 2
		 * \"http://dl-learner.org/carcinogenesis#hasStructure\".\"http://dl-learner.org/carcinogenesis#Ar_halide\"
		 * OR ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS
		 * TRUE) AND >= 5 \"http://dl-learner.org/carcinogenesis#hasBond\".
		 * TOP)))"; // String conceptStr =
		 * "(\"http://dl-learner.org/carcinogenesis#Compound\" AND
		 * ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS TRUE)
		 * AND (\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS
		 * TRUE)))"; String conceptStr =
		 * "(\"http://dl-learner.org/carcinogenesis#Compound\" AND (>= 3
		 * \"http://dl-learner.org/carcinogenesis#hasStructure\".\"http://dl-learner.org/carcinogenesis#Halide\"
		 * OR ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS
		 * TRUE) AND ALL
		 * \"http://dl-learner.org/carcinogenesis#hasAtom\".TOP)))"; String
		 * conceptStr2 = "(\"http://dl-learner.org/carcinogenesis#Compound\" AND
		 * (>= 4
		 * \"http://dl-learner.org/carcinogenesis#hasStructure\".\"http://dl-learner.org/carcinogenesis#Halide\"
		 * OR ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS
		 * TRUE) AND ALL
		 * \"http://dl-learner.org/carcinogenesis#hasAtom\".TOP)))"; try {
		 * OWLClass struc = new
		 * NamedClass("http://dl-learner.org/carcinogenesis#Compound");
		 * OWLClassExpression d = KBParser.parseConcept(conceptStr); OWLClassExpression d2 =
		 * KBParser.parseConcept(conceptStr2); // SortedSet<OWLClassExpression> ds =
		 * (SortedSet<OWLClassExpression>) operator.refine(d,15,null,struc); //
		 * System.out.println(ds);
		 *  // System.out.println(RhoDRDown.checkIntersection((Intersection)d));
		 * 
		 * 
		 * Set<OWLIndividual> coveredNegatives = rs.instanceCheck(d,
		 * learningProblem.getNegativeExamples()); Set<OWLIndividual>
		 * coveredPositives = rs.instanceCheck(d,
		 * learningProblem.getPositiveExamples()); ExampleBasedNode ebn = new
		 * ExampleBasedNode(d); ebn.setCoveredExamples(coveredPositives,
		 * coveredNegatives);
		 * 
		 * properRefinements.add(d2); extendNodeProper(ebn,13);
		 * extendNodeProper(ebn,14); for(OWLClassExpression refinement:
		 * ebn.getChildConcepts()) System.out.println("refinement: " +
		 * refinement);
		 *  // Individual i = new
		 * Individual("http://dl-learner.org/carcinogenesis#d101"); //
		 * for(OWLIndividual i : learningProblem.getPositiveExamples()) //
		 * rs.instanceCheck(ds.last(), i);
		 * 
		 * System.out.println("finished"); } catch (ParseException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } System.exit(0);
		 */

		// calculate quality threshold required for a solution
		allowedMisclassifications = (int) Math.round(noise * nrOfExamples);

		// start search with start class
		if (startDescription == null) {
			startNode = new ExampleBasedNode(dataFactory.getOWLThing(), negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
			startNode.setCoveredExamples(positiveExamples, negativeExamples);
		} else {
			startNode = new ExampleBasedNode(startDescription,  negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
			Set<OWLIndividual> coveredNegatives = rs.hasType(startDescription, negativeExamples);
			Set<OWLIndividual> coveredPositives = rs.hasType(startDescription, positiveExamples);
			startNode.setCoveredExamples(coveredPositives, coveredNegatives);
		}

		candidates.add(startNode);
		candidatesStable.add(startNode);

		ExampleBasedNode bestNode = startNode;
		ExampleBasedNode bestNodeStable = startNode;

		logger.info("starting top down refinement with: " + OWLAPIRenderers.toManchesterOWLSyntax(startNode.getConcept()) + " (" + df.format(100*startNode.getAccuracy(nrOfPositiveExamples, nrOfNegativeExamples)) + "% accuracy)");
		
		int loop = 0;

		algorithmStartTime = System.nanoTime();
		long lastPrintTime = 0;
		long lastTreeTraversalTime = System.nanoTime();
		long lastReductionTime = System.nanoTime();
		// try a traversal after x seconds
		long traversalInterval = 300l * 1000000000l;
		long reductionInterval = 300l * 1000000000l;
		long currentTime;
		
		while (!isTerminationCriteriaReached()) {
		//while ((!solutionFound || !configurator.getTerminateOnNoiseReached() ) && !stop) {

			// print statistics at most once a second
			currentTime = System.nanoTime();
			if (currentTime - lastPrintTime > 3000000000l) {
				printStatistics(false);
				lastPrintTime = currentTime;
				logger.debug("--- loop " + loop + " started ---");
//				logger.info("used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);
//				logger.info("test: " + ExampleBasedNode.exampleMemoryCounter/1024);
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

			// System.out.println("next expanded: " +
			// candidates.last().getShortDescription(nrOfPositiveExamples,
			// nrOfNegativeExamples, baseURI));

			// we record when a more accurate node is found and log it
			if (bestNodeStable.getCovPosMinusCovNeg() < candidatesStable.last()
					.getCovPosMinusCovNeg()) {
				String acc = new DecimalFormat( ".00%" ).format((candidatesStable.last().getAccuracy(nrOfPositiveExamples, nrOfNegativeExamples)));
				// no handling needed, it will just look ugly in the output
				logger.info("more accurate ("+acc+") class expression found: " + OWLAPIRenderers.toManchesterOWLSyntax(candidatesStable.last().getConcept()));
				if(logger.isTraceEnabled()){
					logger.trace(Helper.difference(positiveExamples,bestNodeStable.getCoveredNegatives()));
					logger.trace(Helper.difference(negativeExamples,bestNodeStable.getCoveredNegatives()));
				}
				printBestSolutions(5, false);
				printStatistics(false);
				bestNodeStable = candidatesStable.last();
			}

			// chose best node according to heuristics
			bestNode = candidates.last();
			// extend best node
			newCandidates.clear();
			// best node is removed temporarily, because extending it can
			// change its evaluation
			candidates.remove(bestNode);
			extendNodeProper(bestNode, bestNode.getHorizontalExpansion() + 1);
			candidates.add(bestNode);
			// newCandidates has been filled during node expansion
			candidates.addAll(newCandidates);
			candidatesStable.addAll(newCandidates);

			// System.out.println("done");

			if (writeSearchTree) {
				// String treeString = "";
				String treeString = "best node: " + bestNode + "\n";
				if (expandedNodes.size() > 1) {
					treeString += "all expanded nodes:\n";
					for (ExampleBasedNode n : expandedNodes) {
						treeString += "   " + n + "\n";
					}
				}
				expandedNodes.clear();
				treeString += startNode.getTreeString(nrOfPositiveExamples, nrOfNegativeExamples,
						baseURI, prefixes);
				treeString += "\n";

				if (replaceSearchTree)
					Files.createFile(searchTreeFile, treeString);
				else
					Files.appendToFile(searchTreeFile, treeString);
			}

			// Anzahl Schleifendurchläufe
			loop++;
		}// end while
		
		if (solutions.size()>0) {
		//if (solutionFound) {
			int solutionLimit = 20;
			// we do not need to print the best node if we display the top 20 solutions below anyway
//			logger.info("best node "
//					+ candidatesStable.last().getShortDescription(nrOfPositiveExamples,
//							nrOfNegativeExamples, baseURI));
			logger.info("solutions (at most " + solutionLimit + " are shown):");
			int show = 1;
			String manchester = "MANCHESTER:\n";
			for (ExampleBasedNode c : solutions) {
				logger.info(show + ": " + OWLAPIRenderers.toManchesterOWLSyntax(c.getConcept()) 
						+ " (accuracy " + df.format(100*c.getAccuracy(nrOfPositiveExamples, nrOfNegativeExamples)) + "%, length " 
						+ OWLClassExpressionUtils.getLength(c.getConcept())
						+ ", depth " + OWLClassExpressionUtils.getDepth(c.getConcept()) + ")");
//				manchester += show + ": " + c.toManchesterSyntaxString(baseURI, prefixes) + "\n";
				if (show >= solutionLimit) {
					break;
				}
				show++;
			}
			logger.debug(manchester);
		} else {
			logger.info("no appropriate solutions found (try increasing the noisePercentage parameter to what was reported as most accurate expression found above)");
		}

		logger.debug("size of candidate set: " + candidates.size());
		boolean showOrderedSolutions = false;
		printBestSolutions(20, showOrderedSolutions);

		printStatistics(true);

		int conceptTests = conceptTestsReasoner + conceptTestsTooWeakList + conceptTestsOverlyGeneralList;
		if (stop) {
			logger.info("Algorithm stopped ("+conceptTests+" descriptions tested).\n");
		} else {
			logger.info("Algorithm terminated successfully ("+conceptTests+" descriptions tested).\n");
            logger.info(rs.toString());
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

	// for all refinements of concept up to max length, we check whether they
	// are proper
	// and call the method recursively if not
	// recDepth is used only for statistics
	private void extendNodeProper(ExampleBasedNode node, OWLClassExpression concept, int maxLength,
			int recDepth) {

//		System.out.println("node: " + node);
//		System.out.println("concept: " + concept);
//		System.out.println("max length: " + maxLength);
		
		// do not execute methods if algorithm has been stopped (this means that
		// the algorithm
		// will terminate without further reasoning queries)
		if (stop)
			return;

		if (recDepth > maxRecDepth)
			maxRecDepth = recDepth;

		// compute refinements => we must not delete refinements with low
		// horizontal expansion,
		// because they are used in recursive calls of this method later on
		long refinementCalcTimeNsStart = System.nanoTime();
		Set<OWLClassExpression> refinements = operator.refine(concept, maxLength, null);
		refinementCalcTimeNs += System.nanoTime() - refinementCalcTimeNsStart;

		if (refinements.size() > maxNrOfRefinements)
			maxNrOfRefinements = refinements.size();

		long childConceptsDeletionTimeNsStart = System.nanoTime();
		// entferne aus den refinements alle Konzepte, die bereits Kinder des
		// Knotens sind
		// for(Node n : node.getChildren()) {
		// refinements.remove(n.getConcept());
		// }

		// das ist viel schneller, allerdings bekommt man ein anderes candidate
		// set(??)
		refinements.removeAll(node.getChildConcepts());

		childConceptsDeletionTimeNs += System.nanoTime() - childConceptsDeletionTimeNsStart;

		// if(refinements.size()<30) {
		// // System.out.println("refinements: " + refinements);
		// for(OWLClassExpression refinement: refinements)
		// System.out.println("refinement: " + refinement);
		// }

		long evaluateSetCreationTimeNsStart = System.nanoTime();

		// alle Konzepte, die länger als horizontal expansion sind, müssen
		// ausgewertet
		// werden
		TreeSet<OWLClassExpression> toEvaluateConcepts = new TreeSet<OWLClassExpression>();
		Iterator<OWLClassExpression> it = refinements.iterator();
		// for(Concept refinement : refinements) {
		while (it.hasNext()) {

			OWLClassExpression refinement = it.next();
			if (OWLClassExpressionUtils.getLength(refinement) > node.getHorizontalExpansion()) {
				// sagt aus, ob festgestellt wurde, ob refinement proper ist
				// (sagt nicht aus, dass das refinement proper ist!)
				boolean propernessDetected = false;

				// 1. short concept construction
				if (useShortConceptConstruction) {
					// kurzes Konzept konstruieren
					OWLClassExpression shortConcept = ConceptTransformation.getShortConcept(refinement);
					int n = shortConcept.compareTo(concept);
					
					// Konzepte sind gleich also Refinement improper
					if (n == 0) {
						propernessTestsAvoidedByShortConceptConstruction++;
						propernessDetected = true;

//						 System.out.println("refinement " + refinement + 
//								 " can be shortened");
//						 System.exit(0);
					}
				}
				
				// 2. too weak test
				if (!propernessDetected && useTooWeakList) {
					if (refinement instanceof OWLObjectIntersectionOf) {
						boolean tooWeakElement = containsTooWeakElement((OWLObjectIntersectionOf) refinement);
						if (tooWeakElement) {
							propernessTestsAvoidedByTooWeakList++;
							conceptTestsTooWeakList++;
							propernessDetected = true;
							// tooWeakList.add(refinement);

							// Knoten wird direkt erzeugt (es ist buganfällig
							// zwei Plätze
							// zu haben, an denen Knoten erzeugt werden, aber es
							// erscheint
							// hier am sinnvollsten)
							properRefinements.add(refinement);
							tooWeakList.add(refinement);

							ExampleBasedNode newNode = new ExampleBasedNode(refinement, negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
							newNode.setHorizontalExpansion(OWLClassExpressionUtils.getLength(refinement) - 1);
							newNode.setTooWeak(true);
							newNode
									.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.TOO_WEAK_LIST);
							node.addChild(newNode);

							// Refinement muss gelöscht werden, da es proper ist
							it.remove();
						}
					}
				}

				// properness konnte nicht vorher ermittelt werden
				if (!propernessDetected) {
					toEvaluateConcepts.add(refinement);
					// if(!res) {
					// System.out.println("already in: " + refinement);
					// Comparator comp = toEvaluateConcepts.comparator();
					// for(OWLClassExpression d : toEvaluateConcepts) {
					// if(comp.compare(d,refinement)==0)
					// System.out.println("see: " + d);
					// }
					// }
				}

			}

			// System.out.println("handled " + refinement + " length: " +
			// refinement.getLength() + " (new size: " +
			// toEvaluateConcepts.size() + ")");

		}
		evaluateSetCreationTimeNs += System.nanoTime() - evaluateSetCreationTimeNsStart;

		// System.out.println("intermediate 1 " +
		// node.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples,
		// baseURI));

		// System.out.println(toEvaluateConcepts.size());

		Set<OWLClassExpression> improperConcepts = null;
		if (toEvaluateConcepts.size() > 0) {
			// Test aller Konzepte auf properness (mit DIG in nur einer Anfrage)
			if (usePropernessChecks) {
				long propCalcReasoningStart = System.nanoTime();
				improperConcepts = rs.isSuperClassOf(toEvaluateConcepts, concept);
				
				propernessTestsReasoner += toEvaluateConcepts.size();
				// boolean isProper =
				// !learningProblem.getReasonerComponent().subsumes(refinement,
				// concept);
				propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart;
			}
		}

		// if(toEvaluateConcepts.size()<10)
		// System.out.println("to evaluate: " + toEvaluateConcepts);
		// else
		// System.out.println("to evaluate: more than 10");

		long improperConceptsRemovalTimeNsStart = System.nanoTime();
		// die improper Konzepte werden von den auszuwertenden gelöscht, d.h.
		// alle proper concepts bleiben übrig (einfache Umbenennung)
		if (improperConcepts != null)
			toEvaluateConcepts.removeAll(improperConcepts);
		Set<OWLClassExpression> properConcepts = toEvaluateConcepts;
		// alle proper concepts von refinements löschen
		refinements.removeAll(properConcepts);
		improperConceptsRemovalTimeNs += System.nanoTime() - improperConceptsRemovalTimeNsStart;

		// if(refinements.size()<10)
		// System.out.println("refinements: " + refinements);
		// else
		// System.out.println("refinements: more than 10");
		//		
		// System.out.println("improper concepts: " + improperConcepts);

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
				ExampleBasedNode newNode = new ExampleBasedNode(refinement, negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
				// die -1 ist wichtig, da sonst keine gleich langen Refinements
				// für den neuen Knoten erlaubt wären z.B. person => male
				newNode.setHorizontalExpansion(OWLClassExpressionUtils.getLength(refinement) - 1);

				boolean qualityKnown = false;
				int quality = -2;

				// overly general list verwenden
				if (useOverlyGeneralList && refinement instanceof OWLObjectUnionOf) {
					if (containsOverlyGeneralElement((OWLObjectUnionOf) refinement)) {
						conceptTestsOverlyGeneralList++;
						// quality = getNumberOfNegatives();
						quality = nrOfNegativeExamples;
						qualityKnown = true;
						newNode
								.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.OVERLY_GENERAL_LIST);
						newNode.setCoveredExamples(positiveExamples, negativeExamples);
					}

				}

				// Qualität des Knotens auswerten
				if (!qualityKnown) {
					long propCalcReasoningStart2 = System.nanoTime();
					conceptTestsReasoner++;

					// quality = coveredNegativesOrTooWeak(refinement);

					// determine individuals which have not been covered yet
					// (more efficient than full retrieval)
					Set<OWLIndividual> coveredPositives = node.getCoveredPositives();
					Set<OWLIndividual> newlyCoveredPositives = new HashSet<OWLIndividual>();

					// calculate how many pos. examples are not covered by the
					// parent node of the refinement
					int misclassifiedPositives = nrOfPositiveExamples - coveredPositives.size();

					// iterate through all covered examples (examples which are
					// not
					// covered do not need to be tested, because they remain
					// uncovered);
					// DIG will be slow if we send each reasoner request
					// individually
					// (however if we send everything in one request, too many
					// instance checks
					// are performed => rely on fast instance checker)
					for (OWLIndividual i : coveredPositives) {
						// TODO: move code to a separate function
						if (quality != -1) {
							boolean covered = rs.hasType(refinement, i);
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
						newlyCoveredNegatives = new HashSet<OWLIndividual>();

						for (OWLIndividual i : coveredNegatives) {
							boolean covered = rs.hasType(refinement, i);
							if (covered)
								newlyCoveredNegatives.add(i);
						}
					}

					propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart2;
					newNode
							.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.REASONER);
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

					newCandidates.add(newNode);

					// we need to make sure that all positives are covered
					// before adding something to the overly general list
					if ((newNode.getCoveredPositives().size() == nrOfPositiveExamples)
							&& quality == nrOfNegativeExamples)
						overlyGeneralList.add(refinement);

				}

				// System.out.println(newNode.getConcept() + " " + quality);
				node.addChild(newNode);
				
				// it is often useful to continue expanding until a longer node is
				// reached (to replace atomic concepts with more specific ones)
				if(forceRefinementLengthIncrease && !newNode.isTooWeak()) {
					// extend node again if its concept has the same length 
					if(OWLClassExpressionUtils.getLength(node.getConcept()) == OWLClassExpressionUtils.getLength(newNode.getConcept())) {
						extendNodeProper(newNode, refinement, maxLength, recDepth + 1);
					}
				}				
				
			}
		}

		// es sind jetzt noch alle Konzepte übrig, die improper refinements sind
		// auf jedem dieser Konzepte wird die Funktion erneut aufgerufen, da
		// sich
		// proper refinements ergeben könnten
		for (OWLClassExpression refinement : refinements) {
//			 for(int i=0; i<=recDepth; i++)
//			 System.out.print(" ");
//			 System.out.println("call: " + refinement + " [maxLength " +
//			 maxLength + ", rec depth " + recDepth + "]");

			// check for redundancy (otherwise we may run into very
			// time-intensive loops,
			// see planned JUnit test case $x)

			long redundancyCheckTimeNsStart = System.nanoTime();
			boolean redundant = properRefinements.contains(refinement);
			redundancyCheckTimeNs += System.nanoTime() - redundancyCheckTimeNsStart;

			if (!redundant) {
//				System.out.println("node " + node);
//				System.out.println("refinement " + refinement);
				extendNodeProper(node, refinement, maxLength, recDepth + 1);
			}
			
			// for(int i=0; i<=recDepth; i++)
			// System.out.print(" ");
			// System.out.println("finished: " + refinement + " [maxLength " +
			// maxLength + "]");
			// System.exit(0);
		}
	}

	
	
	private void printStatistics(boolean finalStats) {
		// TODO: viele Tests haben ergeben, dass man nie 100% mit der
		// Zeitmessung abdecken
		// kann (zum einen weil Stringausgabe verzögert erfolgt und zum anderen
		// weil
		// Funktionsaufrufe, garbage collection, Zeitmessung selbst auch Zeit
		// benötigt);
		// es empfiehlt sich folgendes Vorgehen:
		// - Messung der Zeit eines Loops im Algorithmus
		// - Messung der Zeit für alle node extensions innerhalb eines Loops
		// => als Normalisierungsbasis empfehlen sich dann die Loopzeit statt
		// Algorithmuslaufzeit
		// ... momentan kann es aber auch erstmal so lassen

		long algorithmRuntime = System.nanoTime() - algorithmStartTime;

		if (!finalStats) {

			ExampleBasedNode bestNode = candidatesStable.last();
			// double accuracy = 100 * ((bestNode.getCoveredPositives().size()
			// + nrOfNegativeExamples -
			// bestNode.getCoveredNegatives().size())/(double)nrOfExamples);
			// Refinementoperator auf Konzept anwenden
			// String bestNodeString = "currently best node: " + bestNode + "
			// accuracy: " + df.format(accuracy) + "%";
			logger.debug("start node: "
					+ startNode.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples,
							baseURI, prefixes));
			String bestNodeString = "currently best node: "
					+ bestNode.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples,
							baseURI, prefixes);

			// searchTree += bestNodeString + "\n";
			logger.debug(bestNodeString);
			logger.trace(bestNode.getStats(nrOfPositiveExamples, nrOfNegativeExamples));
			if (bestNode.getCoveredNegatives().size() <= 5)
				logger.trace("covered negs: " + bestNode.getCoveredNegatives());
			String expandedNodeString = "next expanded node: "
					+ candidates.last().getShortDescription(nrOfPositiveExamples,
							nrOfNegativeExamples, baseURI, prefixes);
			// searchTree += expandedNodeString + "\n";
			logger.debug(expandedNodeString);
			logger.debug("algorithm runtime " + Helper.prettyPrintNanoSeconds(algorithmRuntime));
			logger.debug("size of candidate set: " + candidates.size());
			// System.out.println("properness max recursion depth: " +
			// maxRecDepth);
			// System.out.println("max. number of one-step refinements: " +
			// maxNrOfRefinements);
			// System.out.println("max. number of children of a node: " +
			// maxNrOfChildren);
			logger.debug("subsumption time: "
					+ Helper.prettyPrintNanoSeconds(rs.getSubsumptionReasoningTimeNs()));
			logger.debug("instance check time: "
					+ Helper.prettyPrintNanoSeconds(rs.getInstanceCheckReasoningTimeNs()));
			logger.debug("retrieval time: "
					+ Helper.prettyPrintNanoSeconds(rs.getRetrievalReasoningTimeNs()));
		}

		if (computeBenchmarkInformation) {

			long reasoningTime = rs.getOverallReasoningTimeNs();
			double reasoningPercentage = 100 * reasoningTime / (double) algorithmRuntime;
			long propWithoutReasoning = propernessCalcTimeNs - propernessCalcReasoningTimeNs;
			double propPercentage = 100 * propWithoutReasoning / (double) algorithmRuntime;
			double deletionPercentage = 100 * childConceptsDeletionTimeNs
					/ (double) algorithmRuntime;
			long subTime = rs.getSubsumptionReasoningTimeNs();
			double subPercentage = 100 * subTime / (double) algorithmRuntime;
			double refinementPercentage = 100 * refinementCalcTimeNs / (double) algorithmRuntime;
			double redundancyCheckPercentage = 100 * redundancyCheckTimeNs
					/ (double) algorithmRuntime;
			double evaluateSetCreationTimePercentage = 100 * evaluateSetCreationTimeNs
					/ (double) algorithmRuntime;
			double improperConceptsRemovalTimePercentage = 100 * improperConceptsRemovalTimeNs
					/ (double) algorithmRuntime;
			double mComputationTimePercentage = 100 * operator.mComputationTimeNs
					/ (double) algorithmRuntime;
			double topComputationTimePercentage = 100 * operator.topComputationTimeNs
					/ (double) algorithmRuntime;
			double cleanTimePercentage = 100 * ConceptTransformation.cleaningTimeNs
					/ (double) algorithmRuntime;
			double onnfTimePercentage = 100 * ConceptTransformation.onnfTimeNs
					/ (double) algorithmRuntime;
			double shorteningTimePercentage = 100 * ConceptTransformation.shorteningTimeNs
					/ (double) algorithmRuntime;

			logger.debug("reasoning percentage: " + df.format(reasoningPercentage) + "%");
			logger.debug("   subsumption check time: " + df.format(subPercentage) + "%");
			logger.debug("proper calculation percentage (wo. reasoning): "
					+ df.format(propPercentage) + "%");
			logger.debug("   deletion time percentage: " + df.format(deletionPercentage) + "%");
			logger.debug("   refinement calculation percentage: " + df.format(refinementPercentage)
					+ "%");
			logger.debug("      m calculation percentage: " + df.format(mComputationTimePercentage)
					+ "%");
			logger.debug("      top calculation percentage: "
					+ df.format(topComputationTimePercentage) + "%");
			logger.debug("   redundancy check percentage: " + df.format(redundancyCheckPercentage)
					+ "%");
			logger.debug("   evaluate set creation time percentage: "
					+ df.format(evaluateSetCreationTimePercentage) + "%");
			logger.debug("   improper concepts removal time percentage: "
					+ df.format(improperConceptsRemovalTimePercentage) + "%");
			logger.debug("clean time percentage: " + df.format(cleanTimePercentage) + "%");
			logger.debug("onnf time percentage: " + df.format(onnfTimePercentage) + "%");
			logger
					.debug("shortening time percentage: " + df.format(shorteningTimePercentage)
							+ "%");
		}

		logger.debug("properness tests (reasoner/short concept/too weak list): "
				+ propernessTestsReasoner + "/" + propernessTestsAvoidedByShortConceptConstruction
				+ "/" + propernessTestsAvoidedByTooWeakList);
		logger
				.debug("concept tests (reasoner/too weak list/overly general list/redundant concepts): "
						+ conceptTestsReasoner
						+ "/"
						+ conceptTestsTooWeakList
						+ "/"
						+ conceptTestsOverlyGeneralList + "/" + redundantConcepts);
	}

	// @SuppressWarnings({"unused"})
	// private int coveredNegativesOrTooWeak(OWLClassExpression concept) {
	// if(posOnly)
	// return
	// posOnlyLearningProblem.coveredPseudoNegativeExamplesOrTooWeak(concept);
	// else
	// return learningProblem.coveredNegativeExamplesOrTooWeak(concept);
	// }

	// private int getNumberOfNegatives() {
	// if(posOnly)
	// return posOnlyLearningProblem.getPseudoNegatives().size();
	// else
	// return learningProblem.getNegativeExamples().size();
	// }

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
		// ExampleBasedNode startNode = candidatesStable.last();
		ExampleBasedNode startNode = findBestTraversalStartNode();
		OWLClassExpression currentDescription = startNode.getConcept();
		Set<OWLIndividual> currentCoveredPos = startNode.getCoveredPositives();
		Set<OWLIndividual> currentCoveredNeg = startNode.getCoveredNegatives();
		double currentAccuracy = startNode.getAccuracy(nrOfPositiveExamples, nrOfNegativeExamples);
		int currentMisclassifications = nrOfPositiveExamples - currentCoveredPos.size()
				+ currentCoveredNeg.size();
		logger.debug("tree traversal start node "
				+ startNode
						.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI, prefixes));
		logger.debug("tree traversal start accuracy: " + currentAccuracy);
		int i = 0;
		// start from the most promising nodes
		NavigableSet<ExampleBasedNode> reverseView = candidatesStable.descendingSet();
		for (ExampleBasedNode currNode : reverseView) {
			// compute covered positives and negatives
			SortedSet<OWLIndividual> newCoveredPositives = new TreeSet<OWLIndividual>(currentCoveredPos);
			newCoveredPositives.retainAll(currNode.getCoveredPositives());
			SortedSet<OWLIndividual> newCoveredNegatives = new TreeSet<OWLIndividual>(currentCoveredNeg);
			newCoveredNegatives.retainAll(currNode.getCoveredNegatives());

			// compute the accuracy we would get by adding this node
			double accuracy = (newCoveredPositives.size() + nrOfNegativeExamples - newCoveredNegatives
					.size())
					/ (double) (nrOfPositiveExamples + nrOfNegativeExamples);
			int misclassifications = nrOfPositiveExamples - newCoveredPositives.size()
					+ newCoveredNegatives.size();
			int misclassifiedPositives = nrOfPositiveExamples - newCoveredPositives.size();

			int lostPositives = currentCoveredPos.size() - newCoveredPositives.size();

			// TODO: maybe we should also consider a minimum improvement when
			// adding something
			// otherwise we could overfit
			// we give double weith to lost positives, i.e. when one positive is
			// lost at least
			// two negatives need to be uncovered
			boolean consider = (misclassifications + lostPositives < currentMisclassifications)
					&& (misclassifiedPositives <= allowedMisclassifications);
			// boolean consider = (misclassifications <
			// currentMisclassifications) &&
			// (misclassifiedPositives <= allowedMisclassifications);

			// concept has been chosen, so construct it
			if (consider) {

				// construct a new concept as intersection of both
				OWLClassExpression mc = dataFactory.getOWLObjectIntersectionOf(currentDescription, currNode.getConcept());

				mc = ConceptTransformation.cleanConceptNonRecursive(mc);
				mc = mc.getNNF();

				// System.out.println("extended concept to: " + mc);
				logger.debug("misclassifications: " + misclassifications);
				logger.debug("misclassified positives: " + misclassifiedPositives);
				logger.debug("accuracy: " + accuracy);

				// update variables
				currentDescription = mc;
				currentCoveredPos = newCoveredPositives;
				currentCoveredNeg = newCoveredNegatives;
				currentMisclassifications = misclassifications;
				currentAccuracy = accuracy;

				if (accuracy > 1 - noise) {
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
		NavigableSet<ExampleBasedNode> reverseView = candidatesStable.descendingSet();
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
		Iterator<ExampleBasedNode> it = candidatesStable.descendingIterator();
		Set<ExampleBasedNode> promisingNodes = new HashSet<ExampleBasedNode>();
		int i = 0;
		while (it.hasNext() && promisingNodes.size() < candidatePostReductionSize) {
			ExampleBasedNode node = it.next();
			// System.out.println(node.getShortDescription(nrOfPositiveExamples,
			// nrOfNegativeExamples, baseURI));
			// first criterion: the considered node should have an accuracy gain
			// over its parent
			// (avoids to use only the most promising node + all its refinements
			// with equal accuracy)
			boolean hasAccuracyGain = (node.getParent() == null)
					|| (node.getCoveredPositives().size() != node.getParent().getCoveredPositives()
							.size())
					|| (node.getCoveredNegatives().size() != node.getParent().getCoveredNegatives()
							.size());
			// second criterion: uncovered positives; it does not make much
			// sense to pick nodes with
			// low potential for reaching a solution (already at the limit of
			// misclassified positives)
			int misclassifiedPositives = nrOfPositiveExamples - node.getCoveredPositives().size();
			boolean hasRefinementPotential = (misclassifiedPositives <= Math
					.floor(0.65d * allowedMisclassifications));
			boolean keep = hasAccuracyGain && hasRefinementPotential;
			if (keep) {
				promisingNodes.add(node);
			}
			i++;
		}
		candidates.retainAll(promisingNodes);
		logger.debug("searched " + i + " nodes and picked the following promising descriptions:");
		for (ExampleBasedNode node : promisingNodes)
			logger.debug(node.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples,
					baseURI, prefixes));
	}

	/*
	 * private Set<OWLIndividual> computeQuality(OWLClassExpression refinement, Set<OWLIndividual>
	 * coveredPositives) { Set<OWLIndividual> ret = new TreeSet<OWLIndividual>();
	 * int misclassifications; for(OWLIndividual i : coveredPositives) { boolean
	 * covered = rs.instanceCheck(refinement, i); if(!covered)
	 * misclassifications++; else ret.add(i);
	 * 
	 * if(misclassifications > allowedMisclassifications) return null; } }
	 */

	public void stop() {
		stop = true;
	}

	public OWLClassExpression getBestSolution() {
		return candidatesStable.last().getConcept();
	}

	public List<OWLClassExpression> getCurrentlyBestDescriptions() {
		List<OWLClassExpression> best = new LinkedList<OWLClassExpression>();
		int i = 0;
		int nrOfSolutions = 200;
		for (ExampleBasedNode n : candidatesStable.descendingSet()) {
			best.add(n.getConcept());
			if (i == nrOfSolutions)
				return best;
			i++;
		}
		return best;
	}
	
	public TreeSet<EvaluatedDescriptionPosNeg> getCurrentlyBestEvaluatedDescriptions() {
		Iterator<ExampleBasedNode> it = candidatesStable.descendingIterator();
		int count = 0;
		TreeSet<EvaluatedDescriptionPosNeg> cbd = new TreeSet<EvaluatedDescriptionPosNeg>(edComparator);
		while(it.hasNext()) {
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
	

	public void printBestSolutions(int nrOfSolutions, boolean showOrderedSolutions) {
		// QUALITY: could be optimized
		if (!logger.isTraceEnabled()) {
			return;
		}
		// if(!logger.getLevel().toString().equalsIgnoreCase("TRACE"))return;
		if (nrOfSolutions == 0)
			nrOfSolutions = candidatesStable.size();
		int i = 0;
		for (ExampleBasedNode n : candidatesStable.descendingSet()) {
			if (n.getAccuracy(nrOfPositiveExamples, nrOfNegativeExamples) < 1)
				break;
			logger.trace("best: "
					+ n.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI, prefixes));
			if (i == nrOfSolutions)
				break;
			i++;
		}

		if (showOrderedSolutions) {
			logger.trace("ordered by generality (most special solutions first):");
			SubsumptionComparator sc = new SubsumptionComparator(rs);
			SortedSet<OWLClassExpression> solutionsOrderedBySubsumption = new TreeSet<OWLClassExpression>(sc);
//			solutionsOrderedBySubsumption.addAll(solutions);
			for (OWLClassExpression d : solutionsOrderedBySubsumption)
				logger.trace("special: " + d);
			throw new Error("implementation needs to be updated to show ordered solutions");			
		}
		/*
		 * for (int j = 0; j < solutions.size(); j++) { OWLClassExpression d =
		 * solutions.get(j); logger.trace(d.toString()); }
		 */

	}

	public ScorePosNeg getSolutionScore() {
		return (ScorePosNeg) learningProblem.computeScore(getBestSolution());
	}

	private ScorePosNeg getScore(OWLClassExpression d) {
		return (ScorePosNeg) learningProblem.computeScore(d);
	}

	public ExampleBasedNode getStartNode() {
		return startNode;
	}
	
	/**
	 * In this function it is calculated whether the algorithm should stop.
	 * This is not always depends whether an actual solution was found
	 * The algorithm stops if:
	 * 1. the object attribute stop is set to true (possibly by an outside source)
	 * 2. the maximimum execution time is reached
	 * 3. the maximum number of class OWLClassExpression tests is reached
	 *
	 * Continuation criteria and result improvement
	 * The algorithm continues (although it would normally stop) if
	 * 1. Minimum execution time is not reached (default 0)
	 * 2. not enough good solutions are found (default 1)
	 * otherwise it stops
	 * 
	 * @return true if the algorithm should stop, this is mostly indepent of the question if a solution was found
	 */
	private boolean isTerminationCriteriaReached(){
		if(this.stop){
			return true;
		}
//		System.out.println("ssssss");
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
		if(maxClassDescriptionTests == 0) 
			result = false;
		//test
		else if(conceptTests >= maxClassDescriptionTests){
			logger.info("Maximum Class OWLClassExpression tests (" + maxClassDescriptionTests
					+ " tests [actual: "+conceptTests+"]) reached, stopping now...");
			return true;
		}
		
		// we stop if sufficiently many solutions (concepts fitting the noise parameter) have been 
		// reached - unless this termination criterion is switched off using terminateOnNoiseReached = false
		if (guaranteeXgoodAlreadyReached){
			result = true;
		} else if(solutions.size() >= guaranteeXgoodDescriptions && terminateOnNoiseReached) {
				if(guaranteeXgoodDescriptions != 1) {
					logger.info("Minimum number (" + guaranteeXgoodDescriptions
							+ ") of good descriptions reached.");
					
					guaranteeXgoodAlreadyReached = true;
					result = true;
				}
		}
		
				
		if (minExecutionTimeAlreadyReached){
			result = result && true;
		}else if(minMilliSeconds < totalTimeNeeded) {
			if(minExecutionTimeInSeconds != 0) {
				logger.info("Minimum time (" + minExecutionTimeInSeconds
						+ " seconds) reached.");
			}
			minExecutionTimeAlreadyReached = true;
			result = result && true;
		}else {
			result = false;
		} 
		
		return result;
	
	}
	
	public boolean isRunning() {
		return isRunning;
	}
}
