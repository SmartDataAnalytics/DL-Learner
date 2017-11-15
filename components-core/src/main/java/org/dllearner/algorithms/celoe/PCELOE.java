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
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassAsInstanceLearningProblem;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.refinementoperators.*;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.TreeUtils;
import org.dllearner.utilities.datastructures.SynchronizedSearchTree;
import org.dllearner.utilities.owl.*;
import org.semanticweb.owlapi.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The PCELOE is an experimental, parallel implementation of the CELOE algorithm.
 *
 * @author Lorenz Buehmann
 *
 */
@ComponentAnn(name="PCELOE", shortName="pceloe", version=1.0, description="PCELOE is an experimental, parallel implementation of the CELOE algorithm.")
public class PCELOE extends AbstractCELA {

	private static Logger logger = Logger.getLogger(PCELOE.class);

	private boolean isRunning = false;
	private boolean stop = false;
	
	@ConfigOption(description = "the refinement operator instance to use")
	private LengthLimitedRefinementOperator operator;

	private SynchronizedSearchTree<OENode> searchTree;
	@ConfigOption(defaultValue="celoe_heuristic")
	private AbstractHeuristic heuristic;
	// the class with which we start the refinement process
	@ConfigOption(defaultValue="owl:Thing", description="You can specify a start class for the algorithm. To do this, you have to use Manchester OWL syntax without using prefixes.")
	private OWLClassExpression startClass;

	// all descriptions in the search tree plus those which were too weak (for fast redundancy check)
//	private TreeSet<OWLClassExpression> descriptions;
	private SortedSet<OWLClassExpression> descriptions;

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

	private long nanoStartTime;

	// important parameters (non-config options but internal)
	private double noise;

	private boolean filterFollowsFromKB;
	
    // less important parameters
	// forces that one solution cannot be subexpression of another expression; this option is useful to get diversity
	// but it can also suppress quite useful expressions
	private boolean forceMutualDifference = false;

	// statistical variables
		private int expressionTests = 0;
		private int minHorizExp = 0;
		private int maxHorizExp = 0;
		private long totalRuntimeNs = 0;

	// TODO: turn those into config options

	// important: do not initialise those with empty sets
	// null = no settings for allowance / ignorance
	// empty set = allow / ignore nothing (it is often not desired to allow no class!)
	Set<OWLClass> allowedConcepts = null;
	Set<OWLClass> ignoredConcepts = null;

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

		@ConfigOption(defaultValue = "10", description = "maximum execution of the algorithm in seconds")
		private int maxExecutionTimeInSeconds = 10;

		@ConfigOption(defaultValue = "0", description = "maximum execution of the algorithm in seconds")
		private int maxExecutionTimeInSecondsAfterImprovement = 0;

		@ConfigOption(defaultValue="false", description="specifies whether to terminate when noise criterion is met")
		private boolean terminateOnNoiseReached = false;

		@ConfigOption(defaultValue="7", description="maximum depth of description")
		private double maxDepth = 7;

		@ConfigOption(defaultValue="false", description="algorithm will terminate immediately when a correct definition is found")
		private boolean stopOnFirstDefinition = false;

		@ConfigOption(defaultValue = "false",  description = "whether to try and refine solutions which already have accuracy value of 1")
		private boolean expandAccuracy100Nodes = false;
		private double currentHighestAccuracy;

	@ConfigOption(defaultValue="2", description="number of threads running in parallel")
	private int nrOfThreads = 2;

	private int expressionTestCountLastImprovement;
	private long timeLastImprovement = 0;

	private Set<OENode> currentlyProcessedNodes = Collections.synchronizedSet(new HashSet<>());
	private volatile double highestAccuracy = 0.0;

	public PCELOE() {}

	public PCELOE(PCELOE celoe){
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
		setMaxExecutionTimeInSeconds(celoe.maxExecutionTimeInSeconds);
		setMaxExecutionTimeInSecondsAfterImprovement(celoe.maxExecutionTimeInSecondsAfterImprovement);
		setMaxNrOfResults(celoe.maxNrOfResults);
		setNoisePercentage(celoe.noisePercentage);

		RhoDRDown op = new RhoDRDown((RhoDRDown)celoe.operator);
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
	}

