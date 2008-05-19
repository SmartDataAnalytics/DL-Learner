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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.refinement.RefinementOperator;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.ConceptTransformation;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;

/**
 * Implements the example based refinement operator learning
 * approach.
 * 
 * TODO: Implement noise handling (here and/or in heuristics). When the 
 * noise level in the examples is set to a certain percentage, we
 * compute the number of positive examples corresponding to this
 * percentage. This is the maximum number of positive examples, which
 * can be misclassified (everything above is considered a too weak
 * concept wrt. the noise percentage).
 * 
 * TODO: Solve subsumption problems. It is easy to implement approximate fast
 * instance checks. However, subsumption is difficult to optimise. It should by
 * analysed whether it is reasonable to perform instance comparisions instead
 * of subsumption checks, e.g. a concept is only included in the search tree
 * when it covers strictly less examples than its parent. Note, that this does
 * not mean that concepts not bearing an advantage in classification are thrown 
 * away. They are just handled like improper refinements. [TODO: How does this 
 * differ from not checking subsumption at all?]
 * 
 * @author Jens Lehmann
 *
 */
public class ExampleBasedROLearner {

	private static Logger logger = Logger
		.getLogger(ExampleBasedROLearner.class);
	
	
	// basic setup: learning problem and reasoning service
	private ReasoningService rs;
	// often the learning problems needn't be accessed directly; instead
	// use the example sets below and the posonly variable
	private PosNegLP learningProblem;
	private PosOnlyDefinitionLP posOnlyLearningProblem;
	private Description startDescription;
	private boolean posOnly = false;
	private int nrOfExamples;
	private int nrOfPositiveExamples;
	private Set<Individual> positiveExamples;
	private int nrOfNegativeExamples;
	private Set<Individual> negativeExamples;
	
	// noise regulates how many positives can be misclassified and when the algorithm terminates
	private double noise = 0.0;
	private int allowedMisclassifications = 0;
	
	// positive only learning options:
	// if no negatives are given, then one possible strategy is to find a very special concept still entailing all positive examples;
	// this is realised by changing the termination criterion: a concept is a solution if it has been expanded x times (x is 
	// configurable) but no more special concept is found (all are either equivalent or too weak) 
	private int maxPosOnlyExpansion = 3;
	
	// search tree options
	private boolean writeSearchTree;
	private File searchTreeFile;
	private boolean replaceSearchTree = false;

	// constructs to improve performance
	private boolean useTooWeakList = true;
	private boolean useOverlyGeneralList = true;
	private boolean useShortConceptConstruction = true;
	
	//extended Options
	private long maxExecutionTimeInSeconds;
	private boolean maxExecutionTimeShown=false;
	private long minExecutionTimeInSeconds;
	private boolean minExecutionTimeShown=false;
	private int guaranteeXgoodDescriptions;
	private boolean guaranteeXgoodShown=false;
	
	// if set to false we do not test properness; this may seem wrong
	// but the disadvantage of properness testing are additional reasoner
	// queries and a search bias towards ALL r.something because 
	// ALL r.TOP is improper and automatically expanded further
	private boolean testProperness = false;
	
	// tree traversal means to run through the most promising concepts
	// and connect them in an intersection to find a solution
	// (this is called irregularly e.g. every 100 seconds)
	private boolean useTreeTraversal = false;
	
	// candidate reduction: using this mechanism we can simulate
	// the divide&conquer approach in many ILP programs using a
	// clause by clause search; after a period of time the candidate
	// set is reduced to focus CPU time on the most promising concepts
	private boolean useCandidateReduction = true;
	private int candidatePostReductionSize = 30;
	
	// setting to true gracefully stops the algorithm
	private boolean stop = false;
	
	// node from which algorithm has started
	private ExampleBasedNode startNode;	
	
	// solution protocol
	private boolean solutionFound = false;
	private List<Description> solutions = new LinkedList<Description>();	
	
	// used refinement operator and heuristic (exchangeable)
	private RhoDRDown operator;
//	private RefinementOperator operator;
	// private ExampleBasedHeuristic heuristic;
	
	// specifies whether to compute and log benchmark information
	private boolean computeBenchmarkInformation = false;
	
	// comparator used to maintain a stable ordering of nodes, i.e.
	// an ordering which does not change during the run of the algorithm
	private NodeComparatorStable nodeComparatorStable = new NodeComparatorStable();
	// stable candidate set; it has no functional part in the algorithm,
	// but is a list of the currently best concepts found
	private TreeSet<ExampleBasedNode> candidatesStable = new TreeSet<ExampleBasedNode>(nodeComparatorStable);
	
	// comparator used to create ordered sets of concepts
	private ConceptComparator conceptComparator = new ConceptComparator();
	
	// utility variables
	private DecimalFormat df = new DecimalFormat();		
	
	// candidates for refinement (used for directly accessing 
	// nodes in the search tree)
	private TreeSet<ExampleBasedNode> candidates;
	
	// new nodes found during a run of the algorithm
	private List<ExampleBasedNode> newCandidates = new LinkedList<ExampleBasedNode>();
	
	// all concepts which have been evaluated as being proper refinements
	private SortedSet<Description> properRefinements = new TreeSet<Description>(conceptComparator);

	// blacklists
	private SortedSet<Description> tooWeakList = new TreeSet<Description>(conceptComparator);
	private SortedSet<Description> overlyGeneralList = new TreeSet<Description>(conceptComparator);
		
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
	
