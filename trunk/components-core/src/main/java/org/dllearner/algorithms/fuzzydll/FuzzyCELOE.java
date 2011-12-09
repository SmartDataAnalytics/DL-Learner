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

package org.dllearner.algorithms.fuzzydll;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.FuzzyClassExpressionLearningAlgorithm;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.DoubleConfigOption;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Restriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.FuzzyPosNegLP;
import org.dllearner.learningproblems.FuzzyPosNegLPStandard;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.DescriptionMinimizer;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.dllearner.utilities.owl.PropertyContext;
import org.springframework.beans.factory.annotation.Autowired;

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
@ComponentAnn(name="Fuzzy CELOE", shortName="fceloe", version=0.2, description="See Fuzzy DL-Learner paper published at ISDA 2011.")
public class FuzzyCELOE extends AbstractCELA implements FuzzyClassExpressionLearningAlgorithm {

	private static Logger logger = Logger.getLogger(FuzzyCELOE.class);
//	private FuzzyCELOEConfigurator configurator;
	
	private boolean isRunning = false;
	private boolean stop = false;	
	
//	private OEHeuristicStable heuristicStable = new OEHeuristicStable();
//	private OEHeuristicRuntime heuristicRuntime = new OEHeuristicRuntime();
	
	private RefinementOperator operator;
	private DescriptionMinimizer minimizer;
	
	// all nodes in the search tree (used for selecting most promising node)
	private TreeSet<FuzzyOENode> nodes;
	private FuzzyOEHeuristicRuntime heuristic; // = new OEHeuristicRuntime();
	// root of search tree
	private FuzzyOENode startNode;
	// the class with which we start the refinement process
	private Description startClass;
	
	// all descriptions in the search tree plus those which were too weak (for fast redundancy check)
	private TreeSet<Description> descriptions;
	
	private EvaluatedDescriptionSet bestEvaluatedDescriptions;
	
	// if true, then each solution is evaluated exactly instead of approximately
	// private boolean exactBestDescriptionEvaluation = false;
	private boolean singleSuggestionMode;
	private Description bestDescription;
	private double bestAccuracy = Double.MIN_VALUE;
	
	private NamedClass classToDescribe;
	// examples are either 1.) instances of the class to describe 2.) positive examples
	// 3.) union of pos.+neg. examples depending on the learning problem at hand
	private Set<Individual> examples;
	
	// CELOE was originally created for learning classes in ontologies, but also
	// works for other learning problem types
	private boolean isClassLearningProblem;
	private boolean isEquivalenceProblem;
	
	private long nanoStartTime;
	
	// important parameters
	private double noise;
	private boolean filterFollowsFromKB;	
	
	// less important parameters
	// forces that one solution cannot be subexpression of another expression; this option is useful to get diversity
	// but it can also suppress quite useful expressions
	private boolean forceMutualDifference = false;
	
	// utility variables
	private String baseURI;
	private Map<String, String> prefixes;
	private DecimalFormat dfPercent = new DecimalFormat("0.00%");
	private ConceptComparator descriptionComparator = new ConceptComparator();
	
	// statistical variables
	private int expressionTests = 0;
	private int minHorizExp = 0;
	private int maxHorizExp = 0;
	
	// TODO: remove this variable, just for testing purposes
//	private int counter = 0;
//	private PrintWriter out;
//	private long start = 0;
	
	// TODO: turn those into config options
	
	// important: do not initialise those with empty sets
	// null = no settings for allowance / ignorance
	// empty set = allow / ignore nothing (it is often not desired to allow no class!)
	Set<NamedClass> allowedConcepts = null;
	Set<NamedClass> ignoredConcepts = null;

	private boolean writeSearchTree = false;

	private String searchTreeFile = "log/searchTree.txt";

	private int maxNrOfResults = 10;

	private double noisePercentage = 0.0;

	private boolean filterDescriptionsFollowingFromKB = false;

	private boolean reuseExistingDescription = false;

	private boolean replaceSearchTree = false;

	private int maxClassDescriptionTests = 0;

