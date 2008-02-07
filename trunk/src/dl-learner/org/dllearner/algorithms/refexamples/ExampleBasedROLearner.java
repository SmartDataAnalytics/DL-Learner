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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.refinement.RefinementOperator;
import org.dllearner.algorithms.refinement.RhoDown;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.MultiConjunction;
import org.dllearner.core.dl.MultiDisjunction;
import org.dllearner.core.dl.Top;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
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
	private PosNegLP learningProblem;
	private PosOnlyDefinitionLP posOnlyLearningProblem;
	private boolean posOnly = false;
	private int nrOfExamples;
	private int nrOfPositiveExamples;
	private int nrOfNegativeExamples;
	
	// noise regulates how many positives can be misclassified and when the algorithm terminates
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
	
	// setting to true gracefully stops the algorithm
	private boolean stop = false;
	
	// solution protocol
	private boolean solutionFound = false;
	private List<Concept> solutions = new LinkedList<Concept>();	
	
	// used refinement operator and heuristic (exchangeable)
	private RhoDown operator;
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
	private SortedSet<Concept> properRefinements = new TreeSet<Concept>(conceptComparator);

	// blacklists
	private SortedSet<Concept> tooWeakList = new TreeSet<Concept>(conceptComparator);
	private SortedSet<Concept> overlyGeneralList = new TreeSet<Concept>(conceptComparator);
		
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
	private long algorithmStartTime;
	private long propernessCalcTimeNs = 0;
	private long propernessCalcReasoningTimeNs = 0;	
	private long childConceptsDeletionTimeNs = 0;
	private long refinementCalcTimeNs = 0;
	private long redundancyCheckTimeNs = 0;
	private long evaluateSetCreationTimeNs = 0;
	private long improperConceptsRemovalTimeNs = 0;
	
	public ExampleBasedROLearner(
			LearningProblem learningProblem,
			ReasoningService rs,
			RefinementOperator operator, 
			ExampleBasedHeuristic heuristic,
			// Set<AtomicConcept> allowedConcepts,
			// Set<AtomicRole> allowedRoles, 
			double noise,
			boolean writeSearchTree, 
			boolean replaceSearchTree, 
			File searchTreeFile, 
			boolean useTooWeakList, 
			boolean useOverlyGeneralList, 
			boolean useShortConceptConstruction
	) {	
		if(learningProblem instanceof PosNegLP) {
			PosNegLP lp = (PosNegLP) learningProblem;
			this.learningProblem = lp;
			posOnly = false;
			nrOfPositiveExamples = lp.getPositiveExamples().size();
			nrOfNegativeExamples = lp.getNegativeExamples().size();
		} else if(learningProblem instanceof PosOnlyDefinitionLP) {
			PosOnlyDefinitionLP lp = (PosOnlyDefinitionLP) learningProblem;
			this.posOnlyLearningProblem = lp;
			posOnly = true;
			nrOfPositiveExamples = lp.getPositiveExamples().size();
			nrOfNegativeExamples = lp.getPseudoNegatives().size();
		}
		nrOfExamples = nrOfPositiveExamples + nrOfNegativeExamples;
		this.rs = rs;
		this.operator = (RhoDown) operator;
		// initialise candidate set with heuristic as ordering
		candidates = new TreeSet<ExampleBasedNode>(heuristic);
		this.noise = noise;
		this.writeSearchTree = writeSearchTree;
		this.replaceSearchTree = replaceSearchTree;
		this.searchTreeFile = searchTreeFile;
		this.useTooWeakList = useTooWeakList;
		this.useOverlyGeneralList = useOverlyGeneralList;
		this.useShortConceptConstruction = useShortConceptConstruction;
		
		logger.setLevel(Level.DEBUG);
	}
	
	public void start() {
		
		// calculate quality threshold required for a solution
		allowedMisclassifications = (int) Math.round(noise * nrOfExamples);
		
		// start search with most general concept
		Top top = new Top();
		ExampleBasedNode topNode = new ExampleBasedNode(top);
		// top covers all negatives
		int coveredNegativeExamples = getNumberOfNegatives();
		topNode.setCoveredNegativeExamples(coveredNegativeExamples);
		topNode.setCoveredPositives(learningProblem.getPositiveExamples());
		topNode.setCoveredNegatives(learningProblem.getNegativeExamples());
		candidates.add(topNode);
		candidatesStable.add(topNode);
		
		// note that TOP may already be a solution
		ExampleBasedNode bestNode = topNode;
//		solutionFound = (coveredNegativeExamples == 0);
//		solutions = new LinkedList<Concept>();
//		if(solutionFound)
//			solutions.add(top);
		
		int loop = 0;
		
		algorithmStartTime = System.nanoTime();
		
		while(!solutionFound && !stop) {		
			
			printStatistics(false);
			
			// chose best node according to heuristics
			bestNode = candidates.last();
			// extend best node	
			newCandidates.clear();
			// TODO: why is the best node tempoariliy removed from the candidates set?
			candidates.remove(bestNode);
			extendNodeProper(bestNode, bestNode.getHorizontalExpansion()+1);
			candidates.add(bestNode);
			// newCandidates has been filled during node expansion
			candidates.addAll(newCandidates);
			candidatesStable.addAll(newCandidates);			
						
			if(writeSearchTree) {
				// String treeString = "";
				String treeString = "best expanded node: " + bestNode+ "\n";
				if(expandedNodes.size()>1) {
					treeString += "all expanded nodes:\n"; // due to minimum horizontal expansion:\n";
					for(ExampleBasedNode n : expandedNodes) {
						treeString += "   " + n + "\n";
					}
				}
				expandedNodes.clear();
				treeString += topNode.getTreeString();
				treeString += "\n";

				if(replaceSearchTree)
					Files.createFile(searchTreeFile, treeString);
				else
					Files.appendFile(searchTreeFile, treeString);
			}
			
			// Anzahl Schleifendurchläufe
			loop++;
			
			logger.debug("--- loop " + loop + " finished ---");	
			
		}
		
		if(solutionFound) {
			logger.info("\nsolutions:");
			for(Concept c : solutions) {
				logger.info("  " + c + " (length " + c.getLength() +", depth " + c.getDepth() + ")");
			}
		}
		System.out.println("size of candidate set: " + candidates.size());
		printStatistics(true);
		
		if(stop)
			System.out.println("Algorithm stopped.");
		else
			System.out.println("Algorithm terminated succesfully.");		
	}
	
	private void extendNodeProper(ExampleBasedNode node, int maxLength) {
		// Rekursionsanfang ist das Konzept am Knoten selbst; danach wird der Operator
		// so lange darauf angewandt bis alle proper refinements bis zu maxLength
		// gefunden wurden
		long propCalcNsStart = System.nanoTime();
		
		if(writeSearchTree)
			expandedNodes.add(node);
		
		if(node.getChildren().size()>maxNrOfChildren)
			maxNrOfChildren = node.getChildren().size();
		
		// Knoten in instabiler Menge muss aktualisiert werden
		// => wird jetzt schon vom Algorithmus entfernt
		/*
		boolean remove = candidates.remove(node);
		
		if(!remove) {
			System.out.println(candidates);
			System.out.println(candidatesStable);
			System.out.println(node);
			
			throw new RuntimeException("remove failed");
		}*/
		
		extendNodeProper(node, node.getConcept(), maxLength, 0);
		node.setHorizontalExpansion(maxLength);
		
		// wird jetzt schon im Kernalgorithmus hinzugefügt
		/*
		boolean add = candidates.add(node);
		if(!add) {
			throw new RuntimeException("add failed");
		}*/
		
		// Knoten wird entfernt und wieder hinzugefügt, da sich seine
		// Position geändert haben könnte => geht noch nicht wg. ConcurrentModification
		// falls Knoten wg. min. horiz. exp. expandiert werden 
		// candidates.remove(node);
		// candidates.add(node);
		propernessCalcTimeNs += (System.nanoTime()-propCalcNsStart);
	}
	

	
	// für alle proper refinements von concept bis maxLength werden Kinderknoten
	// für node erzeugt;
	// recDepth dient nur zur Protokollierung
	private void extendNodeProper(ExampleBasedNode node, Concept concept, int maxLength, int recDepth) {
		
		// führe Methode nicht aus, wenn Algorithmus gestoppt wurde (alle rekursiven Funktionsaufrufe
		// werden nacheinander abgebrochen, so dass ohne weitere Reasoninganfragen relativ schnell beendet wird)
		if(stop)
			return;
		
		if(recDepth > maxRecDepth)
			maxRecDepth = recDepth;
		
		// Refinements berechnen => hier dürfen dürfen refinements <= horizontal expansion
		// des Konzepts nicht gelöscht werden!
		long refinementCalcTimeNsStart = System.nanoTime();
		Set<Concept> refinements = operator.refine(concept, maxLength, null);
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
		
		long evaluateSetCreationTimeNsStart = System.nanoTime();
		
		// alle Konzepte, die länger als horizontal expansion sind, müssen ausgewertet
		// werden
		Set<Concept> toEvaluateConcepts = new TreeSet<Concept>(conceptComparator);
		Iterator<Concept> it = refinements.iterator();
		// for(Concept refinement : refinements) {
		while(it.hasNext()) {
			Concept refinement = it.next();
			if(refinement.getLength()>node.getHorizontalExpansion()) {
				// TODO: an dieser Stelle könnte man Algorithmen ansetzen lassen, die
				// versuchen properness-Anfragen zu vermeiden:
				// 1. Konzept kürzen und schauen, ob es Mutterkonzept entspricht
				// 2. Blacklist, die überprüft, ob Konzept too weak ist
				// (dann ist es auch proper)
				
				// sagt aus, ob festgestellt wurde, ob refinement proper ist
				// (sagt nicht aus, dass das refinement proper ist!)
				boolean propernessDetected = false;
				
				// 1. short concept construction
				if(useShortConceptConstruction) {
					// kurzes Konzept konstruieren
					Concept shortConcept = ConceptTransformation.getShortConcept(refinement, conceptComparator);
					int n = conceptComparator.compare(shortConcept, concept);
					
					// Konzepte sind gleich also Refinement improper
					if(n==0) {
						propernessTestsAvoidedByShortConceptConstruction++;
						propernessDetected = true;
					}
				}
				
				// 2. too weak test
				if(!propernessDetected && useTooWeakList) {
					if(refinement instanceof MultiConjunction) {
						boolean tooWeakElement = containsTooWeakElement((MultiConjunction)refinement);
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
				if(!propernessDetected)
					toEvaluateConcepts.add(refinement);
				
					
			}
		}
		evaluateSetCreationTimeNs += System.nanoTime() - evaluateSetCreationTimeNsStart;
		
		// System.out.println(toEvaluateConcepts.size());
		
		Set<Concept> improperConcepts = null;
		if(toEvaluateConcepts.size()>0) {
			// Test aller Konzepte auf properness (mit DIG in nur einer Anfrage)
			long propCalcReasoningStart = System.nanoTime();
			improperConcepts = rs.subsumes(toEvaluateConcepts, concept);
			propernessTestsReasoner+=toEvaluateConcepts.size();
			// boolean isProper = !learningProblem.getReasoningService().subsumes(refinement, concept);
			propernessCalcReasoningTimeNs += System.nanoTime() - propCalcReasoningStart;
		}

		long improperConceptsRemovalTimeNsStart = System.nanoTime();
		// die improper Konzepte werden von den auszuwertenden gelöscht, d.h.
		// alle proper concepts bleiben übrig (einfache Umbenennung)
		if(improperConcepts != null)
			toEvaluateConcepts.removeAll(improperConcepts);
		Set<Concept> properConcepts = toEvaluateConcepts;
		// alle proper concepts von refinements löschen
		refinements.removeAll(properConcepts);
		improperConceptsRemovalTimeNs += System.nanoTime() - improperConceptsRemovalTimeNsStart;
		
		for(Concept refinement : properConcepts) {
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
				if(useOverlyGeneralList && refinement instanceof MultiDisjunction) {
					if(containsOverlyGeneralElement((MultiDisjunction)refinement)) {
						conceptTestsOverlyGeneralList++;
						quality = getNumberOfNegatives();
						qualityKnown = true;
						newNode.setQualityEvaluationMethod(ExampleBasedNode.QualityEvaluationMethod.OVERLY_GENERAL_LIST);
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
					int misclassifications = nrOfPositiveExamples - coveredPositives.size();
					
					// iterate through all covered examples (examples which are not
					// covered do not need to be tested, because they remain uncovered)
					// TODO: DIG will be slow if we send each reasoner request individually
					// (however if we send everything in one request, too many instance checks
					// are performed => rely on fast instance checker [still to implement])
					for(Individual i : coveredPositives) {
						// TODO: move code to a separate function
						if(quality != -1) {
							boolean covered = rs.instanceCheck(refinement, i);
							if(!covered)
								misclassifications++;
							else
								newlyCoveredPositives.add(i);
							
							if(misclassifications > allowedMisclassifications)
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
						newNode.setCoveredNegatives(newlyCoveredNegatives);
						newNode.setCoveredPositives(newlyCoveredPositives);
					}
					
				}

				if(quality == -1) {
					newNode.setTooWeak(true);
					// Blacklist für too weak concepts
					tooWeakList.add(refinement);
				} else {
					// Lösung gefunden
					if(quality == 0) {
						solutionFound = true;
						solutions.add(refinement);
					}			
					
					newNode.setCoveredNegativeExamples(quality);
					newCandidates.add(newNode);
					// candidates.add(newNode);
					// candidatesStable.add(newNode);
				
					
					if(quality == getNumberOfNegatives())
						overlyGeneralList.add(refinement);
					
					// System.out.print(".");
				}
				
				node.addChild(newNode);
			}			
		}
		
		
		// es sind jetzt noch alle Konzepte übrig, die improper refinements sind
		// auf jedem dieser Konzepte wird die Funktion erneut aufgerufen, da sich
		// proper refinements ergeben könnten
		for(Concept refinement : refinements) {
			// for(int i=0; i<=recDepth; i++)
			//	System.out.print("  ");
			// System.out.println("call: " + refinement + " [maxLength " + maxLength + "]");
			extendNodeProper(node, refinement, maxLength, recDepth+1);
			// for(int i=0; i<=recDepth; i++)
			//	System.out.print("  ");
			// System.out.println("finished: " + refinement + " [maxLength " + maxLength + "]");			
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
			// Refinementoperator auf Konzept anwenden
			String bestNodeString = "currently best node: " + candidatesStable.last();
			// searchTree += bestNodeString + "\n";
			System.out.println(bestNodeString);
			String expandedNodeString = "next expanded node: " + candidates.last();
			// searchTree += expandedNodeString + "\n";
			System.out.println(expandedNodeString);		
			System.out.println("algorithm runtime " + Helper.prettyPrintNanoSeconds(algorithmRuntime));
			System.out.println("size of candidate set: " + candidates.size());
			// System.out.println("properness max recursion depth: " + maxRecDepth);
			// System.out.println("max. number of one-step refinements: " + maxNrOfRefinements);
			// System.out.println("max. number of children of a node: " + maxNrOfChildren);
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
			
			System.out.println("reasoning percentage: " + df.format(reasoningPercentage) + "%");
			System.out.println("   subsumption check time: " + df.format(subPercentage) + "%");		
			System.out.println("proper calculation percentage (wo. reasoning): " + df.format(propPercentage) + "%");
			System.out.println("   deletion time percentage: " + df.format(deletionPercentage) + "%");
			System.out.println("   refinement calculation percentage: " + df.format(refinementPercentage) + "%");
			System.out.println("      m calculation percentage: " + df.format(mComputationTimePercentage) + "%");
			System.out.println("      top calculation percentage: " + df.format(topComputationTimePercentage) + "%");
			System.out.println("   redundancy check percentage: " + df.format(redundancyCheckPercentage) + "%");
			System.out.println("   evaluate set creation time percentage: " + df.format(evaluateSetCreationTimePercentage) + "%");
			System.out.println("   improper concepts removal time percentage: " + df.format(improperConceptsRemovalTimePercentage) + "%");
			System.out.println("clean time percentage: " + df.format(cleanTimePercentage) + "%");
			System.out.println("onnf time percentage: " + df.format(onnfTimePercentage) + "%");
			System.out.println("shortening time percentage: " + df.format(shorteningTimePercentage) + "%");			
		}
		System.out.println("properness tests (reasoner/short concept/too weak list): " + propernessTestsReasoner + "/" + propernessTestsAvoidedByShortConceptConstruction 
				+ "/" + propernessTestsAvoidedByTooWeakList);
		System.out.println("concept tests (reasoner/too weak list/overly general list/redundant concepts): " + conceptTestsReasoner + "/"
				+ conceptTestsTooWeakList + "/" + conceptTestsOverlyGeneralList + "/" + redundantConcepts);	
	}
	
	@SuppressWarnings({"unused"})
	private int coveredNegativesOrTooWeak(Concept concept) {
		if(posOnly)
			return posOnlyLearningProblem.coveredPseudoNegativeExamplesOrTooWeak(concept);
		else
			return learningProblem.coveredNegativeExamplesOrTooWeak(concept);
	}
	
	private int getNumberOfNegatives() {
		if(posOnly)
			return posOnlyLearningProblem.getPseudoNegatives().size();
		else
			return learningProblem.getNegativeExamples().size();
	}	
	
	private boolean containsTooWeakElement(MultiConjunction mc) {
		for(Concept child : mc.getChildren()) {
			if(tooWeakList.contains(child))
				return true;
		}
		return false;
	}
	
	private boolean containsOverlyGeneralElement(MultiDisjunction md) {
		for(Concept child : md.getChildren()) {
			if(overlyGeneralList.contains(child))
				return true;
		}
		return false;
	}		
	
	public void stop() {
		stop = true;
	}

	public Concept getBestSolution() {
		return candidatesStable.last().getConcept();
	}

	public synchronized List<Concept> getBestSolutions(int nrOfSolutions) {
		List<Concept> best = new LinkedList<Concept>();
		int i=0;
		for(ExampleBasedNode n : candidatesStable.descendingSet()) {
			best.add(n.getConcept());
			if(i==nrOfSolutions)
				return best;
			i++;
		}
		return best;
	}
	
	public Score getSolutionScore() {
		if(posOnly)
			return posOnlyLearningProblem.computeScore(getBestSolution());
		else
			return learningProblem.computeScore(getBestSolution());
	}	
	

	
}
