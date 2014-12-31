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

package org.dllearner.algorithms.celoe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractHeuristic;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.CustomHierarchyRefinementOperator;
import org.dllearner.refinementoperators.CustomStartRefinementOperator;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.OperatorInverter;
import org.dllearner.refinementoperators.ReasoningBasedRefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.owl.OWLEntityTypeAdder;
import org.dllearner.utilities.owl.PropertyContext;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
@ComponentAnn(name="CELOE", shortName="celoe", version=1.0, description="CELOE is an adapted and extended version of the OCEL algorithm applied for the ontology engineering use case. See http://jens-lehmann.org/files/2011/celoe.pdf for reference.")
public class CELOE extends AbstractCELA implements Cloneable{

	private static Logger logger = Logger.getLogger(CELOE.class);
//	private CELOEConfigurator configurator;
	
	private boolean isRunning = false;
	private boolean stop = false;	
	
//	private OEHeuristicStable heuristicStable = new OEHeuristicStable();
//	private OEHeuristicRuntime heuristicRuntime = new OEHeuristicRuntime();
	
	private LengthLimitedRefinementOperator operator;
	private OWLClassExpressionMinimizer minimizer;
	@ConfigOption(name="useMinimizer", defaultValue="true", description="Specifies whether returned expressions should be minimised by removing those parts, which are not needed. (Basically the minimiser tries to find the shortest expression which is equivalent to the learned expression). Turning this feature off may improve performance.")
	private boolean useMinimizer = true;
	
	// all nodes in the search tree (used for selecting most promising node)
	private TreeSet<OENode> nodes;
	private AbstractHeuristic heuristic; // = new OEHeuristicRuntime();
	// root of search tree
	private OENode startNode;
	// the class with which we start the refinement process
	@ConfigOption(name = "startClass", defaultValue="owl:Thing", description="You can specify a start class for the algorithm. To do this, you have to use Manchester OWL syntax without using prefixes.")
	private OWLClassExpression startClass;
	
	// all descriptions in the search tree plus those which were too weak (for fast redundancy check)
	private TreeSet<OWLClassExpression> descriptions;
	
	
	// if true, then each solution is evaluated exactly instead of approximately
	// private boolean exactBestDescriptionEvaluation = false;
	@ConfigOption(name = "singleSuggestionMode", defaultValue="false", description="Use this if you are interested in only one suggestion and your learning problem has many (more than 1000) examples.")
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
	private int minHorizExp = 0;
	private int maxHorizExp = 0;
	private long totalRuntimeNs = 0;
	
	// TODO: turn those into config options
	

	// important: do not initialise those with empty sets
	// null = no settings for allowance / ignorance
	// empty set = allow / ignore nothing (it is often not desired to allow no class!)
	Set<OWLClass> allowedConcepts = null;
	Set<OWLClass> ignoredConcepts = null;

	@ConfigOption(name = "writeSearchTree", defaultValue="false", description="specifies whether to write a search tree")
	private boolean writeSearchTree = false;

	@ConfigOption(name = "searchTreeFile", defaultValue="log/searchTree.txt", description="file to use for the search tree")
	private String searchTreeFile = "log/searchTree.txt";

	@ConfigOption(name = "replaceSearchTree", defaultValue="false", description="specifies whether to replace the search tree in the log file after each run or append the new search tree")
	private boolean replaceSearchTree = false;
	
	@ConfigOption(name = "maxNrOfResults", defaultValue="10", description="Sets the maximum number of results one is interested in. (Setting this to a lower value may increase performance as the learning algorithm has to store/evaluate/beautify less descriptions).")	
	private int maxNrOfResults = 10;

	@ConfigOption(name = "noisePercentage", defaultValue="0.0", description="the (approximated) percentage of noise within the examples")
	private double noisePercentage = 0.0;