	private int maxExecutionTimeInSeconds = 100;

	private boolean terminateOnNoiseReached = false;
	
	private double maxDepth = 7;	
	
	public FuzzyCELOE() {
		
	}
	
//	public FuzzyCELOEConfigurator getConfigurator() {
//		return configurator;
//	}
	
	public FuzzyCELOE(AbstractLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
//		configurator = new FuzzyCELOEConfigurator(this);
	}

	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractLearningProblem>> problems = new LinkedList<Class<? extends AbstractLearningProblem>>();
		problems.add(AbstractLearningProblem.class);
		return problems;
	}	
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(CommonConfigOptions.useAllConstructor());
		options.add(CommonConfigOptions.useExistsConstructor());
		options.add(CommonConfigOptions.useHasValueConstructor());
		options.add(CommonConfigOptions.useDataHasValueConstructor());
		options.add(CommonConfigOptions.valueFreqencyThreshold());
		options.add(CommonConfigOptions.useCardinalityRestrictions());
		options.add(CommonConfigOptions.cardinalityLimit());
		// by default, we do not use negation (should be configurable in GUI)
		options.add(CommonConfigOptions.useNegation(false));
		options.add(CommonConfigOptions.useBooleanDatatypes());
		options.add(CommonConfigOptions.useDoubleDatatypes());
		options.add(CommonConfigOptions.maxExecutionTimeInSeconds(10));
		options.add(CommonConfigOptions.getNoisePercentage());
		options.add(CommonConfigOptions.getTerminateOnNoiseReached(false));
		options.add(CommonConfigOptions.getMaxDepth(7));
		options.add(CommonConfigOptions.maxNrOfResults(10));
		options.add(CommonConfigOptions.maxClassDescriptionTests());
		options.add(new BooleanConfigOption("singleSuggestionMode", "Use this if you are interested in only one suggestion and your learning problem has many (more than 1000) examples.", false));
		options.add(CommonConfigOptions.getInstanceBasedDisjoints());
		options.add(new BooleanConfigOption("filterDescriptionsFollowingFromKB", "If true, then the results will not contain suggestions, which already follow logically from the knowledge base. Be careful, since this requires a potentially expensive consistency check for candidate solutions.", false));
		options.add(new BooleanConfigOption("reuseExistingDescription", "If true, the algorithm tries to find a good starting point close to an existing definition/super class of the given class in the knowledge base.", false));
		options.add(new BooleanConfigOption("writeSearchTree", "specifies whether to write a search tree", false));
		options.add(new StringConfigOption("searchTreeFile","file to use for the search tree", "log/searchTree.txt"));
		options.add(new BooleanConfigOption("replaceSearchTree","specifies whether to replace the search tree in the log file after each run or append the new search tree", false));
		options.add(new DoubleConfigOption("expansionPenaltyFactor","heuristic penalty per syntactic construct used (lower = finds more complex expression, but might miss simple ones)", 0.1));	
		return options;
	}
	
	public static String getName() {
		return "fuzzy CELOE";
	}
	
	@Override
	public void init() throws ComponentInitException {
		
		// compute used concepts/roles from allowed/ignored
		// concepts/roles
		Set<NamedClass> usedConcepts;
//		Set<NamedClass> allowedConcepts = configurator.getAllowedConcepts()==null ? null : CommonConfigMappings.getAtomicConceptSet(configurator.getAllowedConcepts());
//		Set<NamedClass> ignoredConcepts = configurator.getIgnoredConcepts()==null ? null : CommonConfigMappings.getAtomicConceptSet(configurator.getIgnoredConcepts());
		if(allowedConcepts != null) {
			// sanity check to control if no non-existing concepts are in the list
			Helper.checkConcepts(reasoner, allowedConcepts);
			usedConcepts = allowedConcepts;
		} else if(ignoredConcepts != null) {
			usedConcepts = Helper.computeConceptsUsingIgnoreList(reasoner, ignoredConcepts);
		} else {
			usedConcepts = Helper.computeConcepts(reasoner);
		}
		
		// copy class hierarchy and modify it such that each class is only
		// reachable via a single path
//		ClassHierarchy classHierarchy = reasoner.getClassHierarchy().clone();
		ClassHierarchy classHierarchy = reasoner.getClassHierarchy().cloneAndRestrict(usedConcepts);
		classHierarchy.thinOutSubsumptionHierarchy();

		// if no one injected a heuristic, we use a default one
		if(heuristic == null) {
			heuristic = new FuzzyOEHeuristicRuntime();
		}
		
		minimizer = new DescriptionMinimizer(reasoner);
		
		startClass = Thing.instance;
		
//		singleSuggestionMode = configurator.getSingleSuggestionMode();
		
		// create refinement operator
		if(operator == null) {
			operator = new RhoDRDown();
			((RhoDRDown)operator).setStartClass(startClass);
			((RhoDRDown)operator).setSubHierarchy(classHierarchy);
			((RhoDRDown)operator).setReasoner(reasoner);
			((RhoDRDown)operator).init();
		}
//		operator = new RhoDRDown(reasoner, classHierarchy, startClass, configurator);
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();		
		if(writeSearchTree) {
			File f = new File(searchTreeFile );
			Files.clearFile(f);
		}
		
		bestEvaluatedDescriptions = new EvaluatedDescriptionSet(maxNrOfResults);
		
		isClassLearningProblem = (learningProblem instanceof ClassLearningProblem);
		
		// we put important parameters in class variables
		noise = noisePercentage/100d;
//		System.out.println("noise " + noise);
//		maxDepth = configurator.getMaxDepth();
		// (filterFollowsFromKB is automatically set to false if the problem
		// is not a class learning problem
		filterFollowsFromKB = filterDescriptionsFollowingFromKB && isClassLearningProblem;
		
//		Set<Description> concepts = operator.refine(Thing.instance, 5);
//		for(Description concept : concepts) {
//			System.out.println(concept);
//		}
//		System.out.println("refinements of thing: " + concepts.size());
		
		// actions specific to ontology engineering
		if(isClassLearningProblem) {
			ClassLearningProblem problem = (ClassLearningProblem) learningProblem;
			classToDescribe = problem.getClassToDescribe();
			isEquivalenceProblem = problem.isEquivalenceProblem();
			
			examples = reasoner.getIndividuals(classToDescribe);
			
			// start class: intersection of super classes for definitions (since it needs to
			// capture all instances), but owl:Thing for learning subclasses (since it is
			// superfluous to add super classes in this case)
			if(isEquivalenceProblem) {
				Set<Description> existingDefinitions = reasoner.getAssertedDefinitions(classToDescribe);
				if(reuseExistingDescription && (existingDefinitions.size() > 0)) {
					// the existing definition is reused, which in the simplest case means to
					// use it as a start class or, if it is already too specific, generalise it
					
					// pick the longest existing definition as candidate
					Description existingDefinition = null;
					int highestLength = 0;
					for(Description exDef : existingDefinitions) {
						if(exDef.getLength() > highestLength) {
							existingDefinition = exDef;
							highestLength = exDef.getLength();
						}
					}
					
					LinkedList<Description> startClassCandidates = new LinkedList<Description>();
					startClassCandidates.add(existingDefinition);
					((RhoDRDown)operator).setDropDisjuncts(true);
					RefinementOperator upwardOperator = new OperatorInverter(operator);
					
					// use upward refinement until we find an appropriate start class
					boolean startClassFound = false;
					Description candidate;
					do {
						candidate = startClassCandidates.pollFirst();
						if(((ClassLearningProblem)learningProblem).getRecall(candidate)<1.0) {
							// add upward refinements to list
							Set<Description> refinements = upwardOperator.refine(candidate, candidate.getLength());
//							System.out.println("ref: " + refinements);
							LinkedList<Description> refinementList = new LinkedList<Description>(refinements);
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
						logger.info("Reusing existing description " + startClass.toManchesterSyntaxString(baseURI, prefixes) + " as start class for learning algorithm.");
					} else {
						logger.info("Generalised existing description " + existingDefinition.toManchesterSyntaxString(baseURI, prefixes) + " to " + startClass.toManchesterSyntaxString(baseURI, prefixes) + ", which is used as start class for the learning algorithm.");
					}
					
//					System.out.println("start class: " + startClass);
//					System.out.println("existing def: " + existingDefinition);
//					System.out.println(reasoner.getIndividuals(existingDefinition));
					
					((RhoDRDown)operator).setDropDisjuncts(false);
					
				} else {
					Set<Description> superClasses = reasoner.getClassHierarchy().getSuperClasses(classToDescribe);
					if(superClasses.size() > 1) {
						startClass = new Intersection(new LinkedList<Description>(superClasses));
					} else if(superClasses.size() == 1){
						startClass = (Description) superClasses.toArray()[0];
					} else {
						startClass = Thing.instance;
						logger.warn(classToDescribe + " is equivalent to owl:Thing. Usually, it is not " +
								"sensible to learn a description in this case.");
					}					
				}
			}					
		} else if(learningProblem instanceof PosOnlyLP) {
			examples = ((PosOnlyLP)learningProblem).getPositiveExamples();
		// changed by Josue
		} else if (learningProblem instanceof PosNegLP) {
			examples = Helper.union(((PosNegLP)learningProblem).getPositiveExamples(),((PosNegLP)learningProblem).getNegativeExamples());
		} 
		// commented by Josue as now there's no need of + and - examples (more code need to be deleted in this sense)
		// else if (learningProblem instanceof FuzzyPosNegLP) {
			//examples = Helper.union(((FuzzyPosNegLP)learningProblem).getPositiveExamples(),((FuzzyPosNegLP)learningProblem).getNegativeExamples());
		// }
	}

	@Override
	public Description getCurrentlyBestDescription() {
		EvaluatedDescription ed = getCurrentlyBestEvaluatedDescription();
		return ed == null ? null : ed.getDescription();
	}

	@Override
	public List<Description> getCurrentlyBestDescriptions() {
		return bestEvaluatedDescriptions.toDescriptionList();
	}
	
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return bestEvaluatedDescriptions.getBest();
	}	
	
	@Override
	public TreeSet<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions() {
		return bestEvaluatedDescriptions.getSet();
	}	
	
	public double getCurrentlyBestAccuracy() {
		return bestEvaluatedDescriptions.getBest().getAccuracy();
	}
	
	@Override
	public void start() {
//		System.out.println(configurator.getMaxExecutionTimeInSeconds());
		
		stop = false;
		isRunning = true;
		reset();
		nanoStartTime = System.nanoTime();
		
		// highest accuracy so far
		double highestAccuracy = 0.0;
		FuzzyOENode nextNode;

		addNode(startClass, null);
		
		int loop = 0;
				
		while (!terminationCriteriaSatisfied()) {
			
			if(!singleSuggestionMode && bestEvaluatedDescriptions.getBestAccuracy() > highestAccuracy) {
				highestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();
				logger.info("more accurate (" + dfPercent.format(highestAccuracy) + ") class expression found: " + descriptionToString(bestEvaluatedDescriptions.getBest().getDescription()));	
			}

			// chose best node according to heuristics
			nextNode = getNextNodeToExpand();
			int horizExp = nextNode.getHorizontalExpansion();
						
			// apply operator
			Monitor mon = MonitorFactory.start("refineNode");
			TreeSet<Description> refinements = refineNode(nextNode);
			mon.stop();
			
			// TODO: just for testing purposes
//			counter++;
//			out.println(counter + " next node: " + nextNode);
//			for(Description refinement : refinements) {
//				out.println("refinement: " + refinement);
//			}
//			out.println();
			
			while(refinements.size() != 0) {
				// pick element from set
				Description refinement = refinements.pollFirst();
				int length = refinement.getLength();
				
				// we ignore all refinements with lower length and too high depth
				// (this also avoids duplicate node children)
				if(length > horizExp && refinement.getDepth() <= maxDepth) {
					
//					System.out.println("potentially adding " + refinement + " to search tree as child of " + nextNode + " " + new Date());
					Monitor mon2 = MonitorFactory.start("addNode");
					
					// TODO: remove
//					start = System.currentTimeMillis();
					
					addNode(refinement, nextNode);
					
					// TODO: remove, just testing purposes
//					counter++;
//					System.out.println(counter + " * " + (System.currentTimeMillis() - start));
					
					mon2.stop();
					// adding nodes is potentially computationally expensive, so we have
					// to check whether max time is exceeded	
					if(terminationCriteriaSatisfied()) {
						break;
					}
//					System.out.println("addNode finished" + " " + new Date());
				}
		
//				System.out.println("  refinement queue length: " + refinements.size());
			}
			
			updateMinMaxHorizExp(nextNode);
			
			// writing the search tree (if configured)
			if (writeSearchTree) {
				String treeString = "best node: " + bestEvaluatedDescriptions.getBest() + "\n";
				if (refinements.size() > 1) {
					treeString += "all expanded nodes:\n";
					for (Description n : refinements) {
						treeString += "   " + n + "\n";
					}
				}
				treeString += startNode.toTreeString(baseURI);
				treeString += "\n";

				if (replaceSearchTree)
					Files.createFile(new File(searchTreeFile), treeString);
				else
					Files.appendToFile(new File(searchTreeFile), treeString);
			}
			
//			System.out.println(loop);
			loop++;
		}
		
		if (stop) {
			logger.info("Algorithm stopped ("+expressionTests+" descriptions tested). " + nodes.size() + " nodes in the search tree.\n");
		} else {
			logger.info("Algorithm terminated successfully ("+expressionTests+" descriptions tested). "  + nodes.size() + " nodes in the search tree.\n");
            logger.info(reasoner.toString());
		}

		if(singleSuggestionMode) {
			bestEvaluatedDescriptions.add(bestDescription, bestAccuracy, learningProblem);
		}		
		
		// print solution(s)
		logger.info("solutions:\n" + getSolutionString());
		
//		System.out.println(startNode.toTreeString(baseURI));
		
		isRunning = false;
//		System.out.println("isRunning: " + isRunning);
	}

	private FuzzyOENode getNextNodeToExpand() {
		// TODO: remove, just for testing purposes
//		Iterator<FuzzyOENode> itx = nodes.descendingIterator();
//		while(itx.hasNext()) {
//			FuzzyOENode node = itx.next();
//			out.println(node.getAccuracy() + " " + node.getHorizontalExpansion() + " " + node.getDescription().getLength() + " " + node);
//		}
		// we expand the best node of those, which have not achieved 100% accuracy
		// already and have a horizontal expansion equal to their length
		// (rationale: further extension is likely to add irrelevant syntactical constructs)
		Iterator<FuzzyOENode> it = nodes.descendingIterator();
		while(it.hasNext()) {
			FuzzyOENode node = it.next();
			// TODO: in a fuzzy environment ... has sense to return nodes with accuracy < 1 ???
			if(node.getAccuracy() < 1.0 || node.getHorizontalExpansion() < node.getDescription().getLength()) {
				return node;
			}
		}
		
		// this should practically never be called, since for any reasonable learning
		// task, we will always have at least one node with less than 100% accuracy
		return nodes.last();
	}
	
	// expand node horizontically
	private TreeSet<Description> refineNode(FuzzyOENode node) {
		// we have to remove and add the node since its heuristic evaluation changes through the expansion
		// (you *must not* include any criteria in the heuristic which are modified outside of this method,
		// otherwise you may see rarely occurring but critical false ordering in the nodes set)
		nodes.remove(node);
//		System.out.println("refining: " + node);
		int horizExp = node.getHorizontalExpansion();
		TreeSet<Description> refinements = (TreeSet<Description>) operator.refine(node.getDescription(), horizExp+1);
		node.incHorizontalExpansion();
		node.setRefinementCount(refinements.size());
		nodes.add(node);
		return refinements;
	}
	
	// add node to search tree if it is not too weak
	// returns true if node was added and false otherwise
	private boolean addNode(Description description, FuzzyOENode parentNode) {
		// counter++;
		// System.out.println(counter + " " + description);

		// redundancy check (return if redundant)
		boolean nonRedundant = descriptions.add(description);
		if(!nonRedundant) {
			return false;
		}
		// check whether the description is allowed
		if(!isDescriptionAllowed(description, parentNode)) {
			return false;
		}	
//		System.out.println("Test " + new Date());
		// quality of description (return if too weak)
		double accuracy = learningProblem.getAccuracyOrTooWeak(description, noise);
		// issue a warning if accuracy is not between 0 and 1 or -1 (too weak)
		if(accuracy > 1.0 || (accuracy < 0.0 && accuracy != -1)) {
			logger.warn("Invalid accuracy value " + accuracy + " for description " + description + ". This could be caused by a bug in the heuristic measure and should be reported to the DL-Learner bug tracker.");
			System.exit(0);
		}
		
//		System.out.println("Test2 " + new Date());
		expressionTests++;
//		System.out.println("acc: " + accuracy);
//		System.out.println(description + " " + accuracy);
		if(accuracy == -1) {
			return false;
		}
		
		FuzzyOENode node = new FuzzyOENode(parentNode, description, accuracy);
			
		// link to parent (unless start node)
		if(parentNode == null) {
			startNode = node;
		} else {
			parentNode.addChild(node);
		}
	
		nodes.add(node);
//		System.out.println("Test3 " + new Date());
		
		// in some cases (e.g. mutation) fully evaluating even a single description is too expensive
		// due to the high number of examples -- so we just stick to the approximate accuracy
		if(singleSuggestionMode) {
			if(accuracy > bestAccuracy) {
				bestAccuracy = accuracy;
				bestDescription = description;
				logger.info("more accurate (" + dfPercent.format(bestAccuracy) + ") class expression found: " + descriptionToString(bestDescription)); // + getTemporaryString(bestDescription)); 
			}
			return true;
		} 
		
//		System.out.println("description " + description + " accuracy " + accuracy);
		
		// maybe add to best descriptions (method keeps set size fixed);
		// we need to make sure that this does not get called more often than
		// necessary since rewriting is expensive
		boolean isCandidate = !bestEvaluatedDescriptions.isFull();
		if(!isCandidate) {
			
			// TODO remove, just testing purposes
//			Iterator i = bestEvaluatedDescriptions.getSet().iterator();
//			int j = 0;
//			out.println(counter + " " + description);
//			while (i.hasNext()) {
//				j++;
//				// System.err.println(j + " -> " + i.next());
//				out.println(j + " -> " + i.next());
//			}
//			out.println();
			
			EvaluatedDescription worst = bestEvaluatedDescriptions.getWorst();
			double accThreshold = worst.getAccuracy();
			isCandidate = 
				(accuracy > accThreshold ||
				(accuracy >= accThreshold && description.getLength() < worst.getDescriptionLength()));
		}
		
//		System.out.println(isCandidate);
		
//		System.out.println("Test4 " + new Date());
		if(isCandidate) {
			
			Description niceDescription = rewriteNode(node);
			ConceptTransformation.transformToOrderedForm(niceDescription, descriptionComparator);
//			Description niceDescription = node.getDescription();
			
			// another test: none of the other suggested descriptions should be 
			// a subdescription of this one unless accuracy is different
			// => comment: on the one hand, this appears to be too strict, because once A is a solution then everything containing
			// A is not a candidate; on the other hand this suppresses many meaningless extensions of A
			boolean shorterDescriptionExists = false;
			if(forceMutualDifference) {
				for(EvaluatedDescription ed : bestEvaluatedDescriptions.getSet()) {
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
//					System.out.println("Test2");
					bestEvaluatedDescriptions.add(niceDescription, accuracy, learningProblem);
//					System.out.println("acc: " + accuracy);
//					System.out.println(bestEvaluatedDescriptions);
				}
			}
						
//			System.out.println(bestEvaluatedDescriptions.getSet().size());
		}
		
//		System.out.println("Test5 " + new Date());
//		System.out.println("best evaluated descriptions size: " + bestEvaluatedDescriptions.size() + " worst: " + bestEvaluatedDescriptions.getWorst());
		return true;
	}	
	
	// checks whether the description is allowed
	private boolean isDescriptionAllowed(Description description, FuzzyOENode parentNode) {
		if(isClassLearningProblem) {
			if(isEquivalenceProblem) {
				// the class to learn must not appear on the outermost property level
				if(occursOnFirstLevel(description, classToDescribe)) {
					return false;
				}
			} else {
				// none of the superclasses of the class to learn must appear on the
				// outermost property level
				TreeSet<Description> toTest = new TreeSet<Description>(descriptionComparator);
				toTest.add(classToDescribe);
				while(!toTest.isEmpty()) {
					Description d = toTest.pollFirst();
					if(occursOnFirstLevel(description, d)) {
						return false;
					}
					toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
				}
			}			
		}
		
		// perform forall sanity tests
		if(parentNode != null && ConceptTransformation.getForallOccurences(description) > ConceptTransformation.getForallOccurences(parentNode.getDescription())) {
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
				Description existentialContext = context.toExistentialContext();
				boolean fillerFound = false;
				for(Individual instance : examples) {
					if(reasoner.hasType(existentialContext, instance)) {
//						System.out.println(instance + "  " + existentialContext);
						fillerFound = true;
						break;
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
//		Set<Description> siblingClasses = reasoner.getClassHierarchy().getSiblingClasses(classToDescribe);
//		for now, we just disable negation
		
		return true;
	}
	
	// determine whether a named class occurs on the outermost level, i.e. property depth 0
	// (it can still be at higher depth, e.g. if intersections are nested in unions)
	private boolean occursOnFirstLevel(Description description, Description clazz) {
		if(description instanceof NamedClass) {
			if(description.equals(clazz)) {
				return true;
			}
		} 
		
		if(description instanceof Restriction) {
			return false;
		}
		
		for(Description child : description.getChildren()) {
			if(occursOnFirstLevel(child, clazz)) {
				return true;
			}
		}
		
		return false;
	}
	
	// check whether the node is a potential solution candidate
	private Description rewriteNode(FuzzyOENode node) {
		Description description = node.getDescription();
		// minimize description (expensive!) - also performes some human friendly rewrites
		Description niceDescription = minimizer.minimizeClone(description);
		// replace \exists r.\top with \exists r.range(r) which is easier to read for humans
		ConceptTransformation.replaceRange(niceDescription, reasoner);
		return niceDescription;
	}
	
	private boolean terminationCriteriaSatisfied() {
		return 
		stop || 
		(maxClassDescriptionTests != 0 && (expressionTests >= maxClassDescriptionTests)) ||
		(maxExecutionTimeInSeconds != 0 && ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSeconds*1000000000l))) ||
		(terminateOnNoiseReached && (100*getCurrentlyBestAccuracy()>=100-noisePercentage));
	}
	
	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
		nodes = new TreeSet<FuzzyOENode>(heuristic);
		descriptions = new TreeSet<Description>(new ConceptComparator());
		bestEvaluatedDescriptions.getSet().clear();
		expressionTests = 0;
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}	
	
	@Override
	public void stop() {
		stop = true;
	}

	public FuzzyOENode getSearchTreeRoot() {
		return startNode;
	}
	
	// central function for printing description
	private String descriptionToString(Description description) {
		return description.toManchesterSyntaxString(baseURI, prefixes);
	}
	
	@SuppressWarnings("unused")
	private String bestDescriptionToString() {
		EvaluatedDescription best = bestEvaluatedDescriptions.getBest();
		return best.getDescription().toManchesterSyntaxString(baseURI, prefixes) + " (accuracy: " + dfPercent.format(best.getAccuracy()) + ")";
	}	
	
	private String getSolutionString() {
		int current = 1;
		String str = "";
		for(EvaluatedDescription ed : bestEvaluatedDescriptions.getSet().descendingSet()) {
			// temporary code
			// changed by Josue
			if(learningProblem instanceof PosNegLPStandard) {
				str += current + ": " + descriptionToString(ed.getDescription()) + " (pred. acc.: " + dfPercent.format(((PosNegLPStandard)learningProblem).getPredAccuracyOrTooWeakExact(ed.getDescription(),1)) + ", F-measure: "+ dfPercent.format(((PosNegLPStandard)learningProblem).getFMeasureOrTooWeakExact(ed.getDescription(),1)) + ")\n";
			} else if (learningProblem instanceof FuzzyPosNegLPStandard) {
				str += current + ": " + descriptionToString(ed.getDescription()) + " (pred. acc.: " + dfPercent.format(((FuzzyPosNegLPStandard)learningProblem).getPredAccuracyOrTooWeakExact(ed.getDescription(),1)) + ", F-measure: "+ dfPercent.format(((FuzzyPosNegLPStandard)learningProblem).getFMeasureOrTooWeakExact(ed.getDescription(),1)) + ")\n";
			} else {
				str += current + ": " + descriptionToString(ed.getDescription()) + " " + dfPercent.format(ed.getAccuracy()) + "\n";
//				System.out.println(ed);
			}
			current++;
		}
		return str;
	}