	public ExampleBasedROLearner(
			LearningProblem learningProblem,
			ReasoningService rs,
			RefinementOperator operator, 
			ExampleBasedHeuristic heuristic,
			Description startDescription,
			// Set<AtomicConcept> allowedConcepts,
			// Set<AtomicRole> allowedRoles, 
			double noise,
			boolean writeSearchTree, 
			boolean replaceSearchTree, 
			File searchTreeFile, 
			boolean useTooWeakList, 
			boolean useOverlyGeneralList, 
			boolean useShortConceptConstruction,
			int maxExecutionTimeInSeconds,
			int minExecutionTimeInSeconds,
			int guaranteeXgoodDescriptions
	) {	
		
		if(learningProblem instanceof PosNegLP) {
			PosNegLP lp = (PosNegLP) learningProblem;
			this.learningProblem = lp;
			posOnly = false;
			positiveExamples = lp.getPositiveExamples();
			negativeExamples = lp.getNegativeExamples();
			nrOfPositiveExamples = positiveExamples.size();
			nrOfNegativeExamples = negativeExamples.size();
			
//			System.out.println(nrOfPositiveExamples);
//			System.out.println(nrOfNegativeExamples);
//			System.exit(0);
			
		} else if(learningProblem instanceof PosOnlyDefinitionLP) {
			PosOnlyDefinitionLP lp = (PosOnlyDefinitionLP) learningProblem;
			this.posOnlyLearningProblem = lp;
			posOnly = true;
			positiveExamples = lp.getPositiveExamples();
			negativeExamples = new TreeSet<Individual>();
			nrOfPositiveExamples = lp.getPositiveExamples().size();
//			nrOfNegativeExamples = lp.getPseudoNegatives().size();
			nrOfNegativeExamples = 0;
		}
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
		this.baseURI = rs.getBaseURI();
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
		this.minExecutionTimeInSeconds = minExecutionTimeInSeconds;
		this.guaranteeXgoodDescriptions = guaranteeXgoodDescriptions;
		
		
//		logger.setLevel(Level.DEBUG);
	}
	