	@ConfigOption(name = "filterDescriptionsFollowingFromKB", defaultValue="false", description="If true, then the results will not contain suggestions, which already follow logically from the knowledge base. Be careful, since this requires a potentially expensive consistency check for candidate solutions.")
	private boolean filterDescriptionsFollowingFromKB = false;

	@ConfigOption(name = "reuseExistingDescription", defaultValue="false", description="If true, the algorithm tries to find a good starting point close to an existing definition/super class of the given class in the knowledge base.")
	private boolean reuseExistingDescription = false;

	@ConfigOption(name = "maxClassExpressionTests", defaultValue="0", description="The maximum number of candidate hypothesis the algorithm is allowed to test (0 = no limit). The algorithm will stop afterwards. (The real number of tests can be slightly higher, because this criterion usually won't be checked after each single test.)")
	private int maxClassExpressionTests = 0;

	@ConfigOption(name = "maxClassExpressionTestsAfterImprovement", defaultValue="0", description = "The maximum number of candidate hypothesis the algorithm is allowed after an improvement in accuracy (0 = no limit). The algorithm will stop afterwards. (The real number of tests can be slightly higher, because this criterion usually won't be checked after each single test.)")
	private int maxClassExpressionTestsAfterImprovement = 0;
	
	@ConfigOption(defaultValue = "10", name = "maxExecutionTimeInSeconds", description = "maximum execution of the algorithm in seconds")
	private int maxExecutionTimeInSeconds = 10;

	@ConfigOption(defaultValue = "0", name = "maxExecutionTimeInSecondsAfterImprovement", description = "maximum execution of the algorithm in seconds")
	private int maxExecutionTimeInSecondsAfterImprovement = 0;
	
	@ConfigOption(name = "terminateOnNoiseReached", defaultValue="false", description="specifies whether to terminate when noise criterion is met")
	private boolean terminateOnNoiseReached = false;
	
	@ConfigOption(name = "maxDepth", defaultValue="7", description="maximum depth of description")
	private double maxDepth = 7;

	@ConfigOption(name = "stopOnFirstDefinition", defaultValue="false", description="algorithm will terminate immediately when a correct definition is found")
	private boolean stopOnFirstDefinition = false;
	
	private int expressionTestCountLastImprovement;
	
	
	@SuppressWarnings("unused")
	private long timeLastImprovement = 0;
	private boolean expandAccuracy100Nodes = false;
	
//	public CELOEConfigurator getConfigurator() {
//		return configurator;
//	}
	
	public CELOE() {
		
	}
	
	public CELOE(CELOE celoe){
		setReasoner(celoe.reasoner);
		setLearningProblem(celoe.learningProblem);
		setAllowedConcepts(celoe.getAllowedConcepts());
		setExpandAccuracy100Nodes(celoe.expandAccuracy100Nodes);
		setFilterDescriptionsFollowingFromKB(celoe.filterDescriptionsFollowingFromKB);
		setHeuristic(celoe.heuristic);
		setIgnoredConcepts(celoe.ignoredConcepts);
		setLearningProblem(celoe.learningProblem);
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
		setReplaceSearchTree(celoe.replaceSearchTree);
		setReuseExistingDescription(celoe.reuseExistingDescription);
		setSingleSuggestionMode(celoe.singleSuggestionMode);
		setStartClass(celoe.startClass);
		setStopOnFirstDefinition(celoe.stopOnFirstDefinition);
		setTerminateOnNoiseReached(celoe.terminateOnNoiseReached);
		setUseMinimizer(celoe.useMinimizer);
		setWriteSearchTree(celoe.writeSearchTree);
	}
	
