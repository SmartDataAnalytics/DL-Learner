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

package org.dllearner.distributed.amqp;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.qpid.QpidException;
import org.apache.qpid.url.URLSyntaxException;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.learningproblems.ClassAsInstanceLearningProblem;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.refinementoperators.CustomHierarchyRefinementOperator;
import org.dllearner.refinementoperators.CustomStartRefinementOperator;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.ReasoningBasedRefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.owl.PropertyContext;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * The CELOE (Class Expression Learner for Ontology Engineering) algorithm.
 * It adapts and extends the standard supervised learning algorithm for the
 * ontology engineering use case.
 *
 * @author Jens Lehmann
 *
 */
@SuppressWarnings("CloneDoesntCallSuperClone")
public class CELOE extends AbstractCELA {

	private static final Logger logger = LoggerFactory.getLogger(CELOE.class);

	private boolean isRunning = false;
	private boolean stop = false;

	// <------------------------ component attributes ------------------------>
	// E
	@ConfigOption(defaultValue="false",  description="whether to try and " +
			"refine solutions which already have accuracy value of 1")
	private boolean expandAccuracy100Nodes = false;

	// F
	@ConfigOption(name="filterDescriptionsFollowingFromKB",
			defaultValue="false", description="If true, then the results " +
					"will not contain suggestions, which already follow " +
					"logically from the knowledge base. Be careful, since " +
					"this requires a potentially expensive consistency " +
					"check for candidate solutions.")
	private boolean filterDescriptionsFollowingFromKB = false;

	// H
	@ConfigOption(name="heuristic", defaultValue="celoe_heuristic")
	private OEHeuristicRuntime heuristic;

	// M
	@ConfigOption(name="maxClassExpressionTests", defaultValue="0",
			description="The maximum number of candidate hypothesis the " +
					"algorithm is allowed to test (0 = no limit). The " +
					"algorithm will stop afterwards. (The real number of " +
					"tests can be slightly higher, because this criterion " +
					"usually won't be checked after each single test.)")
	private int maxClassExpressionTests = 0;

	@ConfigOption(name="maxClassExpressionTestsAfterImprovement",
			defaultValue="0", description="The maximum number of candidate " +
					"hypothesis the algorithm is allowed after an " +
					"improvement in accuracy (0 = no limit). The algorithm " +
					"will stop afterwards. (The real number of tests can be " +
					"slightly higher, because this criterion usually won't " +
					"be checked after each single test.)")
	private int maxClassExpressionTestsAfterImprovement = 0;

	@ConfigOption(defaultValue="0",
			name="maxExecutionTimeInSecondsAfterImprovement",
			description = "maximum execution of the algorithm in seconds")
	private int maxExecutionTimeInSecondsAfterImprovement = 0;

	@ConfigOption(name="maxDepth", defaultValue="7",
			description="maximum depth of description")
	private double maxDepth = 7;

	@ConfigOption(name="maxNrOfResults", defaultValue="10",
			description="Sets the maximum number of results one is " +
					"interested in. (Setting this to a lower value may " +
					"increase performance as the learning algorithm has to " +
					"store/evaluate/beautify less descriptions).")
	private int maxNrOfResults = 10;

	// N
	@ConfigOption(name="noisePercentage", defaultValue="0.0",
			description="the (approximated) percentage of noise within the " +
					"examples")
	private double noisePercentage = 0.0;

	// O
	@ConfigOption(description="the refinement operator instance to use")
	private LengthLimitedRefinementOperator operator;

	// R
	@ConfigOption(name="replaceSearchTree", defaultValue="false",
			description="specifies whether to replace the search tree in " +
					"the log file after each run or append the new search tree")
	private boolean replaceSearchTree = false;

	@ConfigOption(name="reuseExistingDescription", defaultValue="false",
			description="If true, the algorithm tries to find a good " +
					"starting point close to an existing definition/super " +
					"class of the given class in the knowledge base.")
	private boolean reuseExistingDescription = false;

	// S
	@ConfigOption(name="searchTreeFile", defaultValue="log/searchTree.txt",
			description="file to use for the search tree")
	private String searchTreeFile = "log/searchTree.txt";

	@ConfigOption(name="singleSuggestionMode", defaultValue="false",
			description="Use this if you are interested in only one " +
					"suggestion and your learning problem has many (more " +
					"than 1000) examples.")
	private boolean singleSuggestionMode;