	public void start() {
		runtime=System.currentTimeMillis();
		//RBC many comments can be removed
		//SimpleClock sc =new SimpleClock();
		// TODO: write a JUnit test for this problem (long-lasting or infinite loops because
		// redundant children of a node are called recursively after when the node is extended
		// twice)
		/*
//		String conceptStr = "(\"http://dl-learner.org/carcinogenesis#Compound\" AND (>= 2 \"http://dl-learner.org/carcinogenesis#hasStructure\".\"http://dl-learner.org/carcinogenesis#Ar_halide\" OR ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS TRUE) AND >= 5 \"http://dl-learner.org/carcinogenesis#hasBond\". TOP)))";
//		String conceptStr = "(\"http://dl-learner.org/carcinogenesis#Compound\" AND ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS TRUE) AND (\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS TRUE)))";
		String conceptStr = "(\"http://dl-learner.org/carcinogenesis#Compound\" AND (>= 3 \"http://dl-learner.org/carcinogenesis#hasStructure\".\"http://dl-learner.org/carcinogenesis#Halide\" OR ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS TRUE) AND ALL \"http://dl-learner.org/carcinogenesis#hasAtom\".TOP)))";
		String conceptStr2 = "(\"http://dl-learner.org/carcinogenesis#Compound\" AND (>= 4 \"http://dl-learner.org/carcinogenesis#hasStructure\".\"http://dl-learner.org/carcinogenesis#Halide\" OR ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS TRUE) AND ALL \"http://dl-learner.org/carcinogenesis#hasAtom\".TOP)))";
		try {
			NamedClass struc = new NamedClass("http://dl-learner.org/carcinogenesis#Compound");
			Description d = KBParser.parseConcept(conceptStr);
			Description d2 = KBParser.parseConcept(conceptStr2);
//			SortedSet<Description> ds = (SortedSet<Description>) operator.refine(d,15,null,struc);
//			System.out.println(ds);
			
//			System.out.println(RhoDRDown.checkIntersection((Intersection)d));
			
			
			Set<Individual> coveredNegatives = rs.instanceCheck(d, learningProblem.getNegativeExamples());
			Set<Individual> coveredPositives =  rs.instanceCheck(d, learningProblem.getPositiveExamples());
			ExampleBasedNode ebn = new ExampleBasedNode(d);
			ebn.setCoveredExamples(coveredPositives, coveredNegatives);
			
			properRefinements.add(d2);
			extendNodeProper(ebn,13);
			extendNodeProper(ebn,14);
			for(Description refinement: ebn.getChildConcepts())
				System.out.println("refinement: " + refinement);			
			
			// Individual i = new Individual("http://dl-learner.org/carcinogenesis#d101");
//			for(Individual i : learningProblem.getPositiveExamples())
//				rs.instanceCheck(ds.last(), i);
			
			System.out.println("finished");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);		
		*/
		
		// calculate quality threshold required for a solution
		allowedMisclassifications = (int) Math.round(noise * nrOfExamples);
		
		// start search with start class
		if(startDescription == null) {
			startNode = new ExampleBasedNode(Thing.instance);
			startNode.setCoveredExamples(positiveExamples, negativeExamples);
		} else {
			startNode = new ExampleBasedNode(startDescription);
			Set<Individual> coveredNegatives = rs.instanceCheck(startDescription, negativeExamples);
			Set<Individual> coveredPositives =  rs.instanceCheck(startDescription, positiveExamples);
			startNode.setCoveredExamples(coveredPositives, coveredNegatives);
		}
		
		candidates.add(startNode);
		candidatesStable.add(startNode);		

		ExampleBasedNode bestNode = startNode;

		int loop = 0;
		
		algorithmStartTime = System.nanoTime();
		long lastPrintTime = 0;
		long lastTreeTraversalTime = System.nanoTime();
		long lastReductionTime = System.nanoTime();
		// try a traversal after 100 seconds
		long traversalInterval = 300l * 1000000000l;
		long reductionInterval = 500l * 1000000000l;
		long currentTime;
		
		while(!solutionFound && !stop ) {		
			
			// print statistics at most once a second
			currentTime = System.nanoTime();
			if(currentTime - lastPrintTime > 3000000000l) {
				printStatistics(false);
				lastPrintTime = currentTime;
				logger.debug("--- loop " + loop + " started ---");				
			}
			//RBC
			//logger.debug("--- loop " + loop + " started ---");
			//printStatistics(false);
			//sc.printAndSet("before Traverse");
			// traverse the current search tree to find a solution
			if(useTreeTraversal && (currentTime - lastTreeTraversalTime > traversalInterval)) {
				traverseTree();
				lastTreeTraversalTime = System.nanoTime();
			}
			//sc.printAndSet("Traverse");
			
			// reduce candidates to focus on promising concepts
			if(useCandidateReduction && (currentTime - lastReductionTime > reductionInterval)) {
				reduceCandidates();
				lastReductionTime = System.nanoTime();
//				Logger.getRootLogger().setLevel(Level.TRACE);
			}			
			
			//sc.printAndSet("candidates");
//			System.out.println("next expanded: " + candidates.last().getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI));			
			// chose best node according to heuristics
			bestNode = candidates.last();
			// extend best node	
			newCandidates.clear();
			// best node is removed temporarily, because extending it can
			// change its evaluation
			candidates.remove(bestNode);
			extendNodeProper(bestNode, bestNode.getHorizontalExpansion()+1);
			candidates.add(bestNode);
			// newCandidates has been filled during node expansion
			candidates.addAll(newCandidates);
			candidatesStable.addAll(newCandidates);		
			
			//sc.printAndSet("after candidates");
//			System.out.println("done");			
			
			if(writeSearchTree) {
				// String treeString = "";
				String treeString = "best node: " + bestNode+ "\n";
				if(expandedNodes.size()>1) {
					treeString += "all expanded nodes:\n";
					for(ExampleBasedNode n : expandedNodes) {
						treeString += "   " + n + "\n";
					}
				}
				expandedNodes.clear();
				treeString += startNode.getTreeString(nrOfPositiveExamples, nrOfNegativeExamples, baseURI);
				treeString += "\n";

				if(replaceSearchTree)
					Files.createFile(searchTreeFile, treeString);
				else
					Files.appendFile(searchTreeFile, treeString);
			}
			//sc.printAndSet("before posonly");
			// special situation for positive only learning: the expanded node can become a solution (see explanations
			// for maxPosOnlyExpansion above)
			if(posOnly && (bestNode.getHorizontalExpansion() - bestNode.getConcept().getLength() >= maxPosOnlyExpansion)) {
				// check whether there are any child concept, which are not too weak (we only need to check whether the best concept
				// is too weak)
				ExampleBasedNode bestChild = null;
				if(bestNode.getChildren().size() > 0)
					bestChild = bestNode.getChildren().last();
				if(bestNode.getChildren().size() == 0 || bestChild.isTooWeak()) {
					solutions.add(bestNode.getConcept());
					System.out.println("solution: " + bestNode.getConcept());
					System.out.println("TODO: needs to be integrated with other stopping criteria");
					System.exit(0);
				}
			}
			
			//sc.printAndSet("before stopping");
			// handle termination criteria
			handleStoppingConditions();
			//sc.printAndSet("after stopping");
			
			//logger.info(minExecutionTimeReached()+"aaaaaaa "+solutions.size()+"::"+guaranteeXgoodDescriptions);
			//logger.info(solutionFound+"aaaaaaa "+stop);
			
			
			// Anzahl Schleifendurchläufe
			loop++;
			
	
			
		}//end while
		
			
		
		if(solutionFound ) {
			logger.info("best node " + candidatesStable.last().getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI));
			logger.info("\nsolutions ( top 5 ):");
			int show=1;
			String manchester="MANCHESTER:\n";
			String KBSyntax="KBSyntax:\n";
			for(Description c : solutions) {
				logger.info(show+": " + c.toString(baseURI,null) + " (length " + c.getLength() +", depth " + c.getDepth() + ")");
				// watch for String.replace Quick hack
				manchester+=show+": "+c.toManchesterSyntaxString(baseURI, new HashMap<String,String>()).replace("\"", "")+"\n";
				KBSyntax+=show+": " + c.toKBSyntaxString()+"\n";
				if(show>=5){break;}	show++;
			}
			logger.debug(manchester);
			logger.debug(KBSyntax);
			
			
		}
		
		logger.debug("size of candidate set: " + candidates.size());
		boolean showOrderedSolutions = false;
		printBestSolutions(20,showOrderedSolutions);
		printStatistics(true);
		