	public CELOE(AbstractLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(problem, reasoner);
//		configurator = new CELOEConfigurator(this);
	}

	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractLearningProblem>> problems = new LinkedList<Class<? extends AbstractLearningProblem>>();
		problems.add(AbstractLearningProblem.class);
		return problems;
	}
	
	public static String getName() {
		return "CELOE";
	}
	
	@Override
	public void init() throws ComponentInitException {
			
		if(maxExecutionTimeInSeconds != 0 && maxExecutionTimeInSecondsAfterImprovement != 0) {
			maxExecutionTimeInSeconds = Math.min(maxExecutionTimeInSeconds, maxExecutionTimeInSecondsAfterImprovement);
		}
		
		// compute used concepts/roles from allowed/ignored
		// concepts/roles
		Set<OWLClass> usedConcepts;
//		Set<OWLClass> allowedConcepts = configurator.getAllowedConcepts()==null ? null : CommonConfigMappings.getAtomicConceptSet(configurator.getAllowedConcepts());
//		Set<OWLClass> ignoredConcepts = configurator.getIgnoredConcepts()==null ? null : CommonConfigMappings.getAtomicConceptSet(configurator.getIgnoredConcepts());
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
			heuristic = new OEHeuristicRuntime();
		}
		
		minimizer = new OWLClassExpressionMinimizer(df, reasoner);
		
		// start at owl:Thing by default
		if(startClass == null) {
			startClass = df.getOWLThing();
		}
		
//		singleSuggestionMode = configurator.getSingleSuggestionMode();
		/*
		// create refinement operator
		if(operator == null) {
			operator = new RhoDRDown();
			((RhoDRDown)operator).setStartClass(startClass);
			((RhoDRDown)operator).setReasoner(reasoner);
		}
		((RhoDRDown)operator).setSubHierarchy(classHierarchy);
		((RhoDRDown)operator).setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
		((RhoDRDown)operator).setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		((RhoDRDown)operator).init();		
		*/
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
		if(operator instanceof CustomHierarchyRefinementOperator) {
			((CustomHierarchyRefinementOperator)operator).setClassHierarchy(classHierarchy);
			((CustomHierarchyRefinementOperator)operator).setObjectPropertyHierarchy(reasoner.getObjectPropertyHierarchy());
			((CustomHierarchyRefinementOperator)operator).setDataPropertyHierarchy(reasoner.getDatatypePropertyHierarchy());
		}
		
