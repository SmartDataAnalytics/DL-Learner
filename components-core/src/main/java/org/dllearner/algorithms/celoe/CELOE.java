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
package org.dllearner.algorithms.celoe;

import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.*;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.refinementoperators.*;
import org.dllearner.utilities.*;
import org.dllearner.utilities.datastructures.SearchTree;
import org.dllearner.utilities.owl.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The CELOE (Class Expression Learner for Ontology Engineering) algorithm.
 * It adapts and extends the standard supervised learning algorithm for the
 * ontology engineering use case.
 * 
 * @author Jens Lehmann
 *
 */
@SuppressWarnings("CloneDoesntCallSuperClone")
@ComponentAnn(name="CELOE", shortName="celoe", version=1.0, description="CELOE is an adapted and extended version of the OCEL algorithm applied for the ontology engineering use case. See http://jens-lehmann.org/files/2011/celoe.pdf for reference.")
public class CELOE extends AbstractCELA implements Cloneable{

	private static final Logger logger = LoggerFactory.getLogger(CELOE.class);
	private static final Marker sparql_debug = MarkerFactory.getMarker("SD");
	
	private boolean isRunning = false;
	private boolean stop = false;
	
//	private OEHeuristicStable heuristicStable = new OEHeuristicStable();
//	private OEHeuristicRuntime heuristicRuntime = new OEHeuristicRuntime();
	
	@ConfigOption(description = "the refinement operator instance to use")
	private LengthLimitedRefinementOperator operator;

	private SearchTree<OENode> searchTree;
	@ConfigOption(defaultValue="celoe_heuristic")
	private AbstractHeuristic heuristic; // = new OEHeuristicRuntime();
	// the class with which we start the refinement process
	@ConfigOption(defaultValue = "owl:Thing",
			description = "You can specify a start class for the algorithm. To do this, you have to use Manchester OWL syntax either with full IRIs or prefixed IRIs.",
			exampleValue = "ex:Male or http://example.org/ontology/Female")
	private OWLClassExpression startClass;
	
	// all descriptions in the search tree plus those which were too weak (for fast redundancy check)
	private TreeSet<OWLClassExpression> descriptions;
	
	
	// if true, then each solution is evaluated exactly instead of approximately
	// private boolean exactBestDescriptionEvaluation = false;
	@ConfigOption(defaultValue="false", description="Use this if you are interested in only one suggestion and your learning problem has many (more than 1000) examples.")
	private boolean singleSuggestionMode;
	private OWLClassExpression bestDescription;
	private double bestAccuracy = Double.MIN_VALUE;
	
	private OWLClass classToDescribe;
	// examples are either 1.) instances of the class to describe 2.) positive examples
	// 3.) union of pos.+neg. examples depending on the learning problem at hand
	private Set<OWLIndividual> examples;
	
	// CELOE was originally created for learning classes in ontologies, but also
	// works for other learning problem types
	private boolean isClassLearningProblem;
	private boolean isEquivalenceProblem;
	
	// important parameters (non-config options but internal)
	private double noise;

	private boolean filterFollowsFromKB = false;
	
	// less important parameters
	// forces that one solution cannot be subexpression of another expression; this option is useful to get diversity
	// but it can also suppress quite useful expressions
	private boolean forceMutualDifference = false;
	
	// utility variables
	
	// statistical variables
	private int expressionTests = 0;
	private int minHorizExp = 1;
	private int maxHorizExp = 0;
	private long totalRuntimeNs = 0;
	
	// TODO: turn those into config options
	

	// important: do not initialise those with empty sets
	// null = no settings for allowance / ignorance
	// empty set = allow / ignore nothing (it is often not desired to allow no class!)
	@ConfigOption(defaultValue="false", description="specifies whether to write a search tree")
	private boolean writeSearchTree = false;

	@ConfigOption(defaultValue="log/searchTree.txt", description="file to use for the search tree")
	private String searchTreeFile = "log/searchTree.txt";

	@ConfigOption(defaultValue="false", description="specifies whether to replace the search tree in the log file after each run or append the new search tree")
	private boolean replaceSearchTree = false;
	
	@ConfigOption(defaultValue="10", description="Sets the maximum number of results one is interested in. (Setting this to a lower value may increase performance as the learning algorithm has to store/evaluate/beautify less descriptions).")
	private int maxNrOfResults = 10;

	@ConfigOption(defaultValue="0.0", description="the (approximated) percentage of noise within the examples")
	private double noisePercentage = 0.0;

	@ConfigOption(defaultValue="false", description="If true, then the results will not contain suggestions, which already follow logically from the knowledge base. Be careful, since this requires a potentially expensive consistency check for candidate solutions.")
	private boolean filterDescriptionsFollowingFromKB = false;

	@ConfigOption(defaultValue="false", description="If true, the algorithm tries to find a good starting point close to an existing definition/super class of the given class in the knowledge base.")
	private boolean reuseExistingDescription = false;

	@ConfigOption(defaultValue="0", description="The maximum number of candidate hypothesis the algorithm is allowed to test (0 = no limit). The algorithm will stop afterwards. (The real number of tests can be slightly higher, because this criterion usually won't be checked after each single test.)")
	private int maxClassExpressionTests = 0;

	@ConfigOption(defaultValue="0", description = "The maximum number of candidate hypothesis the algorithm is allowed after an improvement in accuracy (0 = no limit). The algorithm will stop afterwards. (The real number of tests can be slightly higher, because this criterion usually won't be checked after each single test.)")
	private int maxClassExpressionTestsAfterImprovement = 0;
	
	@ConfigOption(defaultValue = "0", description = "maximum execution of the algorithm in seconds after last improvement")
	private int maxExecutionTimeInSecondsAfterImprovement = 0;
	
	@ConfigOption(defaultValue="false", description="specifies whether to terminate when noise criterion is met")
	private boolean terminateOnNoiseReached = false;
	
	@ConfigOption(defaultValue="7", description="maximum depth of description")
	private double maxDepth = 7;

	@ConfigOption(defaultValue="false", description="algorithm will terminate immediately when a correct definition is found")
	private boolean stopOnFirstDefinition = false;
	
	private int expressionTestCountLastImprovement;

	OWLClassExpressionLengthMetric lengthMetric = OWLClassExpressionLengthMetric.getDefaultMetric();

	private TreeMap<OENode, Double> solutionCandidates;
	private final double solutionCandidatesMinAccuracyDiff = 0.0001;