	/** the class with which we start the refinement process */
	@ConfigOption( name="startClass", defaultValue = "owl:Thing",
			description="You can specify a start class for the algorithm. " +
					"To do this, you have to use Manchester OWL syntax " +
					"either with full IRIs or prefixed IRIs.",
			exampleValue="ex:Male or http://example.org/ontology/Female")
	private OWLClassExpression startClass;

	@ConfigOption(name="stopOnFirstDefinition", defaultValue="false",
			description="algorithm will terminate immediately when a " +
					"correct definition is found")
	private boolean stopOnFirstDefinition = false;

	// T
	@ConfigOption(name="terminateOnNoiseReached", defaultValue="false",
			description="specifies whether to terminate when noise " +
					"criterion is met")
	private boolean terminateOnNoiseReached = false;

	// W
	@ConfigOption(name="writeSearchTree", defaultValue="false",
			description="specifies whether to write a search tree")
	private boolean writeSearchTree = false;

	// </----------------------- component attributes ------------------------>


	// <---------------------- non-component attributes ---------------------->
	private double bestAccuracy = Double.MIN_VALUE;
	private OWLClassExpression bestDescription;
	private OWLClass classToDescribe;  // TODO: is this needed?
	private double currentHighestAccuracy;

	/**
	 * all descriptions in the search tree plus those which were too weak (for
	 * fast redundancy check) */
	private TreeSet<OWLClassExpression> descriptions;

	/**
	 * examples are either
	 * 1.) instances of the class to describe
	 * 2.) positive examples
	 * 3.) union of pos. + neg. examples depending on the learning problem at
	 *     hand
	 */
	private Set<OWLIndividual> examples;

	private int expressionTestCountLastImprovement;
	private int expressionTests = 0;
	private boolean filterFollowsFromKB = false;

	/**
	 * forces that one solution cannot be subexpression of another expression;
	 * this option is useful to get diversity but it can also suppress quite
	 * useful expressions
	 */
	private boolean forceMutualDifference = false;

	private boolean isClassLearningProblem;
	private boolean isEquivalenceProblem;
	private int minHorizExp = 0;
	private int maxHorizExp = 0;

	/**
	 * The number of max pending requests determines when the master should
	 * stop adding worker messages to the queue. Accordingly, setting
	 * maxPendingRequests to a low number will result in a higher probability
	 * of workers not having anything to work on. A higher value of
	 * maxPendingRequests on the other hand might fill the workers queue
	 * unnecessarily. In this case a lot of nodes in the global search tree
	 * are blocked and the corresponding worker task messages will eventually
	 * be processed even though a far better path in the search tree might be
	 * found already.
	 *
	 * A good number might be 2-3 times the number of workers. This should
	 * guarantee that all workers should get something to work on even though
	 * the master might be very busy on something else for some time.
	 */
	private int maxPendingRequests = 60;

	private double noise;

	/**
	 * The number of worker requests that were put in the workers queue but no
	 * answer was received, yet */
	private int pendingRequests;

	private int receivedMessagesCount;
	private boolean runMaster = false;
	private SearchTree searchTree;

	@SuppressWarnings("unused")
	private long timeLastImprovement = 0;

	private long totalRuntimeNs = 0;
	// </--------------------- non-component attributes ---------------------->


	// <--------------------------- constructors ----------------------------->
	public CELOE() {}

	public CELOE(AbstractClassExpressionLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
	}
	// </-------------------------- constructors ----------------------------->

	// <-------------------------- interface methods ------------------------->
	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		EvaluatedDescription<? extends Score> ed =
				getCurrentlyBestEvaluatedDescription();
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