	public PCELOE(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
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

		minimizer = new OWLClassExpressionMinimizer(dataFactory, reasoner);

		if (writeSearchTree) {
			File f = new File(searchTreeFile);
			if (f.getParentFile() != null) {
				f.getParentFile().mkdirs();
			}
			Files.clearFile(f);
		}

		// start at owl:Thing by default
		if (startClass == null) {
			startClass = computeStartClass();
		} else {
			try {
				this.startClass = OWLAPIUtils.classExpressionPropertyExpander(this.startClass, reasoner, dataFactory);
			} catch (Exception e) {
				logger.warn("Error parsing start class.", e);
				logger.warn("Using owl:Thing instead.");
				this.startClass = dataFactory.getOWLThing();
			}
		}

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
			if (operator instanceof CustomStartRefinementOperator) {
				((CustomStartRefinementOperator) operator).setStartClass(startClass);
			}
			if (operator instanceof ReasoningBasedRefinementOperator) {
				((ReasoningBasedRefinementOperator) operator).setReasoner(reasoner);
			}
			operator.init();
		}
		if (operator instanceof CustomHierarchyRefinementOperator) {
			((CustomHierarchyRefinementOperator) operator).setClassHierarchy(classHierarchy);
			((CustomHierarchyRefinementOperator) operator).setObjectPropertyHierarchy(objectPropertyHierarchy);
			((CustomHierarchyRefinementOperator) operator).setDataPropertyHierarchy(datatypePropertyHierarchy);
		}

		reasoner.setSynchronized();
		operator = new SynchronizedRefinementOperator(operator);
		