//	private String getTemporaryString(Description description) {
//		return descriptionToString(description) + " (pred. acc.: " + dfPercent.format(((PosNegLPStandard)learningProblem).getPredAccuracyOrTooWeakExact(description,1)) + ", F-measure: "+ dfPercent.format(((PosNegLPStandard)learningProblem).getFMeasureOrTooWeakExact(description,1)) + ")";
//	}
	
	private void updateMinMaxHorizExp(FuzzyOENode node) {
		int newHorizExp = node.getHorizontalExpansion();
		
		// update maximum value
		maxHorizExp = Math.max(maxHorizExp, newHorizExp);
		
		// we just expanded a node with minimum horizontal expansion;
		// we need to check whether it was the last one
		if(minHorizExp == newHorizExp - 1) {
			
			// the best accuracy that a node can achieve 
			double scoreThreshold = heuristic.getNodeScore(node) + 1 - node.getAccuracy();
			
			for(FuzzyOENode n : nodes.descendingSet()) {
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
	
	public int getMaximumHorizontalExpansion() {
		return maxHorizExp;
	}

	public int getMinimumHorizontalExpansion() {
		return minHorizExp;
	}

	// added by Josue (when implementing FuzzyClassExpressionLearningAlgorithm)

	@Override
	public List<Description> getCurrentlyBestDescriptions(int nrOfDescriptions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(
			int nrOfDescriptions) {
		// TODO Auto-generated method stub
		return null;
	}
	

	/**
	 * @return the expressionTests
	 */
	public int getClassExpressionTests() {
		return expressionTests;
	}

	public RefinementOperator getOperator() {
		return operator;
	}

	@Autowired(required=false)
	public void setOperator(RefinementOperator operator) {
		this.operator = operator;
	}

	public Description getStartClass() {
		return startClass;
	}

	public void setStartClass(Description startClass) {
		this.startClass = startClass;
	}

	public Set<NamedClass> getAllowedConcepts() {
		return allowedConcepts;
	}

	public void setAllowedConcepts(Set<NamedClass> allowedConcepts) {
		this.allowedConcepts = allowedConcepts;
	}

	public Set<NamedClass> getIgnoredConcepts() {
		return ignoredConcepts;
	}

	public void setIgnoredConcepts(Set<NamedClass> ignoredConcepts) {
		this.ignoredConcepts = ignoredConcepts;
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

	public int getMaxClassDescriptionTests() {
		return maxClassDescriptionTests;
	}

	public void setMaxClassDescriptionTests(int maxClassDescriptionTests) {
		this.maxClassDescriptionTests = maxClassDescriptionTests;
	}

	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
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
		
	
}