		if(stop)
			logger.info("Algorithm stopped.");
		else
			logger.info("Algorithm terminated succesfully.");		
	}
	
	// we apply the operator recursively until all proper refinements up
	// to the maxmimum length are reached
	private void extendNodeProper(ExampleBasedNode node, int maxLength) {
		long propCalcNsStart = System.nanoTime();
		
		if(writeSearchTree)
			expandedNodes.add(node);
		
		if(node.getChildren().size()>maxNrOfChildren)
			maxNrOfChildren = node.getChildren().size();
		
		extendNodeProper(node, node.getConcept(), maxLength, 0);
		node.setHorizontalExpansion(maxLength);
		
		propernessCalcTimeNs += (System.nanoTime()-propCalcNsStart);
	}
	
	// for all refinements of concept up to max length, we check whether they are properr
	// and call the method recursively if not
	// recDepth is used only for statistics
	private void extendNodeProper(ExampleBasedNode node, Description concept, int maxLength, int recDepth) {
		
		// do not execute methods if algorithm has been stopped (this means that the algorithm
		// will terminate without further reasoning queries)
		if(stop)
			return;
		
		if(recDepth > maxRecDepth)
			maxRecDepth = recDepth;
		
		// compute refinements => we must not delete refinements with low horizontal expansion,
		// because they are used in recursive calls of this method later on
		long refinementCalcTimeNsStart = System.nanoTime();
		Set<Description> refinements = operator.refine(concept, maxLength, null);
		refinementCalcTimeNs += System.nanoTime() - refinementCalcTimeNsStart;
		
		if(refinements.size()>maxNrOfRefinements)
			maxNrOfRefinements = refinements.size();
		
		long childConceptsDeletionTimeNsStart = System.nanoTime();
		// entferne aus den refinements alle Konzepte, die bereits Kinder des Knotens sind
		// for(Node n : node.getChildren()) {
		// 	refinements.remove(n.getConcept());
		// }
		
		// das ist viel schneller, allerdings bekommt man ein anderes candidate set(??)
		refinements.removeAll(node.getChildConcepts());

		childConceptsDeletionTimeNs += System.nanoTime() - childConceptsDeletionTimeNsStart;
		
//		if(refinements.size()<30) {
////			System.out.println("refinements: " + refinements);
//			for(Description refinement: refinements)
//				System.out.println("refinement: " + refinement);
//		}
		
		long evaluateSetCreationTimeNsStart = System.nanoTime();
		
		// alle Konzepte, die länger als horizontal expansion sind, müssen ausgewertet
		// werden
		TreeSet<Description> toEvaluateConcepts = new TreeSet<Description>(conceptComparator);
		Iterator<Description> it = refinements.iterator();
		// for(Concept refinement : refinements) {
		while(it.hasNext()) {			
			
			Description refinement = it.next();
			if(refinement.getLength()>node.getHorizontalExpansion()) {
				// sagt aus, ob festgestellt wurde, ob refinement proper ist
				// (sagt nicht aus, dass das refinement proper ist!)
				boolean propernessDetected = false;
				
				// 1. short concept construction
				if(useShortConceptConstruction) {
					// kurzes Konzept konstruieren
					Description shortConcept = ConceptTransformation.getShortConcept(refinement, conceptComparator);
					int n = conceptComparator.compare(shortConcept, concept);
					
					// Konzepte sind gleich also Refinement improper
					if(n==0) {
						propernessTestsAvoidedByShortConceptConstruction++;
						propernessDetected = true;
						
//						System.out.println("refinement " + refinement + " can be shortened");
//						System.exit(0);
					}
				}
				
				// 2. too weak test
				if(!propernessDetected && useTooWeakList) {			
					if(refinement instanceof Intersection) {
						boolean tooWeakElement = containsTooWeakElement((Intersection)refinement);
						if(tooWeakElement) {
							propernessTestsAvoidedByTooWeakList++;
							conceptTestsTooWeakList++;
							propernessDetected = true;
							// tooWeakList.add(refinement);
							
							// Knoten wird direkt erzeugt (es ist buganfällig zwei Plätze
							// zu haben, an denen Knoten erzeugt werden, aber es erscheint
							// hier am sinnvollsten)
							properRefinements.add(refinement);
							tooWeakList.add(refinement);
							
							ExampleBasedNode newNode = new ExampleBasedNode(refinement);
							newNode.setHorizontalExpansion(refinement.getLength()-1);
							newNode.setTooWeak(true);
							newNode.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.TOO_WEAK_LIST);
							node.addChild(newNode);
							
							// Refinement muss gelöscht werden, da es proper ist
							it.remove();
						}
					}
				}
				
				// properness konnte nicht vorher ermittelt werden
				if(!propernessDetected) {
					toEvaluateConcepts.add(refinement);
//					if(!res) {
//						System.out.println("already in: " + refinement);
//						Comparator comp = toEvaluateConcepts.comparator();
//						for(Description d : toEvaluateConcepts) {
//							if(comp.compare(d,refinement)==0)
//								System.out.println("see: " + d);
//						}						
//					}
				}
				
					
			}
			
//			System.out.println("handled " + refinement + " length: " + refinement.getLength() + " (new size: " + toEvaluateConcepts.size() + ")");			
			
		}
		evaluateSetCreationTimeNs += System.nanoTime() - evaluateSetCreationTimeNsStart;
		
//		System.out.println("intermediate 1 " + node.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI));		
		
		// System.out.println(toEvaluateConcepts.size());
		
		Set<Description> improperConcepts = null;
		if(toEvaluateConcepts.size()>0) {
			// Test aller Konzepte auf properness (mit DIG in nur einer Anfrage)
			if(testProperness) {
				long propCalcReasoningStart = System.nanoTime();
				improperConcepts = rs.subsumes(toEvaluateConcepts, concept);
				propernessTestsReasoner+=toEvaluateConcepts.size();
				// boolean isProper = !learningProblem.getReasoningService().subsumes(refinement, concept);
				propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart;
			}
		}