		initialized = true;
	}

	@Override
	public void start() {
		logger.info("starting PCELOE with " + nrOfThreads + " threads");
//		System.out.println(configurator.getMaxExecutionTimeInSeconds());

		stop = false;
		isRunning = true;
		reset();
		nanoStartTime = System.nanoTime();

		addNode(startClass, null);

		int nrOfWorkers = nrOfThreads;
		if(nrOfWorkers == 0){
			nrOfWorkers = Runtime.getRuntime().availableProcessors();
		}
		ExecutorService service = Executors.newFixedThreadPool(nrOfWorkers);

		List<Runnable> tasks = new ArrayList<>();

		boolean shareRefinementOperator = false;
		
		for(int i = 0; i < nrOfWorkers; i++){
			PCELOEWorker worker;
			if(shareRefinementOperator) {
				ClosedWorldReasoner reasonerCopy = new ClosedWorldReasoner();
				reasonerCopy.setSources(reasoner.getSources());
				try {
					reasonerCopy.init();
				} catch (ComponentInitException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if(operator instanceof SynchronizedRefinementOperator) {
					operator = ((SynchronizedRefinementOperator) operator).getDelegate();
				}
				RhoDRDown op = new RhoDRDown((RhoDRDown) operator);
				op.setReasoner(reasonerCopy);
				try {
					op.init();
				} catch (ComponentInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				worker = new PCELOEWorker(op);
			} else {
				worker = new PCELOEWorker();

			}
			tasks.add(worker);
		}
		nanoStartTime = System.nanoTime();

		//needed to block until all threads have been finished, because otherwise the main thread outputs the result to early
		List<Future> futures = new ArrayList<>();
		for(Runnable task : tasks){
			futures.add(service.submit(task));
		}
		for(Future future : futures){
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		if (stop) {
			logger.info("Algorithm stopped ("+expressionTests+" descriptions tested). " + searchTree.size() + " nodes in the search tree.\n");
		} else {
			logger.info("Algorithm terminated successfully (time: " + Helper.prettyPrintNanoSeconds(System.nanoTime()-nanoStartTime) + ", "+expressionTests+" descriptions tested, "  + searchTree.size() + " nodes in the search tree).\n");
            logger.info(reasoner.toString());
		}

		if(singleSuggestionMode) {
			bestEvaluatedDescriptions.add(bestDescription, bestAccuracy, learningProblem);
		}

		// print solution(s)
		logger.info("solutions:\n" + getSolutionString());

//		System.out.println(startNode.toTreeString(baseURI));

		isRunning = false;
		service.shutdown();
//		System.out.println("isRunning: " + isRunning);
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
		synchronized (searchTree) {
			Iterator<OENode> it = searchTree.descendingIterator();

			while(it.hasNext()) {
				OENode node = it.next();
				if (currentlyProcessedNodes.contains(node)) continue;

				if (isExpandAccuracy100Nodes() && node.getHorizontalExpansion() < OWLClassExpressionUtils.getLength(node.getDescription())) {
						currentlyProcessedNodes.add(node);
						return node;
				} else {
					if(node.getAccuracy() < 1.0 || node.getHorizontalExpansion() < OWLClassExpressionUtils.getLength(node.getDescription())) {
						currentlyProcessedNodes.add(node);
						return node;
					}
				}
			}
		}

		// this should practically never be called, since for any reasonable learning
		// task, we will always have at least one node with less than 100% accuracy
		return null;//nodes.last();
	}

	// expand node horizontically
	private TreeSet<OWLClassExpression> refineNode(LengthLimitedRefinementOperator operator, OENode node) {
		MonitorFactory.getTimeMonitor("refineNode").start();
		// we have to remove and add the node since its heuristic evaluation changes through the expansion
		// (you *must not* include any criteria in the heuristic which are modified outside of this method,
		// otherwise you may see rarely occurring but critical false ordering in the nodes set)
		searchTree.updatePrepare(node);
		int horizExp = node.getHorizontalExpansion();
		TreeSet<OWLClassExpression> refinements = (TreeSet<OWLClassExpression>) operator.refine(node.getDescription(), horizExp+1);
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
		MonitorFactory.getTimeMonitor("addNode").start();

		// redundancy check (return if redundant)
		boolean nonRedundant = descriptions.add(description);
		if(!nonRedundant) {
			return false;
		}

		// check whether the class expression is allowed
		if(!isDescriptionAllowed(description, parentNode)) {
			return false;
		}

		// quality of class expression (return if too weak)
		Monitor mon = MonitorFactory.start("lp");
		double accuracy = learningProblem.getAccuracyOrTooWeak(description, noise);
		mon.stop();

		// issue a warning if accuracy is not between 0 and 1 or -1 (too weak)
		if(accuracy > 1.0 || (accuracy < 0.0 && accuracy != -1)) {
			throw new RuntimeException("Invalid accuracy value " + accuracy + " for class expression " + description +
					". This could be caused by a bug in the heuristic measure and should be reported to the DL-Learner bug tracker.");
		}

		expressionTests++;

		// return FALSE if 'too weak'
		if(accuracy == -1) {
			return false;
		}

		OENode node = new OENode(description, accuracy);

		// link to parent (unless start node)
		searchTree.addNode(parentNode, node);

		// in some cases (e.g. mutation) fully evaluating even a single class expression is too expensive
		// due to the high number of examples -- so we just stick to the approximate accuracy
		if(singleSuggestionMode) {
			if(accuracy > bestAccuracy) {
				bestAccuracy = accuracy;
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
			EvaluatedDescription worst = bestEvaluatedDescriptions.getWorst();
			double accThreshold = worst.getAccuracy();
			isCandidate =
				(accuracy > accThreshold ||
				(accuracy >= accThreshold && OWLClassExpressionUtils.getLength(description) < worst.getDescriptionLength()));
		}

		if(isCandidate) {
			OWLClassExpression niceDescription = node.getExpression();//rewrite(node.getExpression());

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
					if(Math.abs(ed.getAccuracy()-accuracy) <= 0.00001 && ConceptTransformation.isSubdescription(niceDescription, ed.getDescription())) {
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
					bestEvaluatedDescriptions.add(niceDescription, accuracy, learningProblem);
//					System.out.println("acc: " + accuracy);
//					System.out.println(bestEvaluatedDescriptions);
				}
			}

//			bestEvaluatedDescriptions.add(node.getDescription(), accuracy, learningProblem);

//			System.out.println(bestEvaluatedDescriptions.getSet().size());
		}

		return true;
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
//				System.out.println("parent description: " + parentNode.getDescription());
//				System.out.println("description: " + description);
//				System.out.println("contexts: " + contexts);
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
//			Set<OWLClassExpression> siblingClasses = reasoner.getClassHierarchy().getSiblingClasses(classToDescribe);
//			for now, we just disable negation

			return true;
		}

		// determine whether a named class occurs on the outermost level, i.e. property depth 0
		// (it can still be at higher depth, e.g. if intersections are nested in unions)
		private boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
			if(cls.isOWLThing()) {
				return false;
			}
			return (description instanceof OWLNaryBooleanClassExpression &&
					((OWLNaryBooleanClassExpression)description).getOperands().contains(cls));
//	        return description.containsConjunct(cls) ||
//	                (description instanceof OWLObjectUnionOf && ((OWLObjectUnionOf) description).getOperands().contains(cls));
	    }

		// determine whether a named class occurs on the outermost level, i.e. property depth 0
			// (it can still be at higher depth, e.g. if intersections are nested in unions)
			private boolean occursOnSecondLevel(OWLClassExpression description, OWLClassExpression cls) {
//				SortedSet<OWLClassExpression> superClasses = reasoner.getSuperClasses(cls);
//				if(description instanceof OWLObjectIntersectionOf) {
//					List<OWLClassExpression> operands = ((OWLObjectIntersectionOf) description).getOperandsAsList();
//
//					for (OWLClassExpression op : operands) {
//						if(superClasses.contains(op) ||
//								(op instanceof OWLObjectUnionOf && !Sets.intersection(((OWLObjectUnionOf)op).getOperands(),superClasses).isEmpty())) {
//							for (OWLClassExpression op2 : operands) {
//								if((op2 instanceof OWLObjectUnionOf && ((OWLObjectUnionOf)op2).getOperands().contains(cls))) {
//									return true;
//								}
//							}
//						}
//					}
//
//					for (OWLClassExpression op1 : operands) {
//						for (OWLClassExpression op2 : operands) {
//							if(!op1.isAnonymous() && op2 instanceof OWLObjectUnionOf) {
//								 for (OWLClassExpression op3 : ((OWLObjectUnionOf)op2).getOperands()) {
//									if(!op3.isAnonymous()) {// A AND B with Disj(A,B)
//										if(reasoner.isDisjoint(op1.asOWLClass(), op3.asOWLClass())) {
//											return true;
//										}
//									} else {// A AND NOT A
//										if(op3 instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)op3).getOperand().equals(op1)) {
//											return true;
//										}
//									}
//								}
//							}
//						}
//					}
//				}

				return false;
		    }

	private boolean terminationCriteriaSatisfied() {
		return
		stop ||
		(maxClassExpressionTestsAfterImprovement != 0 && (expressionTests - expressionTestCountLastImprovement >= maxClassExpressionTestsAfterImprovement)) ||
		(maxClassExpressionTests != 0 && (expressionTests >= maxClassExpressionTests)) ||
		(maxExecutionTimeInSecondsAfterImprovement != 0 && ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSecondsAfterImprovement* 1000000000L))) ||
		(maxExecutionTimeInSeconds != 0 && ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSeconds* 1000000000L))) ||
		(terminateOnNoiseReached && (100*getCurrentlyBestAccuracy()>=100-noisePercentage)) ||
		(stopOnFirstDefinition && (getCurrentlyBestAccuracy() >= 1));
	}

	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