//		operator = new RhoDRDown(reasoner, classHierarchy, startClass, configurator);
			
		if(writeSearchTree) {
			File f = new File(searchTreeFile );
			if(f.getParentFile() != null){
				f.getParentFile().mkdirs();
			}
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
		
//		Set<OWLClassExpression> concepts = operator.refine(Thing.instance, 5);
//		for(OWLClassExpression concept : concepts) {
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
					
					LinkedList<OWLClassExpression> startClassCandidates = new LinkedList<OWLClassExpression>();
					startClassCandidates.add(existingDefinition);
					// hack for RhoDRDown
					if(operator instanceof RhoDRDown) {
						((RhoDRDown)operator).setDropDisjuncts(true);
					}
					LengthLimitedRefinementOperator upwardOperator = (LengthLimitedRefinementOperator) new OperatorInverter(operator);
					
					// use upward refinement until we find an appropriate start class
					boolean startClassFound = false;
					OWLClassExpression candidate;
					do {
						candidate = startClassCandidates.pollFirst();
						if(((ClassLearningProblem)learningProblem).getRecall(candidate)<1.0) {
							// add upward refinements to list
							Set<OWLClassExpression> refinements = upwardOperator.refine(candidate, OWLClassExpressionUtils.getLength(candidate));
//							System.out.println("ref: " + refinements);
							LinkedList<OWLClassExpression> refinementList = new LinkedList<OWLClassExpression>(refinements);
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
						logger.info("Reusing existing OWLClassExpression " + OWLAPIRenderers.toManchesterOWLSyntax(startClass) + " as start class for learning algorithm.");
					} else {
						logger.info("Generalised existing OWLClassExpression " + OWLAPIRenderers.toManchesterOWLSyntax(existingDefinition) + " to " + OWLAPIRenderers.toManchesterOWLSyntax(startClass) + ", which is used as start class for the learning algorithm.");
					}
					
//					System.out.println("start class: " + startClass);
//					System.out.println("existing def: " + existingDefinition);
//					System.out.println(reasoner.getIndividuals(existingDefinition));
					
					if(operator instanceof RhoDRDown) {
						((RhoDRDown)operator).setDropDisjuncts(false);
					}
					
				} else {
					Set<OWLClassExpression> superClasses = reasoner.getClassHierarchy().getSuperClasses(classToDescribe);
					if(superClasses.size() > 1) {
						startClass = df.getOWLObjectIntersectionOf(superClasses);
					} else if(superClasses.size() == 1){
						startClass = (OWLClassExpression) superClasses.toArray()[0];
					} else {
						startClass = df.getOWLThing();
						logger.warn(classToDescribe + " is equivalent to owl:Thing. Usually, it is not " +
								"sensible to learn a OWLClassExpression in this case.");
					}					
				}
			}				
		} else if(learningProblem instanceof PosOnlyLP) {
			examples = ((PosOnlyLP)learningProblem).getPositiveExamples();
		} else if(learningProblem instanceof PosNegLP) {
			examples = Helper.union(((PosNegLP)learningProblem).getPositiveExamples(),((PosNegLP)learningProblem).getNegativeExamples());
		}
	}

	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		EvaluatedDescription ed = getCurrentlyBestEvaluatedDescription();
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
	public TreeSet<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions() {
		return bestEvaluatedDescriptions.getSet();
	}	
	
	public double getCurrentlyBestAccuracy() {
		return bestEvaluatedDescriptions.getBest().getAccuracy();
	}
	
	@Override
	public void start() {
		stop = false;
		isRunning = true;
		reset();
		nanoStartTime = System.nanoTime();
		
		// highest accuracy so far
		double highestAccuracy = 0.0;
		OENode nextNode;

		addNode(startClass, null);
		
		int loop = 0;
		while (!terminationCriteriaSatisfied()) {
//			System.out.println("loop " + loop);
			
			if(!singleSuggestionMode && bestEvaluatedDescriptions.getBestAccuracy() > highestAccuracy) {
				highestAccuracy = bestEvaluatedDescriptions.getBestAccuracy();
				expressionTestCountLastImprovement = expressionTests;
				timeLastImprovement = System.nanoTime();
				long durationInMillis = getCurrentRuntimeInMilliSeconds();
				String durationStr = getDurationAsString(durationInMillis);
				logger.info("more accurate (" + dfPercent.format(highestAccuracy) + ") class expression found after " + durationStr + ": " + descriptionToString(bestEvaluatedDescriptions.getBest().getDescription()));
			}

			// chose best node according to heuristics
			nextNode = getNextNodeToExpand();
			int horizExp = nextNode.getHorizontalExpansion();
			
			// apply operator
			Monitor mon = MonitorFactory.start("refineNode");
			System.out.print("NEXT NODE: " + nextNode);
			TreeSet<OWLClassExpression> refinements = refineNode(nextNode);
			System.out.println(
					"||" + OWLClassExpressionUtils.getLength(nextNode.getDescription()) 
					+ "||" + heuristic.getNodeScore(nextNode)
					+ "##" + refinements.size()
					);
			mon.stop();
				
//			System.out.println("next node: " + nextNode);
//			for(OWLClassExpression refinement : refinements) {
//				System.out.println("refinement: " + refinement);
//			}
//			if((loop+1) % 500 == 0) {
//				System.out.println(getMinimumHorizontalExpansion() + " - " + getMaximumHorizontalExpansion());
//				System.exit(0);
//			}
			
			while(refinements.size() != 0) {
				// pick element from set
				OWLClassExpression refinement = refinements.pollFirst();
				int length = OWLClassExpressionUtils.getLength(refinement);
//				System.out.print(OWLAPIRenderers.toDLSyntax(refinement));
				// we ignore all refinements with lower length and too high depth
				// (this also avoids duplicate node children)
				if(length > horizExp && OWLClassExpressionUtils.getDepth(refinement) <= maxDepth) {
					
//					System.out.println("potentially adding " + refinement + " to search tree as child of " + nextNode + " " + new Date());
					Monitor mon2 = MonitorFactory.start("addNode");
					addNode(refinement, nextNode);
					mon2.stop();
					// adding nodes is potentially computationally expensive, so we have
					// to check whether max time is exceeded	
					if(terminationCriteriaSatisfied()) {
						break;
					}
//					System.out.println("addNode finished" + " " + new Date());
				} else {
//					System.out.println();
				}
		
//				System.out.println("  refinement queue length: " + refinements.size());
			}
			
			updateMinMaxHorizExp(nextNode);
			
			// writing the search tree (if configured)
			if (writeSearchTree) {
				String treeString = "best node: " + bestEvaluatedDescriptions.getBest() + "\n";
				if (refinements.size() > 1) {
					treeString += "all expanded nodes:\n";
					for (OWLClassExpression n : refinements) {
						treeString += "   " + n + "\n";
					}
				}
				treeString += startNode.toTreeString(baseURI, prefixes);
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
			totalRuntimeNs = System.nanoTime()-nanoStartTime;
			logger.info("Algorithm terminated successfully (time: " + Helper.prettyPrintNanoSeconds(totalRuntimeNs) + ", "+expressionTests+" descriptions tested, "  + nodes.size() + " nodes in the search tree).\n");
            logger.info(reasoner.toString());
		}

		if(singleSuggestionMode) {
			bestEvaluatedDescriptions.add(bestDescription, bestAccuracy, learningProblem);
		}		
		
		// print solution(s)
		logger.info("solutions:\n" + getSolutionString());
		
//		System.out.println(startNode.toTreeString(baseURI));
		
		isRunning = false;

		System.err.println(MonitorFactory.start("refineNode"));
		System.err.println(MonitorFactory.start("addNode"));
		System.err.println(MonitorFactory.start("lp"));
	}

	private OENode getNextNodeToExpand() {
		// we expand the best node of those, which have not achieved 100% accuracy
		// already and have a horizontal expansion equal to their length
		// (rationale: further extension is likely to add irrelevant syntactical constructs)
		Iterator<OENode> it = nodes.descendingIterator();
		while(it.hasNext()) {
			OENode node = it.next();
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
		return null;//nodes.last();
	}
	
	// expand node horizontically
	private TreeSet<OWLClassExpression> refineNode(OENode node) {
		// we have to remove and add the node since its heuristic evaluation changes through the expansion
		// (you *must not* include any criteria in the heuristic which are modified outside of this method,
		// otherwise you may see rarely occurring but critical false ordering in the nodes set)
		nodes.remove(node);
//		System.out.println("refining: " + node);
		int horizExp = node.getHorizontalExpansion();
		TreeSet<OWLClassExpression> refinements = (TreeSet<OWLClassExpression>) operator.refine(node.getDescription(), horizExp+1);
//		System.out.println(refinements);
		node.incHorizontalExpansion();
		node.setRefinementCount(refinements.size());
		nodes.add(node);
		return refinements;
	}
	
	// add node to search tree if it is not too weak
	// returns true if node was added and false otherwise
	private boolean addNode(OWLClassExpression description, OENode parentNode) {
//		System.out.print(OWLAPIRenderers.toDLSyntax(description));
//		System.out.println("d: " + description);
		
		// redundancy check (return if redundant)
		boolean nonRedundant = descriptions.add(description);
		if(!nonRedundant) {
//			System.out.println(": redundant");
			return false;
		}
		
		// check whether the OWLClassExpression is allowed
		if(!isDescriptionAllowed(description, parentNode)) {
//			System.out.println(": not allowed");
			return false;
		}
		
//		System.out.println("Test " + new Date());
		// quality of OWLClassExpression (return if too weak)
		Monitor mon = MonitorFactory.start("lp");
		double accuracy = learningProblem.getAccuracyOrTooWeak(description, noise);
		mon.stop();
		// issue a warning if accuracy is not between 0 and 1 or -1 (too weak)
		if(accuracy > 1.0 || (accuracy < 0.0 && accuracy != -1)) {
			logger.warn("Invalid accuracy value " + accuracy + " for OWLClassExpression " + description + ". This could be caused by a bug in the heuristic measure and should be reported to the DL-Learner bug tracker.");
			System.exit(0);
		}
		
//		System.out.println("Test2 " + new Date());
		expressionTests++;
//		System.out.println("acc: " + accuracy);
//		System.out.println(OWLClassExpression + " " + accuracy);
		if(accuracy == -1) {
//			System.out.println(": too weak");
			return false;
		}
		
		OENode node = new OENode(parentNode, description, accuracy);
			
		// link to parent (unless start node)
		if(parentNode == null) {
			startNode = node;
		} else {
			parentNode.addChild(node);
		}
//		System.out.println(node + "::" + heuristic.getNodeScore(node));
		nodes.add(node);
//		System.out.println("Test3 " + new Date());
		
		// in some cases (e.g. mutation) fully evaluating even a single OWLClassExpression is too expensive
		// due to the high number of examples -- so we just stick to the approximate accuracy
		if(singleSuggestionMode) {
			if(accuracy > bestAccuracy) {
				bestAccuracy = accuracy;
				bestDescription = description;
				logger.info("more accurate (" + dfPercent.format(bestAccuracy) + ") class expression found: " + descriptionToString(bestDescription)); // + getTemporaryString(bestDescription)); 
			}
			return true;
		} 
		
//		System.out.println("description " + OWLClassExpression + " accuracy " + accuracy);
		
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
		
//		System.out.println(isCandidate);
		
//		System.out.println("Test4 " + new Date());
		if(isCandidate) {
			OWLClassExpression niceDescription = rewriteNode(node);
			ConceptTransformation.transformToOrderedForm(niceDescription);
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
			
//			bestEvaluatedDescriptions.add(node.getDescription(), accuracy, learningProblem);
			
//			System.out.println(bestEvaluatedDescriptions.getSet().size());
		}
		
//		System.out.println("Test5 " + new Date());
//		System.out.println("best evaluated descriptions size: " + bestEvaluatedDescriptions.size() + " worst: " + bestEvaluatedDescriptions.getWorst());
		return true;
	}	
	
	// checks whether the OWLClassExpression is allowed
	private boolean isDescriptionAllowed(OWLClassExpression description, OENode parentNode) {
		if(isClassLearningProblem) {
			if(isEquivalenceProblem) {
				// the class to learn must not appear on the outermost property level
				if(occursOnFirstLevel(description, classToDescribe)) {
					return false;
				}
			} else {
				// none of the superclasses of the class to learn must appear on the
				// outermost property level
				TreeSet<OWLClassExpression> toTest = new TreeSet<OWLClassExpression>();
				toTest.add(classToDescribe);
				while(!toTest.isEmpty()) {
					OWLClassExpression d = toTest.pollFirst();
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
				OWLClassExpression existentialContext = context.toExistentialContext();
				boolean fillerFound = false;
				for(OWLIndividual instance : examples) {
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
//		Set<OWLClassExpression> siblingClasses = reasoner.getClassHierarchy().getSiblingClasses(classToDescribe);
//		for now, we just disable negation
		
		return true;
	}
	
	// determine whether a named class occurs on the outermost level, i.e. property depth 0
	// (it can still be at higher depth, e.g. if intersections are nested in unions)
	private boolean occursOnFirstLevel(OWLClassExpression description, OWLClassExpression cls) {
        return description.containsConjunct(cls) ||
                (description instanceof OWLObjectUnionOf && ((OWLObjectUnionOf) description).getOperands().contains(cls));

    }
	
	// check whether the node is a potential solution candidate
	private OWLClassExpression rewriteNode(OENode node) {
		OWLClassExpression description = node.getDescription();
		// minimize OWLClassExpression (expensive!) - also performs some human friendly rewrites
		OWLClassExpression niceDescription;
		if(useMinimizer) {
			niceDescription = minimizer.minimizeClone(description);
		} else {
			niceDescription = description;
		}
		// replace \exists r.\top with \exists r.range(r) which is easier to read for humans
		niceDescription = ConceptTransformation.replaceRange(niceDescription, reasoner);
		return niceDescription;
	}
	
	// check whether the node is a potential solution candidate
		private OWLClassExpression rewriteNodeSimple(OENode node) {
			OWLClassExpression description = node.getDescription();
			// minimize OWLClassExpression (expensive!) - also performs some human friendly rewrites
			OWLClassExpression niceDescription;
			if(useMinimizer) {
				niceDescription = minimizer.minimizeClone(description);
			} else {
				niceDescription = description;
			}
			// replace \exists r.\top with \exists r.range(r) which is easier to read for humans
			niceDescription = ConceptTransformation.replaceRange(niceDescription, reasoner);
			return niceDescription;
		}
	
	private boolean terminationCriteriaSatisfied() {
		return 
		stop || 
		(maxClassExpressionTestsAfterImprovement != 0 && (expressionTests - expressionTestCountLastImprovement >= maxClassExpressionTestsAfterImprovement)) ||
		(maxClassExpressionTests != 0 && (expressionTests >= maxClassExpressionTests)) ||
		(maxExecutionTimeInSecondsAfterImprovement != 0 && ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSecondsAfterImprovement*1000000000l))) ||
		(maxExecutionTimeInSeconds != 0 && ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSeconds*1000000000l))) ||
		(terminateOnNoiseReached && (100*getCurrentlyBestAccuracy()>=100-noisePercentage)) ||
		(stopOnFirstDefinition && (getCurrentlyBestAccuracy() >= 1));
	}
	
	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
		nodes = new TreeSet<OENode>(heuristic);
		descriptions = new TreeSet<OWLClassExpression>();
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

	public OENode getSearchTreeRoot() {
		return startNode;
	}
	
	@SuppressWarnings("unused")
	private String bestDescriptionToString() {
		EvaluatedDescription best = bestEvaluatedDescriptions.getBest();
		return OWLAPIRenderers.toManchesterOWLSyntax(best.getDescription()) + " (accuracy: " + dfPercent.format(best.getAccuracy()) + ")";
	}	
	
//	private String getTemporaryString(OWLClassExpression description) {
//		return descriptionToString(description) + " (pred. acc.: " + dfPercent.format(((PosNegLPStandard)learningProblem).getPredAccuracyOrTooWeakExact(description,1)) + ", F-measure: "+ dfPercent.format(((PosNegLPStandard)learningProblem).getFMeasureOrTooWeakExact(description,1)) + ")";
//	}
	
	private void updateMinMaxHorizExp(OENode node) {
		int newHorizExp = node.getHorizontalExpansion();
		
		// update maximum value
		maxHorizExp = Math.max(maxHorizExp, newHorizExp);
		
		// we just expanded a node with minimum horizontal expansion;
		// we need to check whether it was the last one
		if(minHorizExp == newHorizExp - 1) {
			
			// the best accuracy that a node can achieve 
			double scoreThreshold = heuristic.getNodeScore(node) + 1 - node.getAccuracy();
			
			for(OENode n : nodes.descendingSet()) {
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
	
	public TreeSet<OENode> getNodes() {
		return nodes;
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

	public Set<OWLClass> getAllowedConcepts() {
		return allowedConcepts;
	}

	public void setAllowedConcepts(Set<OWLClass> allowedConcepts) {
		this.allowedConcepts = allowedConcepts;
	}

	public Set<OWLClass> getIgnoredConcepts() {
		return ignoredConcepts;
	}

	public void setIgnoredConcepts(Set<OWLClass> ignoredConcepts) {
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
		return maxClassExpressionTests;
	}

	public void setMaxClassDescriptionTests(int maxClassDescriptionTests) {
		this.maxClassExpressionTests = maxClassDescriptionTests;
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

	public boolean isUseMinimizer() {
		return useMinimizer;
	}

	public void setUseMinimizer(boolean useMinimizer) {
		this.useMinimizer = useMinimizer;
	}

	public AbstractHeuristic getHeuristic() {
		return heuristic;
	}

	@Autowired(required=false)
	public void setHeuristic(AbstractHeuristic heuristic) {
		this.heuristic = heuristic;
	}

	public int getMaxClassExpressionTestsWithoutImprovement() {
		return maxClassExpressionTestsAfterImprovement;
	}

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
		return new CELOE(this);
	}

	public static void main(String[] args) throws Exception{
		
		OWLDataFactory df = new OWLDataFactoryImpl();
		PrefixManager pm = new DefaultPrefixManager("");
		List<OWLClassExpression> operands = new ArrayList<>();
		operands.add(df.getOWLObjectSomeValuesFrom(
				df.getOWLObjectProperty("p", pm), 
				df.getOWLClass("C", pm)));
		operands.add(df.getOWLObjectUnionOf(df.getOWLClass("A", pm), df.getOWLClass("B", pm)));
		Collections.sort(operands);
		OWLClassExpression ce = df.getOWLObjectIntersectionOf(operands);
		System.out.println(OWLAPIRenderers.toDLSyntax(ce));
		
		String cls = "http://purl.org/procurement/public-contracts#Tender";
		String file = "/home/me/work/datasets/e-procurement/dl-learner-sample-with-classes-pco.rdf";
		Model model = ModelFactory.createDefaultModel();
		model.read(new FileInputStream(file), null);
		OWLEntityTypeAdder.addEntityTypes(model);
		Query query = QueryFactory.create("SELECT (COUNT(distinct ?s) as ?cnt)  WHERE {" +
				"?s a <" + cls + ">.}", Syntax.syntaxARQ);
		System.out.println(ResultSetFormatter.asText(QueryExecutionFactory.create(query, model).execSelect()));
		query = QueryFactory.create("SELECT ?p (COUNT(distinct ?s) AS ?cnt) WHERE {" +
				"?s ?p ?o. ?p a <http://www.w3.org/2002/07/owl#ObjectProperty>." +
				"?s a <" + cls + ">. " +
//				"OPTIONAL{?x ?p ?o1. " +
//				"FILTER NOT EXISTS{?x a <" + cls + ">.}}" +
				
						"}GROUP BY ?p ORDER BY DESC(?cnt)", Syntax.syntaxARQ);
		
		System.out.println(ResultSetFormatter.asText(QueryExecutionFactory.create(query, model).execSelect()));
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos, "TURTLE");
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new ByteArrayInputStream(baos.toByteArray()));
		
		AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		AbstractReasonerComponent rc = new FastInstanceChecker(ks);
		rc.init();
		
		ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(df.getOWLClass(IRI.create("http://purl.org/procurement/public-contracts#Tender")));
		lp.init();
		
//		ELLearningAlgorithm alg = new ELLearningAlgorithm(lp, rc);
//		alg.setNoisePercentage(30);
//		alg.setClassToDescribe(df.getOWLClass(IRI.create("http://purl.org/procurement/public-contracts#Tender"));
//		alg.init();
		
		
		CELOE alg = new CELOE(lp, rc);
		alg.setMaxExecutionTimeInSeconds(10);
		alg.init();
		
		alg.start();
		
	}
	
}