//		if(toEvaluateConcepts.size()<10)
//			System.out.println("to evaluate: " + toEvaluateConcepts);
//		else
//			System.out.println("to evaluate: more than 10");
		
		long improperConceptsRemovalTimeNsStart = System.nanoTime();
		// die improper Konzepte werden von den auszuwertenden gelöscht, d.h.
		// alle proper concepts bleiben übrig (einfache Umbenennung)
		if(improperConcepts != null)
			toEvaluateConcepts.removeAll(improperConcepts);
		Set<Description> properConcepts = toEvaluateConcepts;
		// alle proper concepts von refinements löschen
		refinements.removeAll(properConcepts);
		improperConceptsRemovalTimeNs += System.nanoTime() - improperConceptsRemovalTimeNsStart;
		
//		if(refinements.size()<10)
//			System.out.println("refinements: " + refinements);
//		else
//			System.out.println("refinements: more than 10");
//		
//		System.out.println("improper concepts: " + improperConcepts);		
		
		for(Description refinement : properConcepts) {
			long redundancyCheckTimeNsStart = System.nanoTime();
			boolean nonRedundant = properRefinements.add(refinement);
			redundancyCheckTimeNs += System.nanoTime() - redundancyCheckTimeNsStart;
			
			if(!nonRedundant)
				redundantConcepts++;
			
			// es wird nur ein neuer Knoten erzeugt, falls das Konzept nicht
			// schon existiert
			if(nonRedundant) {
			
				// newly created node
				ExampleBasedNode newNode = new ExampleBasedNode(refinement);
				// die -1 ist wichtig, da sonst keine gleich langen Refinements 
				// für den neuen Knoten erlaubt wären z.B. person => male
				newNode.setHorizontalExpansion(refinement.getLength()-1);
				
				boolean qualityKnown = false;
				int quality = -2;
				
				// overly general list verwenden
				if(useOverlyGeneralList && refinement instanceof Union) {
					if(containsOverlyGeneralElement((Union)refinement)) {
						conceptTestsOverlyGeneralList++;
//						quality = getNumberOfNegatives();
						quality = nrOfNegativeExamples;
						qualityKnown = true;
						newNode.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.OVERLY_GENERAL_LIST);
						newNode.setCoveredExamples(positiveExamples, negativeExamples);
					}	
					
				}

				// Qualität des Knotens auswerten
				if(!qualityKnown) {
					long propCalcReasoningStart2 = System.nanoTime();
					conceptTestsReasoner++;
					
					// quality = coveredNegativesOrTooWeak(refinement);
					
					// determine individuals which have not been covered yet (more efficient than full retrieval)
					Set<Individual> coveredPositives = node.getCoveredPositives();
					Set<Individual> newlyCoveredPositives = new HashSet<Individual>();
					
					// calculate how many pos. examples are not covered by the
					// parent node of the refinement
					int misclassifiedPositives = nrOfPositiveExamples - coveredPositives.size();
					
					// iterate through all covered examples (examples which are not
					// covered do not need to be tested, because they remain uncovered);
					// DIG will be slow if we send each reasoner request individually
					// (however if we send everything in one request, too many instance checks
					// are performed => rely on fast instance checker)
					for(Individual i : coveredPositives) {
						// TODO: move code to a separate function
						if(quality != -1) {
							boolean covered = rs.instanceCheck(refinement, i);
							if(!covered)
								misclassifiedPositives++;
							else
								newlyCoveredPositives.add(i);
							
							if(misclassifiedPositives > allowedMisclassifications)
								quality = -1;
							
						}
					}
					
					Set<Individual> newlyCoveredNegatives = null;
					if(quality != -1) {
						Set<Individual> coveredNegatives = node.getCoveredNegatives();
						newlyCoveredNegatives = new HashSet<Individual>();
					
						for(Individual i : coveredNegatives) {
							boolean covered = rs.instanceCheck(refinement, i);
							if(covered)
								newlyCoveredNegatives.add(i);
						}
					}
					
					propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart2;
					newNode.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.REASONER);
					if(quality != -1) {
						// quality is the number of misclassifications (if it is not too weak)
						quality = (nrOfPositiveExamples - newlyCoveredPositives.size())
							+ newlyCoveredNegatives.size();
						newNode.setCoveredExamples(newlyCoveredPositives, newlyCoveredNegatives);
					}

				}

				if(quality == -1) {
					newNode.setTooWeak(true);
					// Blacklist für too weak concepts
					tooWeakList.add(refinement);
				} else {
					// Lösung gefunden
					if(quality >= 0 && quality<=allowedMisclassifications && !posOnly) {
						solutionFound = true;
						solutions.add(refinement);
					}			
					

					newCandidates.add(newNode);
	
					// we need to make sure that all positives are covered
					// before adding something to the overly general list
					if((newNode.getCoveredPositives().size() == nrOfPositiveExamples) && quality == nrOfNegativeExamples) 
						overlyGeneralList.add(refinement);
						
				}
				