	@ConfigOption(defaultValue = "0.0", description = "determines a lower bound on noisiness of an expression with respect to noisePercentage " +
		"in order to be considered a reasonable solution candidate (must be non-negative), e.g. for noisePercentage = 15 and noisePercentageMargin = 5, " +
		"the algorithm will suggest expressions with the number of misclassified positives less than or equal to 20% of all examples " +
		"as solution candidates as well; note: difference between accuracies of any two candidates must be at least 0.01% to ensure diversity")
	private double noisePercentageMargin = 0.0;

	@ConfigOption(defaultValue = "20", description = "the number of solution candidates within margin to be presented, sorted in descending order by accuracy")
	private int maxNrOfResultsWithinMargin = 20;

	private double noiseWithMargin;
	
	
	@SuppressWarnings("unused")
	private long timeLastImprovement = 0;
	@ConfigOption(defaultValue = "false",  description = "whether to try and refine solutions which already have accuracy value of 1")
	private boolean expandAccuracy100Nodes = false;
	private double currentHighestAccuracy;

	// option to keep track of best score during algorithm run
	private boolean keepTrackOfBestScore = false;
	private SortedMap<Long, Double> runtimeVsBestScore = new TreeMap<>();

	
	public CELOE() {}
	
	public CELOE(CELOE celoe){
		setReasoner(celoe.reasoner);
		setLearningProblem(celoe.learningProblem);
		
		setAllowedConcepts(celoe.getAllowedConcepts());
		setAllowedObjectProperties(celoe.getAllowedObjectProperties());
		setAllowedDataProperties(celoe.getAllowedDataProperties());
		
		setIgnoredConcepts(celoe.ignoredConcepts);
		setIgnoredObjectProperties(celoe.getIgnoredObjectProperties());
		setIgnoredDataProperties(celoe.getIgnoredDataProperties());
		
		setExpandAccuracy100Nodes(celoe.expandAccuracy100Nodes);
		setFilterDescriptionsFollowingFromKB(celoe.filterDescriptionsFollowingFromKB);
		setHeuristic(celoe.heuristic);
		
		setMaxClassExpressionTests(celoe.maxClassExpressionTests);
		setMaxClassExpressionTestsAfterImprovement(celoe.maxClassExpressionTestsAfterImprovement);
		setMaxDepth(celoe.maxDepth);
		setMaxExecutionTimeInSeconds(celoe.getMaxExecutionTimeInSeconds());
		setMaxExecutionTimeInSecondsAfterImprovement(celoe.maxExecutionTimeInSecondsAfterImprovement);
		setMaxNrOfResults(celoe.maxNrOfResults);
		setNoisePercentage(celoe.noisePercentage);
		
		LengthLimitedRefinementOperator op = new RhoDRDown((RhoDRDown)celoe.operator);
		try {
			op.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		setOperator(op);
		
		
		setReuseExistingDescription(celoe.reuseExistingDescription);
		setSingleSuggestionMode(celoe.singleSuggestionMode);
		setStartClass(celoe.startClass);
		setStopOnFirstDefinition(celoe.stopOnFirstDefinition);
		setTerminateOnNoiseReached(celoe.terminateOnNoiseReached);
		setUseMinimizer(celoe.isUseMinimizer());
		
		setWriteSearchTree(celoe.writeSearchTree);
		setReplaceSearchTree(celoe.replaceSearchTree);

		setMaxNrOfResultsWithinMargin(celoe.maxNrOfResultsWithinMargin);
		setNoisePercentageMargin(celoe.noisePercentageMargin);
	}
	
	public CELOE(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
	}

	public static Collection<Class<? extends AbstractClassExpressionLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractClassExpressionLearningProblem>> problems = new LinkedList<>();
		problems.add(AbstractClassExpressionLearningProblem.class);
		return problems;
	}
	
	@Override
	public void init() throws ComponentInitException {
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();
			
		if(maxExecutionTimeInSeconds != 0 && maxExecutionTimeInSecondsAfterImprovement != 0) {
			maxExecutionTimeInSeconds = Math.min(maxExecutionTimeInSeconds, maxExecutionTimeInSecondsAfterImprovement);
		}
		
		// TODO add comment
		ClassHierarchy classHierarchy = initClassHierarchy();
		ObjectPropertyHierarchy objectPropertyHierarchy = initObjectPropertyHierarchy();
		DatatypePropertyHierarchy datatypePropertyHierarchy = initDataPropertyHierarchy();

		// if no one injected a heuristic, we use a default one
		if(heuristic == null) {
			heuristic = new OEHeuristicRuntime();
			heuristic.init();
		}

		// TODO: MY copy from MultiHeuristic
//		if (heuristic instanceof OEHeuristicRuntime) {
//			((OEHeuristicRuntime) heuristic).setLengthMetric(lengthMetric);
//		}
		
		minimizer = new OWLClassExpressionMinimizer(dataFactory, reasoner);
		
		if (writeSearchTree) {
			File f = new File(searchTreeFile);
			if (f.getParentFile() != null) {
				f.getParentFile().mkdirs();
			}
			Files.clearFile(f);
		}
		
		// start at owl:Thing by default
		startClass = OWLAPIUtils.classExpressionPropertyExpanderChecked(this.startClass, reasoner, dataFactory, this::computeStartClass, logger);

		bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);
		
		isClassLearningProblem = (learningProblem instanceof ClassLearningProblem);
		
		// we put important parameters in class variables
		noise = noisePercentage/100d;

		// (filterFollowsFromKB is automatically set to false if the problem
		// is not a class learning problem
		filterFollowsFromKB = filterDescriptionsFollowingFromKB && isClassLearningProblem;
		
		// actions specific to ontology engineering
		if(isClassLearningProblem) {
			ClassLearningProblem problem = (ClassLearningProblem) learningProblem;
			classToDescribe = problem.getClassToDescribe();
			isEquivalenceProblem = problem.isEquivalenceProblem();
			
			examples = reasoner.getIndividuals(classToDescribe);
		} else if(learningProblem instanceof PosOnlyLP) {
			examples = ((PosOnlyLP)learningProblem).getPositiveExamples();
		} else if(learningProblem instanceof PosNegLP) {
			examples = Sets.union(((PosNegLP)learningProblem).getPositiveExamples(),((PosNegLP)learningProblem).getNegativeExamples());
		}
		