//		nodes = new TreeSet<OENode>(heuristic);
		searchTree = new SynchronizedSearchTree(heuristic);
		//Sets.synchronizedNavigableSet(new TreeSet<OENode>(Collections.reverseOrder(heuristic)));
		descriptions = Collections.synchronizedSortedSet(new TreeSet<>());
		bestEvaluatedDescriptions.getSet().clear();
		expressionTests = 0;
		highestAccuracy = 0.0;
	}

	private void printAlgorithmRunStats() {
		if (stop) {
			logger.info("Algorithm stopped ("+expressionTests+" descriptions tested). " + searchTree.size() + " nodes in the search tree.\n");
		} else {
			totalRuntimeNs = System.nanoTime()-nanoStartTime;
			logger.info("Algorithm terminated successfully (time: " + Helper.prettyPrintNanoSeconds(totalRuntimeNs) + ", "+expressionTests+" descriptions tested, "  + searchTree.size() + " nodes in the search tree).\n");
            logger.info(reasoner.toString());
		}
	}

	private void showIfBetterSolutionsFound() {
		if(!singleSuggestionMode && bestEvaluatedDescriptions.getBestAccuracy() > currentHighestAccuracy) {
			currentHighestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();
			expressionTestCountLastImprovement = expressionTests;
			timeLastImprovement = System.nanoTime();
			long durationInMillis = getCurrentRuntimeInMilliSeconds();
			String durationStr = getDurationAsString(durationInMillis);
			logger.info("more accurate (" + dfPercent.format(currentHighestAccuracy) + ") class expression found after " + durationStr + ": " + descriptionToString(bestEvaluatedDescriptions.getBest().getDescription()));
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
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
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

	@Deprecated
	public int getMaxClassDescriptionTests() {
		return maxClassExpressionTests;
	}

	@Deprecated
	public void setMaxClassDescriptionTests(int maxClassDescriptionTests) {
		this.maxClassExpressionTests = maxClassDescriptionTests;
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

	@Deprecated
	public int getMaxClassExpressionTestsWithoutImprovement() {
		return maxClassExpressionTestsAfterImprovement;
	}

	@Deprecated
	public void setMaxClassExpressionTestsWithoutImprovement(
			int maxClassExpressionTestsWithoutImprovement) {
		this.maxClassExpressionTestsAfterImprovement = maxClassExpressionTestsWithoutImprovement;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new PCELOE(this);
	}

	public int getNrOfThreads() {
		return nrOfThreads;
	}

	public void setNrOfThreads(int nrOfThreads) {
		this.nrOfThreads = nrOfThreads;
	}

	private synchronized double getHighestAccuracy(){
		return highestAccuracy;
	}

	class PCELOEWorker implements Runnable{
		
		private LengthLimitedRefinementOperator operator;

		public PCELOEWorker() {
			operator = PCELOE.this.operator;
		}
		
		public PCELOEWorker(LengthLimitedRefinementOperator operator) {
			this.operator = operator;
		}

		@Override
		public void run() {
			OENode nextNode;
			while (!terminationCriteriaSatisfied()) {
//				String threadName = Thread.currentThread().getName();

				nextNode = getNextNodeToExpand();
//				System.out.println(threadName + " processing " + nextNode);

				if(nextNode != null){
					int horizExp = nextNode.getHorizontalExpansion();

					// apply refinement operator
					TreeSet<OWLClassExpression> refinements = refineNode(operator, nextNode);

					while(!refinements.isEmpty() && !terminationCriteriaSatisfied()) {
						// pick element from set
						OWLClassExpression refinement = refinements.pollFirst();
//						System.out.println(threadName + " analysing " + refinement);

						// get length of class expression
						int length = OWLClassExpressionUtils.getLength(refinement);

						// we ignore all refinements with lower length and too high depth
						// (this also avoids duplicate node children)
						if(length > horizExp && OWLClassExpressionUtils.getDepth(refinement) <= maxDepth) {
							// add node to search tree
							boolean added = addNode(refinement, nextNode);
							if(added) {
//								System.out.println(threadName + " put " + refinement);
							}
						}
					}
					currentlyProcessedNodes.remove(nextNode);
//					showIfBetterSolutionsFound();
				}

			}
		}
	}

	public static void main(String[] args) throws Exception{
//		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(PCELOE.class).setLevel(Level.DEBUG);
		Logger.getLogger(PCELOE.class).addAppender(new FileAppender(new PatternLayout( "[%t] %c: %m%n" ), "log/parallel_run.txt", false));

		AbstractKnowledgeSource ks = new OWLFile("../examples/family/father_oe.owl");
		ks.init();

		AbstractReasonerComponent rc = new ClosedWorldReasoner(ks);
		rc.init();

		ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(new OWLClassImpl(IRI.create("http://example.com/father#father")));
		lp.setCheckConsistency(false);
		lp.init();

		PCELOE alg = new PCELOE(lp, rc);
		alg.setMaxExecutionTimeInSeconds(10);
		alg.setNrOfThreads(4);
//		alg.setMaxClassDescriptionTests(200);
		alg.init();

		alg.start();
	}

}