//				System.out.println(newNode.getConcept() + " " + quality);
				node.addChild(newNode);
			}			
		}
		
		// es sind jetzt noch alle Konzepte übrig, die improper refinements sind
		// auf jedem dieser Konzepte wird die Funktion erneut aufgerufen, da sich
		// proper refinements ergeben könnten
		for(Description refinement : refinements) {
			// for(int i=0; i<=recDepth; i++)
			//	System.out.print("  ");
//			System.out.println("call: " + refinement + " [maxLength " + maxLength + ", rec depth " + recDepth + "]");
			
			// check for redundancy (otherwise we may run into very time-intensive loops,
			// see planned JUnit test case $x)
			
			long redundancyCheckTimeNsStart = System.nanoTime();
			boolean redundant = properRefinements.contains(refinement);
			redundancyCheckTimeNs += System.nanoTime() - redundancyCheckTimeNsStart;
						
			if(!redundant)
				extendNodeProper(node, refinement, maxLength, recDepth+1);
			// for(int i=0; i<=recDepth; i++)
			//	System.out.print("  ");
			// System.out.println("finished: " + refinement + " [maxLength " + maxLength + "]");
//			System.exit(0);
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
		
		if(!finalStats) {
			
			ExampleBasedNode bestNode = candidatesStable.last();
//			double accuracy = 100 * ((bestNode.getCoveredPositives().size()
//			+ nrOfNegativeExamples - bestNode.getCoveredNegatives().size())/(double)nrOfExamples);
			// Refinementoperator auf Konzept anwenden
//			String bestNodeString = "currently best node: " + bestNode + " accuracy: " + df.format(accuracy) + "%";
			logger.debug("start node: " + startNode.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI));
			String bestNodeString = "currently best node: " + bestNode.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI);
			String bestNodeStringKBSyntax = "currently best node KBSyntax: " + bestNode.getConcept().toKBSyntaxString();
			
			// searchTree += bestNodeString + "\n";
			logger.debug(bestNodeString);
			logger.trace(bestNode.getStats(nrOfPositiveExamples, nrOfNegativeExamples));
			logger.debug(bestNodeStringKBSyntax);
			if(bestNode.getCoveredNegatives().size()<=5)logger.trace("covered negs: "+bestNode.getCoveredNegatives());
			String expandedNodeString = "next expanded node: " + candidates.last().getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI);
			// searchTree += expandedNodeString + "\n";
			logger.debug(expandedNodeString);		
			logger.debug("algorithm runtime " + Helper.prettyPrintNanoSeconds(algorithmRuntime));
			logger.debug("size of candidate set: " + candidates.size());
			// System.out.println("properness max recursion depth: " + maxRecDepth);
			// System.out.println("max. number of one-step refinements: " + maxNrOfRefinements);
			// System.out.println("max. number of children of a node: " + maxNrOfChildren);
			logger.debug("subsumption time: " + Helper.prettyPrintNanoSeconds(rs.getSubsumptionReasoningTimeNs()));
			logger.debug("instance check time: " + Helper.prettyPrintNanoSeconds(rs.getInstanceCheckReasoningTimeNs()));
			logger.debug("retrieval time: " + Helper.prettyPrintNanoSeconds(rs.getRetrievalReasoningTimeNs()));
		}
		
		if(computeBenchmarkInformation) {
			
			long reasoningTime = rs.getOverallReasoningTimeNs();
			double reasoningPercentage = 100 * reasoningTime/(double)algorithmRuntime;
			long propWithoutReasoning = propernessCalcTimeNs-propernessCalcReasoningTimeNs;
			double propPercentage = 100 * propWithoutReasoning/(double)algorithmRuntime;
			double deletionPercentage = 100 * childConceptsDeletionTimeNs/(double)algorithmRuntime;
			long subTime = rs.getSubsumptionReasoningTimeNs();
			double subPercentage = 100 * subTime/(double)algorithmRuntime;
			double refinementPercentage = 100 * refinementCalcTimeNs/(double)algorithmRuntime;
			double redundancyCheckPercentage = 100 * redundancyCheckTimeNs/(double)algorithmRuntime;
			double evaluateSetCreationTimePercentage = 100 * evaluateSetCreationTimeNs/(double)algorithmRuntime;
			double improperConceptsRemovalTimePercentage = 100 * improperConceptsRemovalTimeNs/(double)algorithmRuntime;
			double mComputationTimePercentage = 100 * operator.mComputationTimeNs/(double)algorithmRuntime;
			double topComputationTimePercentage = 100 * operator.topComputationTimeNs/(double)algorithmRuntime;
			double cleanTimePercentage = 100 * ConceptTransformation.cleaningTimeNs/(double)algorithmRuntime;
			double onnfTimePercentage = 100 * ConceptTransformation.onnfTimeNs/(double)algorithmRuntime;
			double shorteningTimePercentage = 100 * ConceptTransformation.shorteningTimeNs/(double)algorithmRuntime;
			
			logger.debug("reasoning percentage: " + df.format(reasoningPercentage) + "%");
			logger.debug("   subsumption check time: " + df.format(subPercentage) + "%");		
			logger.debug("proper calculation percentage (wo. reasoning): " + df.format(propPercentage) + "%");
			logger.debug("   deletion time percentage: " + df.format(deletionPercentage) + "%");
			logger.debug("   refinement calculation percentage: " + df.format(refinementPercentage) + "%");
			logger.debug("      m calculation percentage: " + df.format(mComputationTimePercentage) + "%");
			logger.debug("      top calculation percentage: " + df.format(topComputationTimePercentage) + "%");
			logger.debug("   redundancy check percentage: " + df.format(redundancyCheckPercentage) + "%");
			logger.debug("   evaluate set creation time percentage: " + df.format(evaluateSetCreationTimePercentage) + "%");
			logger.debug("   improper concepts removal time percentage: " + df.format(improperConceptsRemovalTimePercentage) + "%");
			logger.debug("clean time percentage: " + df.format(cleanTimePercentage) + "%");
			logger.debug("onnf time percentage: " + df.format(onnfTimePercentage) + "%");
			logger.debug("shortening time percentage: " + df.format(shorteningTimePercentage) + "%");			
		}
		
		logger.debug("properness tests (reasoner/short concept/too weak list): " + propernessTestsReasoner + "/" + propernessTestsAvoidedByShortConceptConstruction 
				+ "/" + propernessTestsAvoidedByTooWeakList);
		logger.debug("concept tests (reasoner/too weak list/overly general list/redundant concepts): " + conceptTestsReasoner + "/"
				+ conceptTestsTooWeakList + "/" + conceptTestsOverlyGeneralList + "/" + redundantConcepts);	
	}
	