		// create a refinement operator and pass all configuration
		// variables to it
		if (operator == null) {
			// we use a default operator and inject the class hierarchy for now
			operator = new RhoDRDown();
			((CustomStartRefinementOperator) operator).setStartClass(startClass);
			((ReasoningBasedRefinementOperator) operator).setReasoner(reasoner);
		}
		if (operator instanceof CustomHierarchyRefinementOperator) {
			((CustomHierarchyRefinementOperator) operator).setClassHierarchy(classHierarchy);
			((CustomHierarchyRefinementOperator) operator).setObjectPropertyHierarchy(objectPropertyHierarchy);
			((CustomHierarchyRefinementOperator) operator).setDataPropertyHierarchy(datatypePropertyHierarchy);
		}
		
		if (!((AbstractRefinementOperator) operator).isInitialized())
			operator.init();

		operator.setLengthMetric(lengthMetric);

		AccuracyBasedComparator solutionComparator = new AccuracyBasedComparator(lengthMetric);
		solutionCandidates = new TreeMap<>(solutionComparator);

		if (noisePercentageMargin < 0) {
			noisePercentageMargin = 0.0;
		}

		noiseWithMargin = (noisePercentage + noisePercentageMargin) / 100.0;
		
		initialized = true;
	}
	
	@Override
	public void start() {
		stop = false;
		isRunning = true;
		reset();
		nanoStartTime = System.nanoTime();
		
		currentHighestAccuracy = 0.0;
		OENode nextNode;

		logger.info("Time " + getCurrentCpuMillis() / 1000.0 + "s");
		logger.info("start class:" + startClass);
		addNode(startClass, null);
		
		while (!terminationCriteriaSatisfied()) {
			showIfBetterSolutionsFound();

			// chose best node according to heuristics
			nextNode = getNextNodeToExpand();
			int horizExp = nextNode.getHorizontalExpansion();
			
			// apply refinement operator
			TreeSet<OWLClassExpression> refinements = refineNode(nextNode);
				
			while(!refinements.isEmpty() && !terminationCriteriaSatisfied()) {
				// pick element from set
				OWLClassExpression refinement = refinements.pollFirst();

				// get length of class expression
				int length = OWLClassExpressionUtils.getLength(refinement);
				
				// we ignore all refinements with lower length and too high depth
				// (this also avoids duplicate node children)
				if(length >= horizExp && OWLClassExpressionUtils.getDepth(refinement) <= maxDepth) {
					// add node to search tree
					addNode(refinement, nextNode);
				}
			}
			
			showIfBetterSolutionsFound();
			
			// update the global min and max horizontal expansion values
			updateMinMaxHorizExp(nextNode);
			
			// write the search tree (if configured)
			if (writeSearchTree) {
				writeSearchTree(refinements);
			}
		}
		
		if(singleSuggestionMode) {
			bestEvaluatedDescriptions.add(bestDescription, bestAccuracy, learningProblem);
		}
		
		// print some stats
		printAlgorithmRunStats();

		printSolutionCandidates();
		
		// print solution(s)
		logger.info("solutions:\n" + getSolutionString());

		if (learningProblem instanceof PosNegLP) {
			((PosNegLP) learningProblem).printTestEvaluation(bestEvaluatedDescriptions.getBest().getDescription());
		}

		printBestConceptsTimesAndAccuracies();
		
		isRunning = false;
	}
	
	/*
	 * Compute the start class in the search space from which the refinement will start.
	 * We use the intersection of super classes for definitions (since it needs to
	 * capture all instances), but owl:Thing for learning subclasses (since it is
	 * superfluous to add super classes in this case)
	 */
	private OWLClassExpression computeStartClass() {
		OWLClassExpression startClass = dataFactory.getOWLThing();
		
		if(isClassLearningProblem) {
			if(isEquivalenceProblem) {
				Set<OWLClassExpression> existingDefinitions = reasoner.getAssertedDefinitions(classToDescribe);
				if(reuseExistingDescription && (existingDefinitions.size() > 0)) {
					// the existing definition is reused, which in the simplest case means to
					// use it as a start class or, if it is already too specific, generalise it
					
					// pick the longest existing definition as candidate
					OWLClassExpression existingDefinition = null;
					int highestLength = 0;
					for(OWLClassExpression exDef : existingDefinitions) {
						if(OWLClassExpressionUtils.getLength(exDef) > highestLength) {
							existingDefinition = exDef;
							highestLength = OWLClassExpressionUtils.getLength(exDef);
						}
					}
					
					LinkedList<OWLClassExpression> startClassCandidates = new LinkedList<>();
					startClassCandidates.add(existingDefinition);
					// hack for RhoDRDown
					if(operator instanceof RhoDRDown) {
						((RhoDRDown)operator).setDropDisjuncts(true);
					}
					LengthLimitedRefinementOperator upwardOperator = new OperatorInverter(operator);
					
					// use upward refinement until we find an appropriate start class
					boolean startClassFound = false;
					OWLClassExpression candidate;
					do {
						candidate = startClassCandidates.pollFirst();
						if(((ClassLearningProblem)learningProblem).getRecall(candidate)<1.0) {
							// add upward refinements to list
							Set<OWLClassExpression> refinements = upwardOperator.refine(candidate, OWLClassExpressionUtils.getLength(candidate));
//							System.out.println("ref: " + refinements);
							LinkedList<OWLClassExpression> refinementList = new LinkedList<>(refinements);
//							Collections.reverse(refinementList);
//							System.out.println("list: " + refinementList);
							startClassCandidates.addAll(refinementList);
//							System.out.println("candidates: " + startClassCandidates);
						} else {
							startClassFound = true;
						}
					} while(!startClassFound);
					startClass = candidate;
					
					if(startClass.equals(existingDefinition)) {
						logger.info("Reusing existing class expression " + OWLAPIRenderers.toManchesterOWLSyntax(startClass) + " as start class for learning algorithm.");
					} else {
						logger.info("Generalised existing class expression " + OWLAPIRenderers.toManchesterOWLSyntax(existingDefinition) + " to " + OWLAPIRenderers.toManchesterOWLSyntax(startClass) + ", which is used as start class for the learning algorithm.");
					}
					
					if(operator instanceof RhoDRDown) {
						((RhoDRDown)operator).setDropDisjuncts(false);
					}
					
				} else {
					Set<OWLClassExpression> superClasses = reasoner.getClassHierarchy().getSuperClasses(classToDescribe, true);
					if(superClasses.size() > 1) {
						startClass = dataFactory.getOWLObjectIntersectionOf(superClasses);
					} else if(superClasses.size() == 1){
						startClass = (OWLClassExpression) superClasses.toArray()[0];
					} else {
						startClass = dataFactory.getOWLThing();
						logger.warn(classToDescribe + " is equivalent to owl:Thing. Usually, it is not " +
								"sensible to learn a class expression in this case.");
					}
				}
			}
		}
		return startClass;
	}
	
	private OENode getNextNodeToExpand() {
		// we expand the best node of those, which have not achieved 100% accuracy
		// already and have a horizontal expansion equal to their length
		// (rationale: further extension is likely to add irrelevant syntactical constructs)
		Iterator<OENode> it = searchTree.descendingIterator();
		if (logger.isTraceEnabled()) {
			for (OENode N:searchTree.getNodeSet()) {
				logger.trace(sparql_debug,"`getnext:"+N);
			}
		}

		while(it.hasNext()) {
			OENode node = it.next();
			logger.trace(sparql_debug,"``"+node+node.getAccuracy());
			if (isExpandAccuracy100Nodes() && node.getHorizontalExpansion() < OWLClassExpressionUtils.getLength(node.getDescription())) {
					return node;
			} else {
				if(node.getAccuracy() < 1.0 || node.getHorizontalExpansion() < OWLClassExpressionUtils.getLength(node.getDescription())) {
					return node;
				}
			}
		}
		
		// this should practically never be called, since for any reasonable learning
		// task, we will always have at least one node with less than 100% accuracy
		throw new RuntimeException("CELOE could not find any node with lesser accuracy.");
	}
	
	// expand node horizontically
	private TreeSet<OWLClassExpression> refineNode(OENode node) {
		logger.trace(sparql_debug,"REFINE NODE " + node);
		MonitorFactory.getTimeMonitor("refineNode").start();
		// we have to remove and add the node since its heuristic evaluation changes through the expansion
		// (you *must not* include any criteria in the heuristic which are modified outside of this method,
		// otherwise you may see rarely occurring but critical false ordering in the nodes set)
		searchTree.updatePrepare(node);
		int horizExp = node.getHorizontalExpansion();
		TreeSet<OWLClassExpression> refinements = (TreeSet<OWLClassExpression>) operator.refine(node.getDescription(), horizExp);
//		System.out.println("refinements: " + refinements);
		node.incHorizontalExpansion();
		node.setRefinementCount(refinements.size());
//		System.out.println("refined node: " + node);
		searchTree.updateDone(node);
		MonitorFactory.getTimeMonitor("refineNode").stop();
		return refinements;
	}

	/**
	 * Add node to search tree if it is not too weak.
	 * @return TRUE if node was added and FALSE otherwise
	 */
	private boolean addNode(OWLClassExpression description, OENode parentNode) {
		String sparql_debug_out = "";
		if (logger.isTraceEnabled()) sparql_debug_out = "DESC: " + description;
		MonitorFactory.getTimeMonitor("addNode").start();
		
		// redundancy check (return if redundant)
		boolean nonRedundant = descriptions.add(description);
		if(!nonRedundant) {
			logger.trace(sparql_debug, sparql_debug_out + "REDUNDANT");
			return false;
		}
		
		// check whether the class expression is allowed
		if(!isDescriptionAllowed(description, parentNode)) {
			logger.trace(sparql_debug, sparql_debug_out + "NOT ALLOWED");
			return false;
		}
		
		OENode node = createNode(parentNode, description);
		
		// issue a warning if accuracy is not between 0 and 1 or -1 (too weak)
		if(node.getAccuracy() > 1.0 || (node.getAccuracy() < 0.0 && node.getAccuracy() != -1)) {
			throw new RuntimeException("Invalid accuracy value " + node.getAccuracy() + " for class expression " + description +
					". This could be caused by a bug in the heuristic measure and should be reported to the DL-Learner bug tracker.");
		}
		
		expressionTests++;
		
		// return FALSE if 'too weak'
		if(node.getAccuracy() == -1) {
			return false;
		}

		searchTree.addNode(parentNode, node);
		
		// in some cases (e.g. mutation) fully evaluating even a single class expression is too expensive
		// due to the high number of examples -- so we just stick to the approximate accuracy
		if(singleSuggestionMode) {
			if(node.getAccuracy() > bestAccuracy) {
				bestAccuracy = node.getAccuracy();
				bestDescription = description;
				logger.info("more accurate (" + dfPercent.format(bestAccuracy) + ") class expression found: " + descriptionToString(bestDescription)); // + getTemporaryString(bestDescription));
			}
			return true;
		}
		
		// maybe add to best descriptions (method keeps set size fixed);
		// we need to make sure that this does not get called more often than
		// necessary since rewriting is expensive
		boolean isCandidate = !bestEvaluatedDescriptions.isFull();
		if(!isCandidate) {
			EvaluatedDescription<? extends Score> worst = bestEvaluatedDescriptions.getWorst();
			double accThreshold = worst.getAccuracy();
			isCandidate =
				(node.getAccuracy() > accThreshold ||
				(node.getAccuracy() >= accThreshold && OWLClassExpressionUtils.getLength(description) < worst.getDescriptionLength()));
		}
		
		if(isCandidate) {
			OWLClassExpression niceDescription = rewrite(node.getExpression());

			if(niceDescription.equals(classToDescribe)) {
				return false;
			}
			
			if(!isDescriptionAllowed(niceDescription, node)) {
				return false;
			}
			
			// another test: none of the other suggested descriptions should be
			// a subdescription of this one unless accuracy is different
			// => comment: on the one hand, this appears to be too strict, because once A is a solution then everything containing
			// A is not a candidate; on the other hand this suppresses many meaningless extensions of A
			boolean shorterDescriptionExists = false;
			if(forceMutualDifference) {
				for(EvaluatedDescription<? extends Score> ed : bestEvaluatedDescriptions.getSet()) {
					if(Math.abs(ed.getAccuracy()-node.getAccuracy()) <= 0.00001 && ConceptTransformation.isSubdescription(niceDescription, ed.getDescription())) {
//						System.out.println("shorter: " + ed.getDescription());
						shorterDescriptionExists = true;
						break;
					}
				}
			}
			
//			System.out.println("shorter description? " + shorterDescriptionExists + " nice: " + niceDescription);
			
			if(!shorterDescriptionExists) {
				if(!filterFollowsFromKB || !((ClassLearningProblem)learningProblem).followsFromKB(niceDescription)) {
//					System.out.println(node + "->" + niceDescription);

					if (learningProblem instanceof PosNegLPStandard) {
						EvaluatedDescription<? extends Score> ed = ((PosNegLPStandard) learningProblem).constructEvaluatedDescription(
							niceDescription, node.getCoveredPositiveExamples(), node.getCoveredNegativeExamples(), node.getAccuracy()
						);
						bestEvaluatedDescriptions.add(ed);
					} else {
						bestEvaluatedDescriptions.add(niceDescription, node.getAccuracy(), learningProblem);
					}

//					System.out.println("acc: " + accuracy);
//					System.out.println(bestEvaluatedDescriptions);
				}
			}
			
//			bestEvaluatedDescriptions.add(node.getDescription(), accuracy, learningProblem);
			
//			System.out.println(bestEvaluatedDescriptions.getSet().size());
		}

		if (node.getAccuracy() >= 1 - noiseWithMargin) {
			if (solutionCandidates.isEmpty()
				|| (node.getAccuracy() > solutionCandidates.firstKey().getAccuracy()
					&& solutionCandidates.keySet().stream().allMatch(
						n -> Math.abs(node.getAccuracy() - n.getAccuracy()) > solutionCandidatesMinAccuracyDiff
					)
				)
			) {
				solutionCandidates.put(node, getCurrentCpuMillis() / 1000.0);
			}

			if (solutionCandidates.size() > maxNrOfResultsWithinMargin) {
				solutionCandidates.pollFirstEntry();
			}
		}

		return true;
	}

	private OENode createNode(OENode parent, OWLClassExpression refinement) {
		if (!(learningProblem instanceof PosNegLPStandard)) {
			Monitor mon = MonitorFactory.start("lp");
			double accuracy = learningProblem.getAccuracyOrTooWeak(refinement, noise);
			mon.stop();

			return new OENode(refinement, accuracy);
		}

		PosNegLPStandard posNegLP = (PosNegLPStandard) learningProblem;

		Set<OWLIndividual> coveredPositives;
		Set<OWLIndividual> coveredNegatives;

		Monitor mon = MonitorFactory.start("lp");

		if (parent == null) {
			coveredPositives = reasoner.hasType(refinement, posNegLP.getPositiveExamples());
			coveredNegatives = reasoner.hasType(refinement, posNegLP.getNegativeExamples());
		} else if (operator instanceof DownwardRefinementOperator) {
			coveredPositives = reasoner.hasType(refinement, parent.getCoveredPositiveExamples());
			coveredNegatives = reasoner.hasType(refinement, parent.getCoveredNegativeExamples());
		} else {
			Set<OWLIndividual> uncoveredPositives = new TreeSet<>(posNegLP.getPositiveExamples());
			uncoveredPositives.removeAll(parent.getCoveredPositiveExamples());
			Set<OWLIndividual> uncoveredNegatives = new TreeSet<>(posNegLP.getNegativeExamples());
			uncoveredNegatives.removeAll(parent.getCoveredNegativeExamples());

			coveredPositives = reasoner.hasType(refinement, uncoveredPositives);
			coveredPositives.addAll(parent.getCoveredPositiveExamples());
			coveredNegatives = reasoner.hasType(refinement, uncoveredNegatives);
			coveredNegatives.addAll(parent.getCoveredNegativeExamples());
		}

		double accuracy = posNegLP.getAccuracyOrTooWeak(coveredPositives, coveredNegatives, noise);

		mon.stop();

		OENode node = new OENode(refinement, accuracy);
		node.setCoveredPositiveExamples(coveredPositives);
		node.setCoveredNegativeExamples(coveredNegatives);

		return node;
	}
	
	// checks whether the class expression is allowed
	private boolean isDescriptionAllowed(OWLClassExpression description, OENode parentNode) {
		if(isClassLearningProblem) {
			if(isEquivalenceProblem) {
				// the class to learn must not appear on the outermost property level
				if(occursOnFirstLevel(description, classToDescribe)) {
					return false;
				}
				if(occursOnSecondLevel(description, classToDescribe)) {
					return false;
				}
			} else {
				// none of the superclasses of the class to learn must appear on the
				// outermost property level
				TreeSet<OWLClassExpression> toTest = new TreeSet<>();
				toTest.add(classToDescribe);
				while(!toTest.isEmpty()) {
					OWLClassExpression d = toTest.pollFirst();
					if(occursOnFirstLevel(description, d)) {
						return false;
					}
					toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
				}
			}
		} else if (learningProblem instanceof ClassAsInstanceLearningProblem) {
			return true;
		}
		
		// perform forall sanity tests
		if (parentNode != null &&
				(ConceptTransformation.getForallOccurences(description) > ConceptTransformation.getForallOccurences(parentNode.getDescription()))) {
			// we have an additional \forall construct, so we now fetch the contexts
			// in which it occurs
			SortedSet<PropertyContext> contexts = ConceptTransformation.getForallContexts(description);
			SortedSet<PropertyContext> parentContexts = ConceptTransformation.getForallContexts(parentNode.getDescription());
			contexts.removeAll(parentContexts);
//			System.out.println("parent description: " + parentNode.getDescription());
//			System.out.println("description: " + description);
//			System.out.println("contexts: " + contexts);
			// we now have to perform sanity checks: if \forall is used, then there
			// should be at least on class instance which has a filler at the given context
			for(PropertyContext context : contexts) {
				// transform [r,s] to \exists r.\exists s.\top
				OWLClassExpression existentialContext = context.toExistentialContext();
				boolean fillerFound = false;
				if(reasoner instanceof SPARQLReasoner) {
					SortedSet<OWLIndividual> individuals = reasoner.getIndividuals(existentialContext);
					fillerFound = !Sets.intersection(individuals, examples).isEmpty();
				} else {
					for(OWLIndividual instance : examples) {
						if(reasoner.hasType(existentialContext, instance)) {
							fillerFound = true;
							break;
						}
					}
				}
				
				// if we do not find a filler, this means that putting \forall at
				// that position is not meaningful
				if(!fillerFound) {
					return false;
				}
			}
		}
		
		// we do not want to have negations of sibling classes on the outermost level
		// (they are expressed more naturally by saying that the siblings are disjoint,
		// so it is reasonable not to include them in solutions)
//		Set<OWLClassExpression> siblingClasses = reasoner.getClassHierarchy().getSiblingClasses(classToDescribe);
//		for now, we just disable negation
		
		return true;
	}
	
	// determine whether a named class occurs on the outermost level, i.e. property depth 0
	// (it can still be at higher depth, e.g. if intersections are nested in unions)
	private boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
		return !cls.isOWLThing() && (description instanceof OWLNaryBooleanClassExpression && ((OWLNaryBooleanClassExpression) description).getOperands().contains(cls));