	@Override
	public void init() throws ComponentInitException {
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();

		if(maxExecutionTimeInSeconds != 0 &&
				maxExecutionTimeInSecondsAfterImprovement != 0) {

			maxExecutionTimeInSeconds = Math.min(
					maxExecutionTimeInSeconds,
					maxExecutionTimeInSecondsAfterImprovement);
		}

		ClassHierarchy classHierarchy = initClassHierarchy();
		ObjectPropertyHierarchy objectPropertyHierarchy = initObjectPropertyHierarchy();
		DatatypePropertyHierarchy datatypePropertyHierarchy = initDataPropertyHierarchy();

		// if no one injected a heuristic, we use a default one
		if(heuristic == null) {
			heuristic = new OEHeuristicRuntime();
			heuristic.init();
		}

		/* - minimizer only used in rewrite(...)
		 * - rewrite called in addNode(...)
		 * - addNode(...) called in master and worker (even though there will
		 *   be a special version of addNode(...) for the worker */
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
				this.startClass = OWLAPIUtils.classExpressionPropertyExpander(
						this.startClass, reasoner, dataFactory);

			} catch (Exception e) {
				logger.warn("Error parsing start class.", e);
				logger.warn("Using owl:Thing instead.");
				this.startClass = dataFactory.getOWLThing();
			}
		}

		bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);

		isClassLearningProblem = (learningProblem instanceof ClassLearningProblem);

		noise = noisePercentage / 100d;

		// (filterFollowsFromKB is automatically set to false if the problem
		// is not a class learning problem
		filterFollowsFromKB = filterDescriptionsFollowingFromKB && isClassLearningProblem;

		// actions specific to ontology engineering
		if (isClassLearningProblem) {
			ClassLearningProblem problem = (ClassLearningProblem) learningProblem;
			classToDescribe = problem.getClassToDescribe();
			isEquivalenceProblem = problem.isEquivalenceProblem();

			examples = reasoner.getIndividuals(classToDescribe);

		} else if (learningProblem instanceof PosOnlyLP) {
			examples = ((PosOnlyLP) learningProblem).getPositiveExamples();

		} else if (learningProblem instanceof PosNegLP) {
			examples = Sets.union(
					((PosNegLP) learningProblem).getPositiveExamples(),
					((PosNegLP) learningProblem).getNegativeExamples());
		}

		// create a refinement operator and pass all configuration
		// variables to it
		if (operator == null) {
			// we use a default operator and inject the class hierarchy for now
			operator = new RhoDRDown();
			((CustomStartRefinementOperator) operator).setStartClass(startClass);
			((ReasoningBasedRefinementOperator) operator).setReasoner(reasoner);
			operator.init();
		}

		if (operator instanceof CustomHierarchyRefinementOperator) {
			((CustomHierarchyRefinementOperator) operator).setClassHierarchy(classHierarchy);
			((CustomHierarchyRefinementOperator) operator).setObjectPropertyHierarchy(objectPropertyHierarchy);
			((CustomHierarchyRefinementOperator) operator).setDataPropertyHierarchy(datatypePropertyHierarchy);
		}
	}

	@Override
	public void start() {
		stop = false;
		isRunning = true;
		reset();

		if (runMaster()) {
			Master master = new Master();
			try {
				master.init();
			} catch (URLSyntaxException | JMSException | QpidException e2) {
				e2.printStackTrace();

				try {
					master.terminateWorkers();

				} catch (JMSException e1) {
					e1.printStackTrace();
				}
			}
			master.start();

			try {
				master.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				master.terminateWorkers();

			} catch (JMSException e1) {
				e1.printStackTrace();
			}

			try {
				master.finalizeMessaging();
			} catch (JMSException e) {
				e.printStackTrace();
			}

		} else {
			Worker worker = new Worker();
			try {
				worker.init();
			} catch (URLSyntaxException | QpidException | JMSException e1) {
				e1.printStackTrace();
				try {
					worker.finalizeMessaging();
				} catch (JMSException e) {
					e.printStackTrace();
				}
				System.exit(1);
			}
			worker.start();

			try {
				worker.finalizeMessaging();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		stop = true;
	}
	// </------------------------- interface methods ------------------------->


	// <------------------------ non-interface methods ----------------------->
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
			throw new RuntimeException("Invalid accuracy value " + accuracy +
					" for class expression " + description + ". This could " +
					"be caused by a bug in the heuristic measure and should " +
					"be reported to the DL-Learner bug tracker.");
		}

		expressionTests++;

		// return FALSE if 'too weak'
		if(accuracy == -1) {
			return false;
		}

		OENode node = new OENode(description, accuracy);
		searchTree.addNode(parentNode, node);

		/* in some cases (e.g. mutation) fully evaluating even a single class
		 * expression is too expensive due to the high number of
		 * examples -- so we just stick to the approximate accuracy */
		if(singleSuggestionMode) {
			if(accuracy > bestAccuracy) {
				bestAccuracy = accuracy;
				bestDescription = description;
				logger.info("more accurate (" +
						dfPercent.format(bestAccuracy) + ") class expression " +
						"found: " + descriptionToString(bestDescription));
			}
			return true;
		}

		/* maybe add to best descriptions (method keeps set size fixed); we
		 * need to make sure that this does not get called more often than
		 * necessary since rewriting is expensive */
		boolean isCandidate = !bestEvaluatedDescriptions.isFull();

		if(!isCandidate) {
			EvaluatedDescription<? extends Score> worst = bestEvaluatedDescriptions.getWorst();
			double accThreshold = worst.getAccuracy();

			isCandidate =
				(accuracy > accThreshold ||
				(accuracy >= accThreshold &&
					OWLClassExpressionUtils.getLength(description) < worst.getDescriptionLength()));
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
					if(Math.abs(ed.getAccuracy()-accuracy) <= 0.00001 && ConceptTransformation.isSubdescription(niceDescription, ed.getDescription())) {
						shorterDescriptionExists = true;
						break;
					}
				}
			}


			if(!shorterDescriptionExists) {
				if(!filterFollowsFromKB || !((ClassLearningProblem)learningProblem).followsFromKB(niceDescription)) {
					bestEvaluatedDescriptions.add(niceDescription, accuracy, learningProblem);
				}
			}
		}

		return true;
	}

	/**
	 * Compute the start class in the search space from which the refinement
	 * will start. We use the intersection of super classes for definitions
	 * (since it needs to capture all instances), but owl:Thing for learning
	 * subclasses (since it is superfluous to add super classes in this case)
	 */
	private OWLClassExpression computeStartClass() {
		OWLClassExpression startClass = dataFactory.getOWLThing();

		if(isClassLearningProblem) {
			if(isEquivalenceProblem) {
				Set<OWLClassExpression> existingDefinitions =
						reasoner.getAssertedDefinitions(classToDescribe);

				if (reuseExistingDescription && (existingDefinitions.size() > 0)) {
					/* the existing definition is reused, which in the
					 * simplest case means to use it as a start class or, if
					 * it is already too specific, generalise it */

					// pick the longest existing definition as candidate
					OWLClassExpression existingDefinition = null;
					int highestLength = 0;

					for (OWLClassExpression exDef : existingDefinitions) {
						if (OWLClassExpressionUtils.getLength(exDef) > highestLength) {
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
							Set<OWLClassExpression> refinements =
									upwardOperator.refine(
											candidate, OWLClassExpressionUtils.getLength(candidate));

							LinkedList<OWLClassExpression> refinementList =
									new LinkedList<>(refinements);

							startClassCandidates.addAll(refinementList);

						} else {
							startClassFound = true;
						}
					} while (!startClassFound);

					startClass = candidate;

					if (startClass.equals(existingDefinition)) {
						logger.info("Reusing existing class expression " +
								OWLAPIRenderers.toManchesterOWLSyntax(startClass) +
								" as start class for learning algorithm.");
					} else {
						logger.info("Generalised existing class expression " +
								OWLAPIRenderers.toManchesterOWLSyntax(existingDefinition) +
								" to " +
								OWLAPIRenderers.toManchesterOWLSyntax(startClass) +
								", which is used as start class for the " +
								"learning algorithm.");
					}

					if (operator instanceof RhoDRDown) {
						((RhoDRDown)operator).setDropDisjuncts(false);
					}

				} else {
					Set<OWLClassExpression> superClasses =
							reasoner.getClassHierarchy().getSuperClasses(classToDescribe, true);

					if (superClasses.size() > 1) {
						startClass = dataFactory.getOWLObjectIntersectionOf(superClasses);

					} else if (superClasses.size() == 1){
						startClass = (OWLClassExpression) superClasses.toArray()[0];

					} else {
						startClass = dataFactory.getOWLThing();
						logger.warn(classToDescribe + " is equivalent to " +
								"owl:Thing. Usually, it is not sensible to " +
								"learn a class expression in this case.");
					}
				}
			}
		}
		return startClass;
	}

	public static String getName() {
		return "Distributed CELOE AMPQ";
	}

	/**
	 * Expands the best node of those, which have not achieved 100% accuracy
	 * already and have a horizontal expansion equal to their length
	 * (rationale: further extension is likely to add irrelevant syntactical
	 * constructs)
	 */
	private OENode getNextNodeToExpand() {
		Iterator<OENode> it = searchTree.descendingIterator();

		while(it.hasNext()) {
			OENode node = it.next();

			if (searchTree.isBlocked(node) || searchTree.isDisabled(node)) continue;

			boolean horizExpLtDescLen = node.getHorizontalExpansion() <
					OWLClassExpressionUtils.getLength(node.getDescription());

			if (isExpandAccuracy100Nodes() && horizExpLtDescLen) {
					return node;

			} else {
				if(node.getAccuracy() < 1.0 || horizExpLtDescLen) {
					return node;
				}
			}
		}

		// this might happen if there are currently no unblocked nodes
		return null;
	}

	/** checks whether the class expression is allowed */
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

			// we now have to perform sanity checks: if \forall is used, then there
			// should be at least on class instance which has a filler at the given context
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

				// if we do not find a filler, this means that putting \forall at
				// that position is not meaningful
				if(!fillerFound) {
					return false;
				}
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
		return !cls.isOWLThing() &&
				(description instanceof OWLNaryBooleanClassExpression &&
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
					"tested). " + searchTree.size() + " nodes in the search " +
					"tree.\n");

		} else {
			totalRuntimeNs = System.nanoTime()-nanoStartTime;
			logger.info("Algorithm terminated successfully (time: " +
					Helper.prettyPrintNanoSeconds(totalRuntimeNs) + ", " +
					expressionTests+" descriptions tested, " +
					searchTree.size() + " nodes in the search tree).\n");
			logger.info(reasoner.toString());
		}
	}

	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
		searchTree = new SearchTree(heuristic);
		descriptions = new TreeSet<>();
		bestEvaluatedDescriptions.getSet().clear();
		expressionTests = 0;

		currentHighestAccuracy = 0;

		// copied from former AMPQ version
		bestAccuracy = Double.MIN_VALUE;
		bestDescription = null;
		expressionTestCountLastImprovement = 0;
		minHorizExp = 0;
		maxHorizExp = 0;
		timeLastImprovement = 0;
		totalRuntimeNs = 0;
	}

	private void showIfBetterSolutionsFound() {
		if(!singleSuggestionMode &&
				bestEvaluatedDescriptions.getBestAccuracy() > currentHighestAccuracy) {

			currentHighestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();
			expressionTestCountLastImprovement = expressionTests;
			timeLastImprovement = System.nanoTime();
			long durationInMillis = getCurrentRuntimeInMilliSeconds();
			String durationStr = getDurationAsString(durationInMillis);

			logger.info("more accurate (" +
					dfPercent.format(currentHighestAccuracy) + ") class " +
					"expression found after " + durationStr + ": " +
					descriptionToString(bestEvaluatedDescriptions.getBest().getDescription()));
		}
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
		}
	}

	private void writeSearchTree(TreeSet<OWLClassExpression> refinements) {
		StringBuilder treeString =
				new StringBuilder("best node: ").append(
						bestEvaluatedDescriptions.getBest()).append("\n");

		if (refinements.size() > 1) {
			treeString.append("all expanded nodes:\n");

			for (OWLClassExpression ref : refinements) {
				treeString.append("   ").append(ref).append("\n");
			}
		}
		treeString.append(TreeUtils.toTreeString(searchTree, baseURI, prefixes)).append("\n");

		// replace or append
		if (replaceSearchTree) {
			Files.createFile(new File(searchTreeFile), treeString.toString());
		} else {
			Files.appendToFile(new File(searchTreeFile), treeString.toString());
		}
	}

	// </----------------------- non-interface methods ----------------------->


	// <---------------------------- AMQP-specific --------------------------->

	// </--------------------------- AMQP-specific --------------------------->


	// <---------------------------- getter/setter --------------------------->

	// bestEvaluatedDescriptions
	public double getCurrentlyBestAccuracy() {
		return bestEvaluatedDescriptions.getBest().getAccuracy();
	}

	// expandAccuracy100Nodes
	public boolean isExpandAccuracy100Nodes() {
		return expandAccuracy100Nodes;
	}

	public void setExpandAccuracy100Nodes(boolean expandAccuracy100Nodes) {
		this.expandAccuracy100Nodes = expandAccuracy100Nodes;
	}

	// expressionTests
	public int getClassExpressionTests() {
		return expressionTests;
	}

	// filterDescriptionsFollowingFromKB
	public boolean isFilterDescriptionsFollowingFromKB() {
		return filterDescriptionsFollowingFromKB;
	}

	public void setFilterDescriptionsFollowingFromKB(boolean filterDescriptionsFollowingFromKB) {
		this.filterDescriptionsFollowingFromKB = filterDescriptionsFollowingFromKB;
	}

	// heuristic
	public OEHeuristicRuntime getHeuristic() {
		return heuristic;
	}

	@Autowired(required=false)
	public void setHeuristic(OEHeuristicRuntime heuristic) {
		this.heuristic = heuristic;
	}

	// isRunning
	@Override
	public boolean isRunning() {
		return isRunning;
	}

	// maxClassExpressionTests
	public int getMaxClassExpressionTests() {
		return maxClassExpressionTests;
	}

	public void setMaxClassExpressionTests(int maxClassExpressionTests) {
		this.maxClassExpressionTests = maxClassExpressionTests;
	}

	// maxClassExpressionTestsAfterImprovement
	public int getMaxClassExpressionTestsAfterImprovement() {
		return maxClassExpressionTestsAfterImprovement;
	}

	public void setMaxClassExpressionTestsAfterImprovement(
			int maxClassExpressionTestsAfterImprovement) {

		this.maxClassExpressionTestsAfterImprovement =
				maxClassExpressionTestsAfterImprovement;
	}

	// maxDepth
	public double getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(double maxDepth) {
		this.maxDepth = maxDepth;
	}

	// maxExecutionTimeInSecondsAfterImprovement
	public int getMaxExecutionTimeInSecondsAfterImprovement() {
		return maxExecutionTimeInSecondsAfterImprovement;
	}

	public void setMaxExecutionTimeInSecondsAfterImprovement(
			int maxExecutionTimeInSecondsAfterImprovement) {

		this.maxExecutionTimeInSecondsAfterImprovement =
				maxExecutionTimeInSecondsAfterImprovement;
	}

	// maxHorizExp
	public int getMaximumHorizontalExpansion() {
		return maxHorizExp;
	}

	// maxNrOfResults
	public int getMaxNrOfResults() {
		return maxNrOfResults;
	}

	public void setMaxNrOfResults(int maxNrOfResults) {
		this.maxNrOfResults = maxNrOfResults;
	}

	// minHorizExp
	public int getMinimumHorizontalExpansion() {
		return minHorizExp;
	}

	// noisePercentage
	public double getNoisePercentage() {
		return noisePercentage;
	}

	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}

	// operator
	public LengthLimitedRefinementOperator getOperator() {
		return operator;
	}

	@Autowired(required=false)
	public void setOperator(LengthLimitedRefinementOperator operator) {
		this.operator = operator;
	}

	// replaceSearchTree
	public boolean isReplaceSearchTree() {
		return replaceSearchTree;
	}

	public void setReplaceSearchTree(boolean replaceSearchTree) {
		this.replaceSearchTree = replaceSearchTree;
	}

	// reuseExistingDescription
	public boolean isReuseExistingDescription() {
		return reuseExistingDescription;
	}

	public void setReuseExistingDescription(boolean reuseExistingDescription) {
		this.reuseExistingDescription = reuseExistingDescription;
	}

	// runMaster
	public void setRunMaster() {
		runMaster = true;
	}

	public boolean runMaster() {
		return runMaster;
	}

	// searchTreeFile
	public String getSearchTreeFile() {
		return searchTreeFile;
	}

	public void setSearchTreeFile(String searchTreeFile) {
		this.searchTreeFile = searchTreeFile;
	}

	// singleSuggestionMode
	public boolean isSingleSuggestionMode() {
		return singleSuggestionMode;
	}

	public void setSingleSuggestionMode(boolean singleSuggestionMode) {
		this.singleSuggestionMode = singleSuggestionMode;
	}

	// startClass
	public OWLClassExpression getStartClass() {
		return startClass;
	}

	public void setStartClass(OWLClassExpression startClass) {
		this.startClass = startClass;
	}

	// stopOnFirstDefinition
	public boolean isStopOnFirstDefinition() {
		return stopOnFirstDefinition;
	}

	public void setStopOnFirstDefinition(boolean stopOnFirstDefinition) {
		this.stopOnFirstDefinition = stopOnFirstDefinition;
	}

	// terminateOnNoiseReached
	public boolean isTerminateOnNoiseReached() {
		return terminateOnNoiseReached;
	}

	public void setTerminateOnNoiseReached(boolean terminateOnNoiseReached) {
		this.terminateOnNoiseReached = terminateOnNoiseReached;
	}

	// totalRuntimeNs
	public long getTotalRuntimeNs() {
		return totalRuntimeNs;
	}

	// writeSearchTree
	public boolean isWriteSearchTree() {
		return writeSearchTree;
	}

	public void setWriteSearchTree(boolean writeSearchTree) {
		this.writeSearchTree = writeSearchTree;
	}

	// </--------------------------- getter/setter --------------------------->

	class Master extends MasterThread {

		@Override
		public void init() throws JMSException, URLSyntaxException, QpidException {
			super.init();
			setMessageListener(new MasterMessageListener());
		}

		@Override
		public void run() {
			try {
				startMaster();
			} catch (JMSException e) {
				e.printStackTrace();
			}

			try {
				terminateWorkers();
			} catch (JMSException e1) {
				e1.printStackTrace();
			}
			System.exit(1);
		}

		@Override
		protected void startMaster() throws JMSException {
			nanoStartTime = System.nanoTime();
			currentHighestAccuracy = 0.0;
			pendingRequests = 0;
			OENode nextNode;

			logger.info("start class:" + startClass);

			// <addNode(...) replacement for start node>

			/* some notes:
			 * - no redundancy check needed for start concept
			 * - no check whether concept is allowed needed for start concept
			 * - no check whether concept is too weak needed (we don't have an
			 *   alternative anyway)
			 * - there is no need to check whether start node is a
			 *   candidate -- there are no alternatives
			 * - there is no need to check whether a shorter description exists
			 */

			Monitor mon = MonitorFactory.start("lp");
			double accuracy = learningProblem.getAccuracyOrTooWeak(startClass, noise);
			mon.stop();

			// issue a warning if accuracy is not between 0 and 1 or -1 (too weak)
			if(accuracy > 1.0 || (accuracy < 0.0 && accuracy != -1)) {
				throw new RuntimeException("Invalid accuracy value " +
						accuracy + " for class expression " + startClass +
						". This could be caused by a bug in the heuristic " +
						"measure and should be reported to the DL-Learner " +
						"bug tracker.");
			}

			expressionTests++;  // should be 1 now

			OENode node = new OENode(startClass, accuracy);
			searchTree.setRoot(node);

			bestAccuracy = accuracy;
			bestDescription = startClass;
			logger.info("more accurate (" + dfPercent.format(bestAccuracy) +
					") class expression found: " +
					descriptionToString(bestDescription));

			OWLClassExpression niceDescription = rewrite(node.getExpression());
			bestEvaluatedDescriptions.add(niceDescription, accuracy, learningProblem);

			// </addNode(...) replacement for start node>

			while (!terminationCriteriaSatisfied()) {
				if (pendingRequests > maxPendingRequests) continue;

				showIfBetterSolutionsFound();

				// chose best node according to heuristics
				nextNode = getNextNodeToExpand();

				/* this might happen if there are currently no unblocked nodes
				 * in the search tree */
				if (nextNode == null) continue;

				logger.info("NEXT NODE ACC: " + nextNode.getAccuracy());

				// With a lot of workers it might happen that temporarilyTOP is
				// going to be the best node. Then the whole global search tree
				// would be sent to the AMQP broker which e.g. took a lot of
				// time and will obviously result in a much too big message to
				// be handles by the broker. So this last second check was added
				// which is obviously causing a lot of meaningless work but
				// should remedy this behavior for now.
				// (During carcinogenesis experiments the whole tree had a size
				// of 1,461,367 nodes after ~5 minutes.)
				SearchTree subTree =
						searchTree.cutSubtreeCopyIfNotBiggerThan(nextNode, 10000);

				if (subTree == null) {
					searchTree.setDisabled(nextNode);
					logger.info("sub-tree of " + nextNode + " too big. Skipping....");
					continue;
				}

				sendTree(subTree, minHorizExp, maxHorizExp);
			}

			if(singleSuggestionMode) {
				bestEvaluatedDescriptions.add(bestDescription, bestAccuracy, learningProblem);
			}

			terminateWorkers();

			// print some stats
			printAlgorithmRunStats();

			// print solution(s)
			logger.info("solutions:\n" + getSolutionString());

			isRunning = false;
		}


		public void processRecvdTree(SearchTreeContainer treeContainer) {
			receivedMessagesCount++;
			pendingRequests--;
			SearchTree remoteTree = treeContainer.getTree();

			searchTree.updateAndSetUnblocked(remoteTree);

			double remoteBestAcc = treeContainer.getBestAccuracy();
			OWLClassExpression remoteBestDescription = treeContainer.getBestDescription();
			int remoteExprTests = treeContainer.getNumChecks();

			expressionTests += remoteExprTests;

			if(remoteBestAcc > bestAccuracy) {
				bestAccuracy = remoteBestAcc;
				bestDescription = remoteBestDescription;
				logger.info("more accurate (" +
						dfPercent.format(bestAccuracy) + ") class expression " +
						"found: " + descriptionToString(bestDescription));
			}

			EvaluatedDescriptionSet remoteBestEvaluatedDescriptions =
					treeContainer.getBestEvaluatedDescriptions();

			for (EvaluatedDescription<? extends Score> ed : remoteBestEvaluatedDescriptions.getSet()) {
				bestEvaluatedDescriptions.add(ed);
			}

			int newMinHorizExp = treeContainer.getMinHorizExpansion();
			int newMaxHorizExp = treeContainer.getMaxHorizExpansion();

			maxHorizExp = Math.max(maxHorizExp, newMaxHorizExp);
			minHorizExp = Math.min(minHorizExp, newMinHorizExp);
		}

		class MasterMessageListener implements MessageListener {

			@Override
			public void onMessage(Message msg) {
				SearchTreeContainer treeContainer;

				try {
					treeContainer = (SearchTreeContainer) ((ObjectMessage) msg).getObject();
					processRecvdTree(treeContainer);

				} catch (JMSException e) {
					e.printStackTrace();
				}

			}
		}
	}

	class Worker extends WorkerThread {
		@Override
		public void init() throws URLSyntaxException, QpidException, JMSException {
			super.init();
			setMessageListener(new WorkerMessageListener());
		}

		public void processRecvdTree(SearchTreeContainer treeContainer) {
			reset();

			receivedMessagesCount++;
			// call refineNode
			// add min/maxHorizExp to tree container

			searchTree = treeContainer.getTree();
			startClass = searchTree.getRoot().getDescription();
			minHorizExp = treeContainer.getMinHorizExpansion();
			maxHorizExp = treeContainer.getMaxHorizExpansion();

			if (operator instanceof RhoDRDown) {
				operator = new RhoDRDown((RhoDRDown) operator);
				((RhoDRDown) operator).setStartClass(startClass);
				try {
					operator.init();
				} catch (ComponentInitException e) {
					e.printStackTrace();
				}
			}

			maxExecutionTimeInSeconds = Math.max(
					getRuntimeBase(),
					(int) (getRuntimeBase() * Math.log(searchTree.size())));

			startWorkerCELOE();

			sendResults(searchTree, bestAccuracy, bestDescription,
					bestEvaluatedDescriptions, expressionTests, minHorizExp,
					maxHorizExp);
		}

		/**
		 * copy from current CELOE implementation
		 */
		public void startWorkerCELOE() {
			OENode nextNode;

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
					if(length > horizExp && OWLClassExpressionUtils.getDepth(refinement) <= maxDepth) {
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
		}

		/** expand node horizontically */
		private TreeSet<OWLClassExpression> refineNode(OENode node) {
			MonitorFactory.getTimeMonitor("refineNode").start();
			/* we have to remove and add the node since its heuristic
			 * evaluation changes through the expansion (you *must not*
			 * include any criteria in the heuristic which are modified
			 * outside of this method, otherwise you may see rarely occurring
			 * but critical false ordering in the nodes set)
			 */
			searchTree.updatePrepare(node);
			int horizExp = node.getHorizontalExpansion();
			TreeSet<OWLClassExpression> refinements =
					(TreeSet<OWLClassExpression>) operator.refine(
							node.getDescription(), horizExp+1);

			node.incHorizontalExpansion();
			node.setRefinementCount(refinements.size());

			searchTree.updateDone(node);
			MonitorFactory.getTimeMonitor("refineNode").stop();

			return refinements;
		}

		class WorkerMessageListener implements MessageListener {

			@Override
			public void onMessage(Message msg) {

				if (stop) {
					Thread.currentThread().interrupt();
					return;
				}

				SearchTreeContainer treeContainer;

				try {
					treeContainer = (SearchTreeContainer) ((ObjectMessage) msg).getObject();
					processRecvdTree(treeContainer);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