//	@SuppressWarnings({"unused"})
//	private int coveredNegativesOrTooWeak(Description concept) {
//		if(posOnly)
//			return posOnlyLearningProblem.coveredPseudoNegativeExamplesOrTooWeak(concept);
//		else
//			return learningProblem.coveredNegativeExamplesOrTooWeak(concept);
//	}
	
//	private int getNumberOfNegatives() {
//		if(posOnly)
//			return posOnlyLearningProblem.getPseudoNegatives().size();
//		else
//			return learningProblem.getNegativeExamples().size();
//	}	
	
	private boolean containsTooWeakElement(Intersection mc) {
		for(Description child : mc.getChildren()) {
			if(tooWeakList.contains(child))
				return true;
		}
		return false;
	}
	
	private boolean containsOverlyGeneralElement(Union md) {
		for(Description child : md.getChildren()) {
			if(overlyGeneralList.contains(child))
				return true;
		}
		return false;
	}		
	
	// TODO: investigate whether it makes sense not to store all individuals
	// in the nodes, but instead perform instance checks in tree traversal
	// (it is only run in large intervals so it shouldn't be too expensive)
	private void traverseTree() {
//		ExampleBasedNode startNode = candidatesStable.last();
		ExampleBasedNode startNode = findBestTraversalStartNode();
		Description currentDescription = startNode.getConcept();
		Set<Individual> currentCoveredPos = startNode.getCoveredPositives();
		Set<Individual> currentCoveredNeg = startNode.getCoveredNegatives();
		double currentAccuracy = startNode.getAccuracy(nrOfPositiveExamples, nrOfNegativeExamples);
		int currentMisclassifications = nrOfPositiveExamples - currentCoveredPos.size() + currentCoveredNeg.size();
		logger.debug("tree traversal start node " + startNode.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI));
		logger.debug("tree traversal start accuracy: " + currentAccuracy);
		int i=0;
		// start from the most promising nodes
		NavigableSet<ExampleBasedNode> reverseView = candidatesStable.descendingSet();
		for(ExampleBasedNode currNode : reverseView) {
			// compute covered positives and negatives
			SortedSet<Individual> newCoveredPositives = new TreeSet<Individual>(currentCoveredPos);
			newCoveredPositives.retainAll(currNode.getCoveredPositives());
			SortedSet<Individual> newCoveredNegatives = new TreeSet<Individual>(currentCoveredNeg);
			newCoveredNegatives.retainAll(currNode.getCoveredNegatives());
			
			// compute the accuracy we would get by adding this node
			double accuracy = (newCoveredPositives.size() + nrOfNegativeExamples - newCoveredNegatives.size())/(double)(nrOfPositiveExamples+nrOfNegativeExamples);
			int misclassifications = nrOfPositiveExamples - newCoveredPositives.size() + newCoveredNegatives.size();
			int misclassifiedPositives = nrOfPositiveExamples - newCoveredPositives.size();
			
			int lostPositives = currentCoveredPos.size() - newCoveredPositives.size();
			
			// TODO: maybe we should also consider a minimum improvement when adding something
			// otherwise we could overfit 
			// we give double weith to lost positives, i.e. when one positive is lost at least 
			// two negatives need to be uncovered
			boolean consider = (misclassifications + lostPositives < currentMisclassifications) &&
				(misclassifiedPositives <= allowedMisclassifications);
//			boolean consider = (misclassifications < currentMisclassifications) &&
//			(misclassifiedPositives <= allowedMisclassifications);			
			
			// concept has been chosen, so construct it
			if(consider) {
				
				// construct a new concept as intersection of both
				Intersection mc = new Intersection(currentDescription, currNode.getConcept());
				
				ConceptTransformation.cleanConceptNonRecursive(mc);
				ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(mc, conceptComparator);
					
//				System.out.println("extended concept to: " + mc);
				logger.debug("misclassifications: " + misclassifications);
				logger.debug("misclassified positives: " + misclassifiedPositives);
				logger.debug("accuracy: " + accuracy);				
				
				// update variables
				currentDescription = mc;
				currentCoveredPos = newCoveredPositives;
				currentCoveredNeg = newCoveredNegatives;
				currentMisclassifications = misclassifications;
				currentAccuracy = accuracy;
				
				if(accuracy > 1 - noise) {
					logger.info("traversal found " + mc);
					logger.info("accuracy: " + accuracy);
					System.exit(0);
				}
			}
			
			i++;
			if(i==1000)
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
		for(ExampleBasedNode node : reverseView) {
			int score = 2 * node.getCoveredPositives().size() + (nrOfNegativeExamples - node.getCoveredNegatives().size());
			if(score > currScore) {
				currScore = score;
				currNode = node;
			}
			i++;
			// limit search because stable candidate set can grow very large
			if(i == 10000)
				break;
		}
		return currNode;
	}
	
	private void reduceCandidates() {
		Iterator<ExampleBasedNode> it = candidatesStable.descendingIterator();
		Set<ExampleBasedNode> promisingNodes = new HashSet<ExampleBasedNode>();
		int i = 0;
		while(it.hasNext() && promisingNodes.size()<candidatePostReductionSize) {
			ExampleBasedNode node = it.next();
//			System.out.println(node.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI));
			// first criterion: the considered node should have an accuracy gain over its parent
			// (avoids to use only the most promising node + all its refinements with equal accuracy)
			boolean hasAccuracyGain = (node.getParent() == null) || (node.getCoveredPositives().size() != node.getParent().getCoveredPositives().size())
				|| (node.getCoveredNegatives().size() != node.getParent().getCoveredNegatives().size());
			// second criterion: uncovered positives; it does not make much sense to pick nodes with
			// low potential for reaching a solution (already at the limit of misclassified positives)
			int misclassifiedPositives = nrOfPositiveExamples - node.getCoveredPositives().size();
			boolean hasRefinementPotential = (misclassifiedPositives <= Math.floor(0.65d*allowedMisclassifications));
			boolean keep = hasAccuracyGain && hasRefinementPotential;
			if(keep) {
				promisingNodes.add(node);
			}
			i++;
		}
		candidates.retainAll(promisingNodes);
		logger.debug("searched " + i + " nodes and picked the following promising descriptions:");
		for(ExampleBasedNode node : promisingNodes)
			logger.debug(node.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI));
	}
	