//        return description.containsConjunct(cls) ||
//                (description instanceof OWLObjectUnionOf && ((OWLObjectUnionOf) description).getOperands().contains(cls));
	}
	
	// determine whether a named class occurs on the outermost level, i.e. property depth 0
		// (it can still be at higher depth, e.g. if intersections are nested in unions)
		private boolean occursOnSecondLevel(OWLClassExpression description, OWLClassExpression cls) {
//			SortedSet<OWLClassExpression> superClasses = reasoner.getSuperClasses(cls);
//			if(description instanceof OWLObjectIntersectionOf) {
//				List<OWLClassExpression> operands = ((OWLObjectIntersectionOf) description).getOperandsAsList();
//
//				for (OWLClassExpression op : operands) {
//					if(superClasses.contains(op) ||
//							(op instanceof OWLObjectUnionOf && !Sets.intersection(((OWLObjectUnionOf)op).getOperands(),superClasses).isEmpty())) {
//						for (OWLClassExpression op2 : operands) {
//							if((op2 instanceof OWLObjectUnionOf && ((OWLObjectUnionOf)op2).getOperands().contains(cls))) {
//								return true;
//							}
//						}
//					}
//				}
//
//				for (OWLClassExpression op1 : operands) {
//					for (OWLClassExpression op2 : operands) {
//						if(!op1.isAnonymous() && op2 instanceof OWLObjectUnionOf) {
//							 for (OWLClassExpression op3 : ((OWLObjectUnionOf)op2).getOperands()) {
//								if(!op3.isAnonymous()) {// A AND B with Disj(A,B)
//									if(reasoner.isDisjoint(op1.asOWLClass(), op3.asOWLClass())) {
//										return true;
//									}
//								} else {// A AND NOT A
//									if(op3 instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)op3).getOperand().equals(op1)) {
//										return true;
//									}
//								}
//							}
//						}
//					}
//				}
//			}
			
			return false;
	    }
	
	private boolean terminationCriteriaSatisfied() {
		return
		stop ||
		(maxClassExpressionTestsAfterImprovement != 0 && (expressionTests - expressionTestCountLastImprovement >= maxClassExpressionTestsAfterImprovement)) ||
		(maxClassExpressionTests != 0 && (expressionTests >= maxClassExpressionTests)) ||
		(maxExecutionTimeInSecondsAfterImprovement != 0 && ((getCurrentCpuMillis() - timeLastImprovement) >= (maxExecutionTimeInSecondsAfterImprovement * 1000L))) ||
		(maxExecutionTimeInSeconds != 0 && (getCurrentCpuMillis() >= (maxExecutionTimeInSeconds * 1000L))) ||
		(terminateOnNoiseReached && (100*getCurrentlyBestAccuracy()>=100-noisePercentage)) ||
		(stopOnFirstDefinition && (getCurrentlyBestAccuracy() >= 1));
	}
	
	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
		searchTree = new SearchTree<>(heuristic);
		descriptions = new TreeSet<>();
		bestEvaluatedDescriptions.getSet().clear();
		expressionTests = 0;
		runtimeVsBestScore.clear();

		solutionCandidates.clear();
	}
	
	private void printAlgorithmRunStats() {
		if (stop) {
			logger.info("Algorithm stopped ("+expressionTests+" descriptions tested). " + searchTree.size() + " nodes in the search tree.\n");
			logger.info(reasoner.toString());
		} else {
			totalRuntimeNs = System.nanoTime()-nanoStartTime;
			logger.info("Algorithm terminated successfully (time: " + Helper.prettyPrintNanoSeconds(totalRuntimeNs) + ", "+expressionTests+" descriptions tested, "  + searchTree.size() + " nodes in the search tree).\n");
            logger.info(reasoner.toString());
		}
	}

	private void printSolutionCandidates() {
		DecimalFormat df = new DecimalFormat();

		if (solutionCandidates.size() > 0) {
			// we do not need to print the best node if we display the top 20 solutions below anyway
			logger.info("solutions within margin (at most " + maxNrOfResultsWithinMargin + " are shown):");
			int show = 1;
			for (OENode c : solutionCandidates.descendingKeySet()) {
				logger.info(show + ": " + renderer.render(c.getDescription())
					+ " (accuracy " + df.format(100 * c.getAccuracy()) + "% / "
					+ df.format(100 * computeTestAccuracy(c.getDescription())) + "%"
					+ ", length " + OWLClassExpressionUtils.getLength(c.getDescription())
					+ ", depth " + OWLClassExpressionUtils.getDepth(c.getDescription())
					+ ", time " + df.format(solutionCandidates.get(c)) + "s)");
				if (show >= maxNrOfResultsWithinMargin) {
					break;
				}
				show++;
			}
		} else {
			logger.info("no appropriate solutions within margin found (try increasing the noisePercentageMargin)");
		}
	}

	private void showIfBetterSolutionsFound() {
		if(!singleSuggestionMode && bestEvaluatedDescriptions.getBestAccuracy() > currentHighestAccuracy) {
			currentHighestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();
			expressionTestCountLastImprovement = expressionTests;
			timeLastImprovement = getCurrentCpuMillis();
			long durationInMillis = getCurrentRuntimeInMilliSeconds();
			String durationStr = getDurationAsString(durationInMillis);

			double cpuTime = getCurrentCpuMillis() / 1000.0;

			OWLClassExpression bestDescription = bestEvaluatedDescriptions.getBest().getDescription();
			double testAccuracy = computeTestAccuracy(bestDescription);

			// track new best accuracy if enabled
			if(keepTrackOfBestScore) {
				runtimeVsBestScore.put(getCurrentRuntimeInMilliSeconds(), currentHighestAccuracy);
			}

			logger.info(
				"Time " + cpuTime +
				"s: more accurate (training: " + dfPercent.format(currentHighestAccuracy) +
				", test: " + dfPercent.format(testAccuracy) +
				") class expression found after " + durationStr + ": " +
				descriptionToString(bestEvaluatedDescriptions.getBest().getDescription())
			);

			recordBestConceptTimeAndAccuracy(cpuTime, currentHighestAccuracy, testAccuracy);
		}
	}

	private void writeSearchTree(TreeSet<OWLClassExpression> refinements) {
		StringBuilder treeString = new StringBuilder("best node: ").append(bestEvaluatedDescriptions.getBest()).append("\n");
		if (refinements.size() > 1) {
			treeString.append("all expanded nodes:\n");
			for (OWLClassExpression ref : refinements) {
				treeString.append("   ").append(ref).append("\n");
			}
		}
		treeString.append(TreeUtils.toTreeString(searchTree)).append("\n");

		// replace or append
		if (replaceSearchTree) {
			Files.createFile(new File(searchTreeFile), treeString.toString());
		} else {
			Files.appendToFile(new File(searchTreeFile), treeString.toString());
		}
	}
	
	private void updateMinMaxHorizExp(OENode node) {
		int newHorizExp = node.getHorizontalExpansion();
		
		// update maximum value
		maxHorizExp = Math.max(maxHorizExp, newHorizExp);
		
		// we just expanded a node with minimum horizontal expansion;
		// we need to check whether it was the last one
		if(minHorizExp == newHorizExp - 1) {
			
			// the best accuracy that a node can achieve
			double scoreThreshold = heuristic.getNodeScore(node) + 1 - node.getAccuracy();
			
			for(OENode n : searchTree.descendingSet()) {
				if(n != node) {
					if(n.getHorizontalExpansion() == minHorizExp) {
						// we can stop instantly when another node with min.
						return;
					}
					if(heuristic.getNodeScore(n) < scoreThreshold) {
						// we can stop traversing nodes when their score is too low
						break;
					}
				}
			}
			
			// inc. minimum since we found no other node which also has min. horiz. exp.
			minHorizExp++;
			
//			System.out.println("minimum horizontal expansion is now " + minHorizExp);
		}
	}
	
	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		EvaluatedDescription<? extends Score> ed = getCurrentlyBestEvaluatedDescription();
		return ed == null ? null : ed.getDescription();
	}

	@Override
	public List<OWLClassExpression> getCurrentlyBestDescriptions() {
		return bestEvaluatedDescriptions.toDescriptionList();
	}
	
	@Override
	public EvaluatedDescription<? extends Score> getCurrentlyBestEvaluatedDescription() {
		return bestEvaluatedDescriptions.getBest();
	}
	
	@Override
	public NavigableSet<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestEvaluatedDescriptions() {
		return bestEvaluatedDescriptions.getSet();
	}
	
	public double getCurrentlyBestAccuracy() {
		return bestEvaluatedDescriptions.getBest().getAccuracy();
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}
	
	@Override
	public void stop() {
		stop = true;
	}

	public int getMaximumHorizontalExpansion() {
		return maxHorizExp;
	}

	public int getMinimumHorizontalExpansion() {
		return minHorizExp;
	}
	
	/**
	 * @return the expressionTests
	 */
	public int getClassExpressionTests() {
		return expressionTests;
	}

	public LengthLimitedRefinementOperator getOperator() {
		return operator;
	}

	@Autowired(required=false)
	public void setOperator(LengthLimitedRefinementOperator operator) {
		this.operator = operator;
	}

	public OWLClassExpression getStartClass() {
		return startClass;
	}

	public void setStartClass(OWLClassExpression startClass) {
		this.startClass = startClass;
	}
	
	public boolean isWriteSearchTree() {
		return writeSearchTree;
	}

	public void setWriteSearchTree(boolean writeSearchTree) {
		this.writeSearchTree = writeSearchTree;
	}

	public String getSearchTreeFile() {
		return searchTreeFile;
	}

	public void setSearchTreeFile(String searchTreeFile) {
		this.searchTreeFile = searchTreeFile;
	}

	public int getMaxNrOfResults() {
		return maxNrOfResults;
	}

	public void setMaxNrOfResults(int maxNrOfResults) {
		this.maxNrOfResults = maxNrOfResults;
	}

	public double getNoisePercentage() {
		return noisePercentage;
	}

	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}

	public boolean isFilterDescriptionsFollowingFromKB() {
		return filterDescriptionsFollowingFromKB;
	}

	public void setFilterDescriptionsFollowingFromKB(boolean filterDescriptionsFollowingFromKB) {
		this.filterDescriptionsFollowingFromKB = filterDescriptionsFollowingFromKB;
	}

	public boolean isReplaceSearchTree() {
		return replaceSearchTree;
	}

	public void setReplaceSearchTree(boolean replaceSearchTree) {
		this.replaceSearchTree = replaceSearchTree;
	}

	public boolean isTerminateOnNoiseReached() {
		return terminateOnNoiseReached;
	}

	public void setTerminateOnNoiseReached(boolean terminateOnNoiseReached) {
		this.terminateOnNoiseReached = terminateOnNoiseReached;
	}

	public boolean isReuseExistingDescription() {
		return reuseExistingDescription;
	}

	public void setReuseExistingDescription(boolean reuseExistingDescription) {
		this.reuseExistingDescription = reuseExistingDescription;
	}

	public AbstractHeuristic getHeuristic() {
		return heuristic;
	}

	@Autowired(required=false)
	public void setHeuristic(AbstractHeuristic heuristic) {
		this.heuristic = heuristic;
	}

	public int getMaxExecutionTimeInSecondsAfterImprovement() {
		return maxExecutionTimeInSecondsAfterImprovement;
	}

	public void setMaxExecutionTimeInSecondsAfterImprovement(
			int maxExecutionTimeInSecondsAfterImprovement) {
		this.maxExecutionTimeInSecondsAfterImprovement = maxExecutionTimeInSecondsAfterImprovement;
	}
	
	public boolean isSingleSuggestionMode() {
		return singleSuggestionMode;
	}

	public void setSingleSuggestionMode(boolean singleSuggestionMode) {
		this.singleSuggestionMode = singleSuggestionMode;
	}

	public int getMaxClassExpressionTests() {
		return maxClassExpressionTests;
	}

	public void setMaxClassExpressionTests(int maxClassExpressionTests) {
		this.maxClassExpressionTests = maxClassExpressionTests;
	}

	public int getMaxClassExpressionTestsAfterImprovement() {
		return maxClassExpressionTestsAfterImprovement;
	}

	public void setMaxClassExpressionTestsAfterImprovement(
			int maxClassExpressionTestsAfterImprovement) {
		this.maxClassExpressionTestsAfterImprovement = maxClassExpressionTestsAfterImprovement;
	}

	public double getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(double maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	public boolean isStopOnFirstDefinition() {
		return stopOnFirstDefinition;
	}

	public void setStopOnFirstDefinition(boolean stopOnFirstDefinition) {
		this.stopOnFirstDefinition = stopOnFirstDefinition;
	}

	public long getTotalRuntimeNs() {
		return totalRuntimeNs;
	}
	
	/**
	 * @return the expandAccuracy100Nodes
	 */
	public boolean isExpandAccuracy100Nodes() {
		return expandAccuracy100Nodes;
	}

	/**
	 * @param expandAccuracy100Nodes the expandAccuracy100Nodes to set
	 */
	public void setExpandAccuracy100Nodes(boolean expandAccuracy100Nodes) {
		this.expandAccuracy100Nodes = expandAccuracy100Nodes;
	}

	/**
	 * Whether to keep track of the best score during the algorithm run.
	 *
	 * @param keepTrackOfBestScore
	 */
	public void setKeepTrackOfBestScore(boolean keepTrackOfBestScore) {
		this.keepTrackOfBestScore = keepTrackOfBestScore;
	}

	/**
	 * @return a map containing time points at which a hypothesis with a better score than before has been found
	 */
	public SortedMap<Long, Double> getRuntimeVsBestScore() {
		return runtimeVsBestScore;
	}

	/**
	 * Return a map that contains
	 * <ol>
	 *     <li>entries with time points at which a hypothesis with a better score than before has been found</li>
	 *     <li>entries with the current best score for each defined interval time point</li>
	 * </ol>
	 *
	 * @param ticksIntervalTimeValue at which time point the current best score is tracked periodically
	 * @param ticksIntervalTimeUnit the time unit of the periodic time point values
	 *
	 * @return the map
	 *
	 */
	public SortedMap<Long, Double> getRuntimeVsBestScore(long ticksIntervalTimeValue, TimeUnit ticksIntervalTimeUnit) {
		SortedMap<Long, Double> map = new TreeMap<>(runtimeVsBestScore);

		// add entries for fixed time points if enabled
		if(ticksIntervalTimeValue > 0) {
			long ticksIntervalInMs = TimeUnit.MILLISECONDS.convert(ticksIntervalTimeValue, ticksIntervalTimeUnit);

			// add  t = 0 -> 0
			map.put(0L, 0d);

			for(long t = ticksIntervalInMs; t <= TimeUnit.SECONDS.toMillis(maxExecutionTimeInSeconds); t += ticksIntervalInMs) {
				// add value of last entry before this time point
				map.put(t, map.get(runtimeVsBestScore.headMap(t).lastKey()));
			}

			// add  entry for t = totalRuntime
			long totalRuntimeMs = Math.min(TimeUnit.SECONDS.toMillis(maxExecutionTimeInSeconds), TimeUnit.NANOSECONDS.toMillis(totalRuntimeNs));
			map.put(totalRuntimeMs, map.get(map.lastKey()));
		}

		return map;
	}

	public int getMaxNrOfResultsWithinMargin() {
		return maxNrOfResultsWithinMargin;
	}

	public void setMaxNrOfResultsWithinMargin(int maxNrOfResultsWithinMargin) {
		this.maxNrOfResultsWithinMargin = maxNrOfResultsWithinMargin;
	}

	public double getNoisePercentageMargin() {
		return noisePercentageMargin;
	}

	public void setNoisePercentageMargin(double noisePercentageMargin) {
		this.noisePercentageMargin = noisePercentageMargin;
	}

	/* (non-Javadoc)
			 * @see java.lang.Object#clone()
			 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new CELOE(this);
	}

	public static void main(String[] args) throws Exception{
//		File file = new File("../examples/swore/swore.rdf");
//		OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/CustomerRequirement"));
		File file = new File("../examples/father.owl");
		OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://example.com/father#male"));
		
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		
		AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
		baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        baseReasoner.init();
		ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
		rc.setReasonerComponent(baseReasoner);
		rc.init();
		
		ClassLearningProblem lp = new ClassLearningProblem(rc);
//		lp.setEquivalence(false);
		lp.setClassToDescribe(classToDescribe);
		lp.init();
		
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(rc);
		op.setUseNegation(false);
		op.setUseHasValueConstructor(false);
		op.setUseCardinalityRestrictions(true);
		op.setUseExistsConstructor(true);
		op.setUseAllConstructor(true);
		op.init();
		
		
		
		//(male ⊓ (∀ hasChild.⊤)) ⊔ (∃ hasChild.(∃ hasChild.male))
		OWLDataFactory df = new OWLDataFactoryImpl();
		OWLClass male = df.getOWLClass(IRI.create("http://example.com/father#male"));
		OWLClassExpression ce = df.getOWLObjectIntersectionOf(
									df.getOWLObjectUnionOf(
											male,
											df.getOWLObjectIntersectionOf(
													male, male),
											df.getOWLObjectSomeValuesFrom(
												df.getOWLObjectProperty(IRI.create("http://example.com/father#hasChild")),
												df.getOWLThing())
									),
									df.getOWLObjectAllValuesFrom(
											df.getOWLObjectProperty(IRI.create("http://example.com/father#hasChild")),
											df.getOWLThing()
											)
				);
		System.out.println(ce);
		OWLClassExpressionMinimizer min = new OWLClassExpressionMinimizer(df, rc);
		ce = min.minimizeClone(ce);
		System.out.println(ce);
		
		CELOE alg = new CELOE(lp, rc);
		alg.setMaxExecutionTimeInSeconds(10);
		alg.setOperator(op);
		alg.setWriteSearchTree(true);
		alg.setSearchTreeFile("log/search-tree.log");
		alg.setReplaceSearchTree(true);
		alg.init();
		alg.setKeepTrackOfBestScore(true);
		
		alg.start();

		SortedMap<Long, Double> map = alg.getRuntimeVsBestScore(1, TimeUnit.SECONDS);
		System.out.println(MapUtils.asTSV(map, "runtime", "best_score"));

	}
	
}