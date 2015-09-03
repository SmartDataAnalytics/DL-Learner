package org.dllearner.algorithms.distributed.amqp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.JMSException;

import org.apache.qpid.AMQException;
import org.apache.qpid.url.URLSyntaxException;
import org.dllearner.algorithms.distributed.AbstractDistHeuristic2;
import org.dllearner.algorithms.distributed.DistOEHeuristicRuntime2;
import org.dllearner.algorithms.distributed.DistOENode2;
import org.dllearner.algorithms.distributed.DistOENodeTree2;
import org.dllearner.algorithms.distributed.containers.NodeTreeContainer2;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassAsInstanceLearningProblem;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.refinementoperators.CustomHierarchyRefinementOperator;
import org.dllearner.refinementoperators.CustomStartRefinementOperator;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.ReasoningBasedRefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.owl.PropertyContext;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class DistCELOEAMQP extends AbstractMultiChannelAMQPAgent {
	private static Logger logger = LoggerFactory.getLogger(DistCELOEAMQP.class);

	// <------------------------ component attributes ------------------------>
	@ConfigOption(name="maxClassExpressionTestsAfterImprovement", defaultValue="0",
			description = "The maximum number of candidate hypothesis the " +
					"algorithm is allowed after an improvement in accuracy " +
					"(0 = no limit). The algorithm will stop afterwards. " +
					"(The real number of tests can be slightly higher, " +
					"because this criterion usually won't be checked after " +
					"each single test.)")
	private int maxClassExpressionTestsAfterImprovement = 0;

	@ConfigOption(name="maxClassExpressionTests", defaultValue="0",
			description="The maximum number of candidate hypothesis the " +
					"algorithm is allowed to test (0 = no limit). The " +
					"algorithm will stop afterwards. (The real number of " +
					"tests can be slightly higher, because this criterion " +
					"usually won't be checked after each single test.)")
	private int maxClassExpressionTests = 0;

	@ConfigOption(name="maxDepth", defaultValue="7", description="maximum depth of description")
	private double maxDepth = 7;

	@ConfigOption(defaultValue="10", name="maxExecutionTimeInSeconds",
			description = "maximum execution of the algorithm in seconds")
	private int maxExecutionTimeInSeconds = 10;

	@ConfigOption(defaultValue="0", name="maxExecutionTimeInSecondsAfterImprovement",
			description = "maximum execution of the algorithm in seconds")
	private int maxExecutionTimeInSecondsAfterImprovement = 0;

	@ConfigOption(name="maxNrOfResults", defaultValue="10",
			description="Sets the maximum number of results one is " +
					"interested in. (Setting this to a lower value may " +
					"increase performance as the learning algorithm has to " +
					"store/evaluate/beautify less descriptions).")
	private int maxNrOfResults = 10;

	@ConfigOption(name="noisePercentage", defaultValue="0.0",
			description="the (approximated) percentage of noise within the examples")
	private double noisePercentage = 0.0;

	@ConfigOption(name="operator", description="the refinement operator instance to use")
	private LengthLimitedRefinementOperator operator;

	@ConfigOption(name="reuseExistingDescription", defaultValue="false",
			description="If true, the algorithm tries to find a good " +
					"starting point close to an existing definition/super " +
					"class of the given class in the knowledge base.")
	private boolean reuseExistingDescription = false;

	@ConfigOption(name="singleSuggestionMode", defaultValue="false",
			description="Use this if you are interested in only one suggestion " +
					"and your learning problem has many (more than 1000) examples.")
	private boolean singleSuggestionMode;

	@ConfigOption(name="startClass", defaultValue="owl:Thing",
			description="You can specify a start class for the algorithm. To " +
					"do this, you have to use Manchester OWL syntax without " +
					"using prefixes.")
	private OWLClassExpression startClass;

	@ConfigOption(name="stopOnFirstDefinition", defaultValue="false",
			description="algorithm will terminate immediately when a correct " +
					"definition is found")
	private boolean stopOnFirstDefinition = false;

	@ConfigOption(name="terminateOnNoiseReached", defaultValue="false",
			description="specifies whether to terminate when noise criterion is met")
	private boolean terminateOnNoiseReached = false;

	@ConfigOption(name="useMinimizer", defaultValue="true",
			description="Specifies whether returned expressions should be " +
					"minimised by removing those parts, which are not needed. " +
					"(Basically the minimiser tries to find the shortest " +
					"expression which is equivalent to the learned expression). " +
					"Turning this feature off may improve performance.")
	private boolean useMinimizer = true;
	// </----------------------- component attributes ------------------------>


	// <---------------------- non-component attributes ---------------------->
	private double bestAccuracy = Double.MIN_VALUE;
	private OWLClassExpression bestDescription;
	private OWLClass classToDescribe;
	private double currentHighestAccuracy;

	/**
	 * all descriptions in the search tree plus those which were too weak (for
	 * fast redundancy check) */
	private TreeSet<OWLClassExpression> descriptions;

	/*
	 * examples are either
	 * 1.) instances of the class to describe
	 * 2.) positive examples
	 * 3.) union of pos.+neg. examples depending on the learning problem at
	 *     hand */
	private Set<OWLIndividual> examples;

	private boolean expandAccuracy100Nodes = false;
	private int expressionTestCountLastImprovement;
	private int expressionTests = 0;
	private boolean filterFollowsFromKB = false;

	/**
	 * forces that one solution cannot be subexpression of another expression;
	 * this option is useful to get diversity but it can also suppress quite
	 * useful expressions
	 */
	private boolean forceMutualDifference = false;

	private AbstractDistHeuristic2 heuristic;
	private boolean isClassLearningProblem;
	private boolean isEquivalenceProblem;
	private OWLClassExpressionMinimizer minimizer;
	private int minHorizExp = 0;
	private int maxHorizExp = 0;
	private int maxPendingRequests = 60;

	/** all nodes in the search tree (used for selecting most promising node) */
	private DistOENodeTree2 nodeTree;

	private double noise;
	private int pendingRequests;
	private String processedTreesQueue = "processedTreesQ";
	private long timeLastImprovement = 0;
	private String treesToProcessQueue = "treesToProcessQ";
	private long totalRuntimeNs = 0;
	/**
	 * The runtime of one single task a worker processes is determined by the
	 * size of the node tree and a base factor (in seconds) */
	private int workerRuntimeBase = 6;


	// FIXME: remove
	BufferedWriter mergeStats;
	// </--------------------- non-component attributes ---------------------->

	// <---------------------------- constructors ----------------------------->
	public DistCELOEAMQP() {
	}

	public DistCELOEAMQP(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
	}
	// </--------------------------- constructors ----------------------------->

	// <------------------------- interface methods ------------------------->
	@Override
	public void init() throws ComponentInitException {
		bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);

		// start at owl:Thing by default
		if(startClass == null) {
			startClass = dataFactory.getOWLThing();
		}

		/* copy class hierarchy and modify it such that each class is only
		 * reachable via a single path */
		ClassHierarchy classHierarchy = initClassHierarchy();
		ObjectPropertyHierarchy objectPropertyHierarchy = initObjectPropertyHierarchy();
		DatatypePropertyHierarchy datatypePropertyHierarchy = initDataPropertyHierarchy();

		// if no one injected a heuristic, we use a default one
		if(heuristic == null) {
			heuristic = new DistOEHeuristicRuntime2();
		}

		minimizer = new OWLClassExpressionMinimizer(dataFactory, reasoner);

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
		if(operator instanceof CustomHierarchyRefinementOperator) {
			((CustomHierarchyRefinementOperator)operator).setClassHierarchy(classHierarchy);
			((CustomHierarchyRefinementOperator)operator).setObjectPropertyHierarchy(objectPropertyHierarchy);
			((CustomHierarchyRefinementOperator)operator).setDataPropertyHierarchy(datatypePropertyHierarchy);
		}

		isClassLearningProblem = (learningProblem instanceof ClassLearningProblem);
		noise = noisePercentage/100d;

		if (isClassLearningProblem) {
			ClassLearningProblem problem = (ClassLearningProblem) learningProblem;
			classToDescribe = problem.getClassToDescribe();
			isEquivalenceProblem = problem.isEquivalenceProblem();

			/* generate examples since class learning problem does not have
			 * positive/negative examples
			 */
			examples = reasoner.getIndividuals(classToDescribe);

			/* start class: intersection of super classes for definitions
			 * (since it needs to capture all instances), but owl:Thing for
			 * learning subclasses (since it is superfluous to add super
			 * classes in this case) */
			if (isEquivalenceProblem) {
				Set<OWLClassExpression> existingDefinitions =
						reasoner.getAssertedDefinitions(classToDescribe);

				if (reuseExistingDescription && existingDefinitions.size() > 0) {
					/* the existing definition is reused, which in the simplest
					 * case means to use it as a start class or, if it is
					 * already too specific, generalise it */

					// pick the longest existing definition as candidate
					OWLClassExpression existingDefinition = null;
					int highestLength = 0;

					for (OWLClassExpression exDef : existingDefinitions) {
						int exDefLength = OWLClassExpressionUtils.getLength(exDef);
						if (exDefLength > highestLength) {
							existingDefinition = exDef;
							highestLength = exDefLength;
						}
					}

					LinkedList<OWLClassExpression> startClassCandidates =
							new LinkedList<OWLClassExpression>();
					startClassCandidates.add(existingDefinition);

					if (operator instanceof RhoDRDown)
						((RhoDRDown)operator).setDropDisjuncts(true);

					LengthLimitedRefinementOperator upwardOperator =
							new OperatorInverter(operator);

					// use upward refinement until we find an appropriate start class
					boolean startClassFound = false;
					OWLClassExpression candidate;

					do {
						candidate = startClassCandidates.pollFirst();
						if (((ClassLearningProblem) learningProblem).getRecall(candidate)<1.0) {
							// add upward refinements to list
							Set<OWLClassExpression> refinements = upwardOperator.refine(candidate, OWLClassExpressionUtils.getLength(candidate));
							LinkedList<OWLClassExpression> refinementList = new LinkedList<OWLClassExpression>(refinements);
							startClassCandidates.addAll(refinementList);

						} else {
							startClassFound = true;
						}
					} while(!startClassFound);

					startClass = candidate;

					if(startClass.equals(existingDefinition)) {
						logger.info("Reusing existing class expression " +
								OWLAPIRenderers.toManchesterOWLSyntax(startClass) +
								" as start class for learning algorithm.");
					} else {
						logger.info("Generalised existing class expression " +
								OWLAPIRenderers.toManchesterOWLSyntax(existingDefinition) +
								" to " + OWLAPIRenderers.toManchesterOWLSyntax(startClass) +
								", which is used as start class for the learning algorithm.");
					}

					if(operator instanceof RhoDRDown) {
						((RhoDRDown)operator).setDropDisjuncts(false);
					}

				} else {
					Set<OWLClassExpression> superClasses =
							reasoner.getClassHierarchy().getSuperClasses(classToDescribe, true);

					if(superClasses.size() > 1) {
						startClass = dataFactory.getOWLObjectIntersectionOf(superClasses);

					} else if(superClasses.size() == 1){
						startClass = (OWLClassExpression) superClasses.toArray()[0];

					} else {
						startClass = dataFactory.getOWLThing();
						logger.warn(classToDescribe + " is equivalent to " +
								"owl:Thing. Usually, it is not sensible to " +
								"learn a class expression in this case.");
					}
				}
			}
		} else if(learningProblem instanceof PosOnlyLP) {
			examples = ((PosOnlyLP) learningProblem).getPositiveExamples();

		} else if(learningProblem instanceof PosNegLP) {
			examples = Sets.union(
					((PosNegLP) learningProblem).getPositiveExamples(),
					((PosNegLP) learningProblem).getNegativeExamples());
		}

		try {
			addTopic(processedTreesQueue);
			addTopic(treesToProcessQueue);
			initMessaging();
		} catch (URLSyntaxException | AMQException | JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		stop = false;
		isRunning = true;
		reset();

		if (isMaster()) {
			try {
				startMaster();
			} catch (JMSException e) {
				e.printStackTrace();
				try {
					terminateAgents();
				} catch (JMSException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			startWorker();
		}

		try {
			finalizeMessaging();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	// </------------------------ interface methods ------------------------->


	// <----------------------- non-interface methods ----------------------->
	/**
	 * Add node to search tree if it is not too weak.
	 * ONLY CALLED BY WORKER PROCESSES
	 * @return TRUE if node was added and FALSE otherwise
	 */
	private boolean addNode(OWLClassExpression description, DistOENode2 parentNode) {

		// redundancy check
		boolean nonRedundant = descriptions.add(description);
		if (!nonRedundant) {
			logger.debug(description + " not added to nodes (REDUNDANT)");
			return false;
		}

		// check whether the class expression is allowed
		if (!isDescriptionAllowed(description, parentNode)) {
			logger.debug(description + " not added to nodes (NOT ALLOWED)");
			return false;
		}

		double accuracy = learningProblem.getAccuracyOrTooWeak(description, noise);

		// throw an exception if accuracy is not between 0 and 1 or -1 (too weak)
		if(accuracy > 1.0 || (accuracy < 0.0 && accuracy != -1)) {
			throw new RuntimeException("Invalid accuracy value " + accuracy +
					" for class expression " + description + ". This could be " +
					"caused by a bug in the heuristic measure and should be " +
					"reported to the DL-Learner bug tracker.");
		}

		expressionTests++;

		if (accuracy == -1) return false;

		DistOENode2 node = new DistOENode2(description, accuracy);

		// TODO: remove commented out stuff
//		DistOENode2 node = new DistOENode2(parentNode, description, accuracy);

//		parentNode.addChild(node);
//		nodeTree.add(node);

		nodeTree.add(node, parentNode);

		/* in some cases (e.g. mutation) fully evaluating even a single class
		 * expression is too expensive due to the high number of examples -- so
		 * we just stick to the approximate accuracy */
		if(singleSuggestionMode) {
			if(accuracy > bestAccuracy) {
				bestAccuracy = accuracy;
				bestDescription = description;
				logger.info("more accurate (" + dfPercent.format(bestAccuracy) +
						") class expression found: " + descriptionToString(bestDescription));
			}

			return true;
		}

		/* maybe add to best descriptions (method keeps set size fixed); we
		 * need to make sure that this does not get called more often than
		 * necessary since rewriting is expensive */
		boolean isCandidate = !bestEvaluatedDescriptions.isFull();
		if (!isCandidate) {
			EvaluatedDescription worst = bestEvaluatedDescriptions.getWorst();
			double accThreshold = worst.getAccuracy();

			isCandidate =
				(accuracy > accThreshold) ||
					(accuracy >= accThreshold &&
						OWLClassExpressionUtils.getLength(description) < worst.getDescriptionLength());
		}

		if (isCandidate) {
			OWLClassExpression niceDescription = rewriteNode(node);

			if (niceDescription.equals(classToDescribe)) return false;

			if (!isDescriptionAllowed(niceDescription, node)) return false;

			/* another test: none of the other suggested descriptions should
			 * be a subdescription of this one unless accuracy is
			 * different => comment: on the one hand, this appears to be too
			 * strict, because once A is a solution then everything containing
			 * A is not a candidate; on the other hand this suppresses many
			 * meaningless extensions of A
			 */
			boolean shorterDescriptionExists = false;

			if (forceMutualDifference) {
				for (EvaluatedDescription<? extends Score> ed : bestEvaluatedDescriptions.getSet()) {
					double accDiff = Math.abs(ed.getAccuracy() - accuracy);
					boolean isSubDescription =
							ConceptTransformation.isSubdescription(
									niceDescription, ed.getDescription());

					if (accDiff >= 0.00001 && isSubDescription) {
						shorterDescriptionExists = true;
						break;
					}
				}
			}

			if (!shorterDescriptionExists) {
				if (!filterFollowsFromKB ||
						!((ClassLearningProblem) learningProblem).followsFromKB(niceDescription)) {

					bestEvaluatedDescriptions.add(niceDescription, accuracy, learningProblem);
				}
			}
		}
		return true;
	}

	public double getCurrentlyBestAccuracy() {
		return bestEvaluatedDescriptions.getBest().getAccuracy();
	}

	/**
	 * Expands the best node of those, which have not achieved 100% accuracy
	 * already and have a horizontal expansion equal to their length
	 * (rationale: further extension is likely to add irrelevant syntactical
	 * constructs)
	 */
	private DistOENode2 getNextNodeToExpand() {
		Iterator<DistOENode2> it = nodeTree.descendingIterator();

		while(it.hasNext()) {
			DistOENode2 node = it.next();
			boolean horizExpLtDescLen = node.getHorizontalExpansion() <
					OWLClassExpressionUtils.getLength(node.getDescription());

			if (nodeTree.isInUse(node)) continue;

			if (expandAccuracy100Nodes && horizExpLtDescLen) {
					return node;

			} else {
				if (node.getAccuracy() < 1.0 || horizExpLtDescLen) return node;
			}
		}

		return null;
	}

	/**
	 * Expands the best node of those, which have not achieved 100% accuracy
	 * already and have a horizontal expansion equal to their length
	 * (rationale: further extension is likely to add irrelevant syntactical
	 * constructs)
	 */
	private DistOENode2 getNextNodeToExpandWorker() {
		Iterator<DistOENode2> it = nodeTree.descendingIterator();

		while(it.hasNext()) {
			DistOENode2 node = it.next();
			boolean horizExpLtDescLen = node.getHorizontalExpansion() <
					OWLClassExpressionUtils.getLength(node.getDescription());

			if (expandAccuracy100Nodes && horizExpLtDescLen) {
					return node;

			} else {
				if (node.getAccuracy() < 1.0 || horizExpLtDescLen) return node;
			}
		}

//		System.out.println("NO NEXT NODE IN " + nodeTree.getRoot().toTreeString() + " after " + trials + " trials");
		return null;
	}

	private boolean isDescriptionAllowed(OWLClassExpression description, DistOENode2 parentNode) {
		if (isClassLearningProblem) {
			if (isEquivalenceProblem) {
				// the class to learn must not appear on the outermost property level
				if (occursOnFirstLevel(description, classToDescribe))
					return false;

				if (occursOnSecondLevel(description, classToDescribe))
					return false;

			} else {
				/* none of the superclasses of the class to learn must appear
				 * on the outermost property level */
				TreeSet<OWLClassExpression> toTest = new TreeSet<OWLClassExpression>();
				toTest.add(classToDescribe);

				while (!toTest.isEmpty()) {
					OWLClassExpression d = toTest.pollFirst();
					if (occursOnFirstLevel(description, d))
						return false;

					toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
				}
			}

		} else if (learningProblem instanceof ClassAsInstanceLearningProblem) {
			return true;  // FIXME
		}

		// perform forall sanity tests
		if (parentNode != null &&
				(ConceptTransformation.getForallOccurences(description) >
					ConceptTransformation.getForallOccurences(parentNode.getDescription()))) {

			/* we have an additional \forall construct, so we now fetch the
			 * contexts in which it occurs */
			SortedSet<PropertyContext> contexts = ConceptTransformation.getForallContexts(description);
			SortedSet<PropertyContext> parentContexts = ConceptTransformation.getForallContexts(parentNode.getDescription());
			contexts.removeAll(parentContexts);

			/* we now have to perform sanity checks: if \forall is used, then
			 * there should be at least on class instance which has a filler at
			 * the given context */
			for(PropertyContext context : contexts) {
				// transform [r,s] to \exists r.\exists s.\top
				OWLClassExpression existentialContext = context.toExistentialContext();

				boolean fillerFound = false;

				if(reasoner.getClass().isAssignableFrom(SPARQLReasoner.class)) {
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

				/* if we do not find a filler, this means that putting \forall
				 * at that position is not meaningful */
				if(!fillerFound) return false;
			}
		}

		return true;
	}

	/**
	 * determine whether a named class occurs on the outermost level, i.e.
	 * property depth 0 (it can still be at higher depth, e.g. if intersections
	 * are nested in unions)
	 */
	private boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
		if (cls.isOWLThing()) return false;

		return (description instanceof OWLNaryBooleanClassExpression &&
				((OWLNaryBooleanClassExpression) description).getOperands().contains(cls));
	}

	/**
	 * determine whether a named class occurs on the outermost level, i.e.
	 * property depth 0 (it can still be at higher depth, e.g. if intersections
	 * are nested in unions)
	 */
	private boolean occursOnSecondLevel(OWLClassExpression description, OWLClassExpression cls) {
		return false;
	}

	private void printAlgorithmRunStats() {
		if (stop) {
			logger.info("Algorithm stopped ("+expressionTests+" descriptions " +
					"tested). " + nodeTree.size() + " nodes in the search tree.\n");
		} else {
			totalRuntimeNs = System.nanoTime()-nanoStartTime;
			logger.info("Algorithm terminated successfully (time: " +
					Helper.prettyPrintNanoSeconds(totalRuntimeNs) + ", " +
					expressionTests+" descriptions tested, "  +
					nodeTree.size() + " nodes in the search tree).\n");
			logger.info(reasoner.toString());
		}
	}

	private void processRecvdTreeMaster(NodeTreeContainer2 treeContainer) {
		pendingRequests--;
		DistOENodeTree2 remoteTree = treeContainer.getTree();
		Monitor mon = MonitorFactory.getTimeMonitor("treemerge");
		mon.start();
		nodeTree.mergeWithAndUnblock(remoteTree);
		mon.stop();
		double duration = mon.getLastValue();
		String line = nodeTree.size() + "\t" + duration;
		try {
			mergeStats.write(line);
			mergeStats.newLine();
			mergeStats.flush();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				terminateAgents();
			} catch (JMSException e1) {
				e1.printStackTrace();
			}
			System.exit(1);
		}

		double remoteBestAcc = treeContainer.getBestAccuracy();
		OWLClassExpression remoteBestDescription = treeContainer.getBestDescription();

		if(remoteBestAcc > bestAccuracy) {
			bestAccuracy = remoteBestAcc;
			bestDescription = remoteBestDescription;
			logger.info("more accurate (" + dfPercent.format(bestAccuracy) + ") class expression found: " + descriptionToString(bestDescription)); // + getTemporaryString(bestDescription));
		}

		EvaluatedDescriptionSet remoteBestEvaluatedDescriptions =
				treeContainer.getBestEvaluatedDescriptions();

		for (EvaluatedDescription ed : remoteBestEvaluatedDescriptions.getSet()) {
			bestEvaluatedDescriptions.add(ed);
		}
	}

	private void processRecvdTreeWorker(NodeTreeContainer2 treeContainer) {
		nodeTree = treeContainer.getTree();
		startClass = nodeTree.getRoot().getDescription();

		if (operator instanceof RhoDRDown) {
			operator = new RhoDRDown((RhoDRDown) operator);
			((RhoDRDown) operator).setStartClass(startClass);
			try {
				operator.init();
			} catch (ComponentInitException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("START CLASS : " + startClass);
		maxExecutionTimeInSeconds = Math.max(
				workerRuntimeBase,
				(int) (workerRuntimeBase * Math.log(nodeTree.size())));
//		System.out.println("MAX EXEC TIME: " + maxExecutionTimeInSeconds);
		startWorkerCELOE();
	}

	/** expand node horizontically */
	private TreeSet<OWLClassExpression> refineNode(DistOENode2 node) {
		/* we have to remove and add the node since its heuristic evaluation
		 * changes through the expansion (you *must not* include any criteria
		 * in the heuristic which are modified outside of this method,
		 * otherwise you may see rarely occurring but critical false ordering
		 * in the nodes set) */
		nodeTree.remove(node);

		int horizExp = node.getHorizontalExpansion();
		TreeSet<OWLClassExpression> refinements =
				(TreeSet<OWLClassExpression>) operator.refine(
						node.getDescription(), horizExp+1);

		node.incHorizontalExpansion();
		node.setRefinementCount(refinements.size());

		nodeTree.add(node);

		return refinements;
	}

	private void reset() {
		bestAccuracy = Double.MIN_VALUE;
		bestDescription = null;
		bestEvaluatedDescriptions.getSet().clear();
		descriptions = new TreeSet<OWLClassExpression>();
		expressionTestCountLastImprovement = 0;
		expressionTests = 0;
		minHorizExp = 0;
		maxHorizExp = 0;

		if (nodeTree == null) nodeTree = new DistOENodeTree2();
		else nodeTree.reset();

		timeLastImprovement = 0;
		totalRuntimeNs = 0;
	}

	/** checks whether the node is a potential solution candidate */
	private OWLClassExpression rewriteNode(DistOENode2 node) {
		OWLClassExpression description = node.getDescription();
		OWLClassExpression niceDescription;

		if (useMinimizer) niceDescription = minimizer.minimizeClone(description);

		else niceDescription = description;

		niceDescription = ConceptTransformation.replaceRange(niceDescription, reasoner);

		return niceDescription;
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

	private void startMaster() throws JMSException {
		nanoStartTime = System.nanoTime();
		currentHighestAccuracy = 0.0;
		pendingRequests = 0;
		DistOENode2 nextNode;

		// TODO: remove
		try {
			mergeStats = new BufferedWriter(new FileWriter(new File("mergeStats.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("start class:" + startClass);

		// <addNode replacement>
		double accuracy = learningProblem.getAccuracyOrTooWeak(startClass, noise);

		DistOENode2 node = new DistOENode2(startClass, accuracy);
		nodeTree.setRoot(node);



		bestAccuracy = accuracy;
		bestDescription = startClass;
		if(!filterFollowsFromKB ||
				!((ClassLearningProblem)learningProblem).followsFromKB(startClass)) {
			bestEvaluatedDescriptions.add(startClass, accuracy, learningProblem);
		}
		// </addNode replacement>

		NodeTreeContainer2 treeRecvContainer;

		while (!terminationCriteriaSatisfied()) {

			if (Math.random() <= 0.66) {
				treeRecvContainer = (NodeTreeContainer2) nonBlockingReceive(processedTreesQueue);
				if (treeRecvContainer != null) processRecvdTreeMaster(treeRecvContainer);
			}

			if (pendingRequests > maxPendingRequests) continue;

			nextNode = getNextNodeToExpand();

			if (nextNode == null) continue;

			DistOENodeTree2 subTree = nodeTree.getSubTreeCopyAndSetInUse(nextNode);
			NodeTreeContainer2 treeSendContainer = new NodeTreeContainer2(subTree);

			nonBlockingSend(treeSendContainer, treesToProcessQueue);
			pendingRequests++;
		}

		terminateAgents();

		printAlgorithmRunStats();
		logger.info("solutions:\n" + getSolutionString());
		try {
			mergeStats.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startWorker() {
		while (true) {
			if (checkTerminateMsg()) break;

			reset();
			currentHighestAccuracy = 0.0;

			NodeTreeContainer2 treeContainer =
					(NodeTreeContainer2) blockingReceive(treesToProcessQueue);
			if (treeContainer == null) continue;

			processRecvdTreeWorker(treeContainer);

			NodeTreeContainer2 resultsTreeContainer =
					new NodeTreeContainer2(nodeTree, bestAccuracy,
							bestDescription, bestEvaluatedDescriptions);
			nonBlockingSend(resultsTreeContainer, processedTreesQueue);
		}

		logger.info("worker " + myID + " terminated");
	}

	/**
	 * A copy-over from the original start method in CELOE ( + replacing OENode
	 * with DistOENode)
	 */
	private void startWorkerCELOE() {
//		System.out.println(nodeTree.getRoot().toTreeString());
		stop = false;
		isRunning = true;
		nanoStartTime = System.nanoTime();

		currentHighestAccuracy = 0.0;
		DistOENode2 nextNode;

		while (!terminationCriteriaSatisfied()) {
			showIfBetterSolutionsFound();

			// chose best node according to heuristics
			nextNode = getNextNodeToExpandWorker();
			if (nextNode == null) break;
			// FIXME: seems nextNode can be null here
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
				if(length > horizExp &&
						OWLClassExpressionUtils.getDepth(refinement) <= maxDepth) {
					// add node to search tree
					addNode(refinement, nextNode);
				}
			}

			showIfBetterSolutionsFound();

			// update the global min and max horizontal expansion values
			updateMinMaxHorizExp(nextNode);

			// write the search tree (if configured)
//			if (writeSearchTree) {
//				writeSearchTree(refinements);
//			}
		}

		if(singleSuggestionMode) {
			bestEvaluatedDescriptions.add(bestDescription, bestAccuracy, learningProblem);
		}

		isRunning = false;
	}

	private boolean terminationCriteriaSatisfied() {
		return stop ||
			(maxExecutionTimeInSeconds != 0 &&
				((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSeconds * 1000000000l))) ||
			(maxClassExpressionTestsAfterImprovement != 0 &&
				(expressionTests - expressionTestCountLastImprovement >= maxClassExpressionTestsAfterImprovement)) ||
			(maxClassExpressionTests != 0 && (expressionTests >= maxClassExpressionTests)) ||
			(maxExecutionTimeInSecondsAfterImprovement != 0 &&
				(System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSecondsAfterImprovement * 1000000000l)) ||
			(terminateOnNoiseReached && (100 * getCurrentlyBestAccuracy() >= 100 - noisePercentage)) ||
			(stopOnFirstDefinition && (getCurrentlyBestAccuracy() >= 1));
	}


	private void updateMinMaxHorizExp(DistOENode2 node) {
		int newHorizExp = node.getHorizontalExpansion();

		// update maximum value
		maxHorizExp = Math.max(maxHorizExp, newHorizExp);

		// we just expanded a node with minimum horizontal expansion;
		// we need to check whether it was the last one
		if(minHorizExp == newHorizExp - 1) {

			// the best accuracy that a node can achieve
			double scoreThreshold = heuristic.getNodeScore(node) + 1 - node.getAccuracy();

			for(DistOENode2 n : nodeTree.descendingSet()) {
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
		}
	}
	// </---------------------- non-interface methods ----------------------->


	// <--------------------------- getter/setter --------------------------->
	public int getMaxClassExpressionTestsAfterImprovement() {
		return maxClassExpressionTestsAfterImprovement;
	}

	public void setMaxClassExpressionTestsAfterImprovement(
			int maxClassExpressionTestsAfterImprovement) {
		this.maxClassExpressionTestsAfterImprovement = maxClassExpressionTestsAfterImprovement;
	}

	public int getMaxClassExpressionTests() {
		return maxClassExpressionTests;
	}

	public void setMaxClassExpressionTests(int maxClassExpressionTests) {
		this.maxClassExpressionTests = maxClassExpressionTests;
	}

	public double getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(double maxDepth) {
		this.maxDepth = maxDepth;
	}

	@Override
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	@Override
	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public int getMaxExecutionTimeInSecondsAfterImprovement() {
		return maxExecutionTimeInSecondsAfterImprovement;
	}

	public void setMaxExecutionTimeInSecondsAfterImprovement(
			int maxExecutionTimeInSecondsAfterImprovement) {
		this.maxExecutionTimeInSecondsAfterImprovement = maxExecutionTimeInSecondsAfterImprovement;
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

	public LengthLimitedRefinementOperator getOperator() {
		return operator;
	}

	public void setOperator(LengthLimitedRefinementOperator operator) {
		this.operator = operator;
	}

	public boolean isReuseExistingDescription() {
		return reuseExistingDescription;
	}

	public void setReuseExistingDescription(boolean reuseExistingDescription) {
		this.reuseExistingDescription = reuseExistingDescription;
	}

	public boolean isSingleSuggestionMode() {
		return singleSuggestionMode;
	}

	public void setSingleSuggestionMode(boolean singleSuggestionMode) {
		this.singleSuggestionMode = singleSuggestionMode;
	}

	public OWLClassExpression getStartClass() {
		return startClass;
	}

	public void setStartClass(OWLClassExpression startClass) {
		this.startClass = startClass;
	}

	public boolean isStopOnFirstDefinition() {
		return stopOnFirstDefinition;
	}

	public void setStopOnFirstDefinition(boolean stopOnFirstDefinition) {
		this.stopOnFirstDefinition = stopOnFirstDefinition;
	}

	public boolean isTerminateOnNoiseReached() {
		return terminateOnNoiseReached;
	}

	public void setTerminateOnNoiseReached(boolean terminateOnNoiseReached) {
		this.terminateOnNoiseReached = terminateOnNoiseReached;
	}

	@Override
	public boolean isUseMinimizer() {
		return useMinimizer;
	}

	@Override
	public void setUseMinimizer(boolean useMinimizer) {
		this.useMinimizer = useMinimizer;
	}

	public AbstractDistHeuristic2 getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(AbstractDistHeuristic2 heuristic) {
		this.heuristic = heuristic;
	}

	// </-------------------------- getter/setter --------------------------->
	// <---------------------------- main method ---------------------------->
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			throw new Exception("Please provide an worker ID (int) as first " +
					"parameter and an indicator whether DistScoreCELOE runs " +
					"as master (1) or worker (0) as second parameter");
		}

		int id = Integer.parseInt(args[0]);
		boolean isMaster = Integer.parseInt(args[1]) > 0;

		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		File file = new File("father.owl");
		OWLClass classToDescribe = new OWLClassImpl(
				IRI.create("http://example.com/father#male"));

		OWLOntology ontology = OWLManager.createOWLOntologyManager().
				loadOntologyFromOntologyDocument(file);

		AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();

		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
		baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
		baseReasoner.init();
		ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
		rc.setReasonerComponent(baseReasoner);
		rc.init();

		ClassLearningProblem lp = new ClassLearningProblem(rc);
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

		// (male ⊓ (∀ hasChild.⊤)) ⊔ (∃ hasChild.(∃ hasChild.male))
		OWLDataFactory df = new OWLDataFactoryImpl();
		OWLClassExpression ce = df.getOWLObjectUnionOf(
				df.getOWLObjectIntersectionOf(
						df.getOWLClass(
								IRI.create("http://example.com/father#male")),
						df.getOWLObjectAllValuesFrom(
								df.getOWLObjectProperty(
										IRI.create("http://example.com/father#hasChild")),
										df.getOWLThing())),
				df.getOWLObjectSomeValuesFrom(
						df.getOWLObjectProperty(
								IRI.create("http://example.com/father#hasChild")),
						df.getOWLObjectSomeValuesFrom(
								df.getOWLObjectProperty(
										IRI.create("http://example.com/father#hasChild")),
								df.getOWLClass(
										IRI.create("http://example.com/father#male"))
								)
						)
				);

		DistCELOEAMQP alg =
				new DistCELOEAMQP(lp, rc);
		alg.setMaxExecutionTimeInSeconds(60);
		alg.setOperator(op);
//		alg.setWriteSearchTree(true);
//		alg.setSearchTreeFile("log/search-tree.log");
//		alg.setReplaceSearchTree(true);
		// AMQP specific
		alg.updateAMQPSettings("amqp.properties");
		if (isMaster) alg.setMaster();
		alg.setAgentID(id);

		alg.init();
		alg.start();
	}
	// </--------------------------- main method ---------------------------->
}