/*
	private Set<Individual> computeQuality(Description refinement, Set<Individual> coveredPositives) {
		Set<Individual> ret = new TreeSet<Individual>();
		int misclassifications;
		for(Individual i : coveredPositives) {
			boolean covered = rs.instanceCheck(refinement, i);
			if(!covered)
				misclassifications++;
			else
				ret.add(i);
				
			if(misclassifications > allowedMisclassifications)
				return null;
		}
	}
*/
	
	public void stop() {
		stop = true;
	}

	public Description getBestSolution() {
		return candidatesStable.last().getConcept();
	}

	public synchronized List<Description> getBestSolutions(int nrOfSolutions) {
		List<Description> best = new LinkedList<Description>();
		int i=0;
		for(ExampleBasedNode n : candidatesStable.descendingSet()) {
			
			best.add(n.getConcept());
			if(i==nrOfSolutions)
				return best;
			i++;
		}
		return best;
	}
	
	//HACK
	public List<String> getBestSolutionsAsKBSyntax(int nrOfSolutions){
		if(nrOfSolutions==0)nrOfSolutions=99999;
		List<String> result = new LinkedList<String>();
		for (Description d : solutions) {
			
			result.add(d.toKBSyntaxString());
			if(result.size()==nrOfSolutions)break;
			//if(result.size()==nrOfSolutions)break;
		}
		if(result.size()==0){
			
			for(ExampleBasedNode n : candidatesStable.descendingSet()) {
				
				result.add(n.getConcept().toKBSyntaxString());
				if(result.size()>=nrOfSolutions)
					return result;
				
			}
		}
		return result;
	}
	
	
	public void printBestSolutions(int nrOfSolutions, boolean showOrderedSolutions){
		if(!logger.isTraceEnabled())
			return;
		//if(!logger.getLevel().toString().equalsIgnoreCase("TRACE"))return;
		if(nrOfSolutions==0)
			nrOfSolutions=candidatesStable.size();
		int i=0;
		for(ExampleBasedNode n : candidatesStable.descendingSet()) {
			if (n.getAccuracy(nrOfPositiveExamples, nrOfNegativeExamples)<1)break;
			logger.trace("best: "+n.getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI));
			if(i==nrOfSolutions)
				break ;
			i++;
		}
		
		
		
		if(showOrderedSolutions) {
			logger.trace("ordered by generality (most special solutions first):");
			SubsumptionComparator sc = new SubsumptionComparator(rs);
			TreeSet<Description> solutionsOrderedBySubsumption = new TreeSet<Description>(sc);
			solutionsOrderedBySubsumption.addAll(solutions);
			for(Description d : solutionsOrderedBySubsumption)
				logger.trace("special: "+d);
		}
		/*for (int j = 0; j < solutions.size(); j++) {
			Description d = solutions.get(j);
			logger.trace(d.toString());
		}*/
		
	}
	
	public Score getSolutionScore() {
		if(posOnly)
			return posOnlyLearningProblem.computeScore(getBestSolution());
		else
			return learningProblem.computeScore(getBestSolution());
	}
	
	public ExampleBasedNode getStartNode() {
		return startNode;
	}
	
	private void handleStoppingConditions(){
		solutionFound = (guaranteeXgoodDescriptions() );
		solutionFound = (minExecutionTimeReached()&& solutionFound);
		if(maxExecutionTimeReached()) { 
			stop();
			if(solutions.size()>0)solutionFound = true;
		}
	}
	
	private boolean guaranteeXgoodDescriptions(){
		if(guaranteeXgoodShown)return true;
		if(solutions.size()>guaranteeXgoodDescriptions){
			logger.info("Minimum number ("+guaranteeXgoodDescriptions+") of good descriptions reached, stopping now...");
			guaranteeXgoodShown=true;
			return true;}
		else return false;
		
	}
	
	
	private boolean maxExecutionTimeReached(){
		if(maxExecutionTimeInSeconds==0)return false;
		if(maxExecutionTimeShown)return true;
		long needed = System.currentTimeMillis()- this.runtime;
		long maxMilliSeconds = maxExecutionTimeInSeconds *1000 ;
		if(maxMilliSeconds<needed){
			logger.info("Maximum time ("+maxExecutionTimeInSeconds+" seconds) reached, stopping now...");
			maxExecutionTimeShown=true;
			return true;}
		else return false;
		
	}
	
	/**
	 * true if minExecutionTime reached
	 * @return true
	 */
	private boolean minExecutionTimeReached(){
		if(minExecutionTimeShown)return true;
		long needed = System.currentTimeMillis()- this.runtime;
		long minMilliSeconds = minExecutionTimeInSeconds *1000 ;
		if(minMilliSeconds<needed){
			logger.info("Minimum time ("+minExecutionTimeInSeconds+" seconds) reached, stopping when next solution is found");
			minExecutionTimeShown=true;
			return true;}
		else return false;
		
	}
	
}
