/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.algorithms.celoe;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.midi.SysexMessage;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.CELOEConfigurator;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Restriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.DescriptionMinimizer;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;

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
public class CELOE extends LearningAlgorithm {

	private static Logger logger = Logger.getLogger(CELOE.class);
	private CELOEConfigurator configurator;
	
	private boolean isRunning = false;
	private boolean stop = false;	
	
//	private OEHeuristicStable heuristicStable = new OEHeuristicStable();
//	private OEHeuristicRuntime heuristicRuntime = new OEHeuristicRuntime();
	
	private RefinementOperator operator;
	private DescriptionMinimizer minimizer;
	
	// all nodes in the search tree (used for selecting most promising node)
	private TreeSet<OENode> nodes;
	// root of search tree
	private OENode startNode;
	// the class with which we start the refinement process
	private Description startClass;
	
	// all descriptions in the search tree plus those which were too weak (for fast redundancy check)
	private TreeSet<Description> descriptions;
	
	private EvaluatedDescriptionSet bestEvaluatedDescriptions;
	
	private NamedClass classToDescribe;
	private boolean isEquivalenceProblem;
	
	private long nanoStartTime;
	
	// important parameters
	private double minAcc;
	private double maxDepth;
	
	// utility variables
	private String baseURI;
	private Map<String, String> prefixes;
	private DecimalFormat dfPercent = new DecimalFormat("0.00%");
	private ConceptComparator descriptionComparator = new ConceptComparator();
	
	// statistical variables
	private int descriptionTests = 0;
	
	@Override
	public Configurator getConfigurator() {
		return configurator;
	}
	
	public CELOE(ClassLearningProblem problem, ReasonerComponent reasoner) {
		super(problem, reasoner);
		configurator = new CELOEConfigurator(this);
		classToDescribe = problem.getClassToDescribe();
		isEquivalenceProblem = problem.isEquivalenceProblem();
	}
	
	public static Collection<Class<? extends LearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends LearningProblem>> problems = new LinkedList<Class<? extends LearningProblem>>();
		problems.add(ClassLearningProblem.class);
		return problems;
	}	
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(CommonConfigOptions.useAllConstructor());
		options.add(CommonConfigOptions.useExistsConstructor());
		options.add(CommonConfigOptions.useHasValueConstructor());
		options.add(CommonConfigOptions.valueFreqencyThreshold());
		options.add(CommonConfigOptions.useCardinalityRestrictions());
		options.add(CommonConfigOptions.cardinalityLimit());
		options.add(CommonConfigOptions.useNegation());
		options.add(CommonConfigOptions.useBooleanDatatypes());
		options.add(CommonConfigOptions.useDoubleDatatypes());
		options.add(CommonConfigOptions.maxExecutionTimeInSeconds(10));
		options.add(CommonConfigOptions.getNoisePercentage());
		options.add(CommonConfigOptions.getMaxDepth(7));
		options.add(CommonConfigOptions.maxNrOfResults(10));
		return options;
	}
	
	public static String getName() {
		return "CELOE";
	}
	
	@Override
	public void init() throws ComponentInitException {
		// copy class hierarchy and modify it such that each class is only
		// reachable via a single path
		ClassHierarchy classHierarchy = reasoner.getClassHierarchy().clone();
		classHierarchy.thinOutSubsumptionHierarchy();		
		
		minimizer = new DescriptionMinimizer(reasoner);
		
		// start class: intersection of super classes for definitions (since it needs to
		// capture all instances), but owl:Thing for learning subclasses (since it is
		// superfluous to add super classes in this case)
		if(isEquivalenceProblem) {
			Set<Description> superClasses = reasoner.getClassHierarchy().getSuperClasses(classToDescribe);
			if(superClasses.size() > 1) {
				startClass = new Intersection(new LinkedList<Description>(superClasses));
			} else {
				startClass = (Description) superClasses.toArray()[0];
			}
			
		} else {
			startClass = Thing.instance;
		}		
		
		// create refinement operator
		operator = new RhoDRDown(reasoner, classHierarchy, startClass, configurator);
		baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();
		
		 bestEvaluatedDescriptions = new EvaluatedDescriptionSet(configurator.getMaxNrOfResults());
			
		// we put important parameters in class variables
		minAcc = configurator.getNoisePercentage()/100d;
		maxDepth = configurator.getMaxDepth();
	}

	@Override
	public Description getCurrentlyBestDescription() {
		return getCurrentlyBestEvaluatedDescription().getDescription();
	}

	@Override
	public List<Description> getCurrentlyBestDescriptions() {
		return bestEvaluatedDescriptions.toDescriptionList();
	}
	
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return bestEvaluatedDescriptions.getSet().last();
	}	
	
	@Override
	public SortedSet<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions() {
		return bestEvaluatedDescriptions.getSet();
	}	
	
	@Override
	public void start() {
		stop = false;
		isRunning = true;
		reset();
		nanoStartTime = System.nanoTime();
		
		// test
//		Description testD = null;
//		try {
////			testD = KBParser.parseConcept("(\"EPC\" AND EXISTS hasModelElements.(\"Function\" AND ALL previousObjects.BOTTOM))", "http://localhost/aris/sap_model.owl#");
//			testD = KBParser.parseConcept("(\"EPC\" AND EXISTS hasModelElements.(\"Function\" AND ALL nextObject.BOTTOM))", "http://localhost/aris/sap_model.owl#");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		double val = learningProblem.getAccuracyOrTooWeak(testD, minAcc);
//		System.out.println(testD);
//		System.out.println(val);
//		System.out.println(testD.getDepth());
//		System.exit(0);
		
		// highest accuracy so far
		double highestAccuracy = 0.0;
		OENode bestNode;

		addNode(startClass, null);
		
		int loop = 0;
		while (!terminationCriteriaSatisfied()) {

//			System.out.println(startNode.toTreeString(baseURI));
			
			if(bestEvaluatedDescriptions.getBest().getAccuracy() > highestAccuracy) {
				highestAccuracy = bestEvaluatedDescriptions.getBest().getAccuracy();
				logger.info("more accurate (" + dfPercent.format(highestAccuracy) + ") class expression found: " + descriptionToString(bestEvaluatedDescriptions.getBest().getDescription()));	
			}

			// chose best node according to heuristics
			bestNode = nodes.last();
			int horizExp = bestNode.getHorizontalExpansion();
			
			// apply operator
			Monitor mon = MonitorFactory.start("refineNode");
			TreeSet<Description> refinements = refineNode(bestNode);
			mon.stop();
				
			while(refinements.size() != 0) {
				// pick element from set
				Description refinement = refinements.pollFirst();
				int length = refinement.getLength();
				
				// we ignore all refinements with lower length and too high depth
				// (this also avoids duplicate node children)
				if(length > horizExp && refinement.getDepth() <= maxDepth) {
					
					Monitor mon2 = MonitorFactory.start("addNode");
					boolean added = addNode(refinement, bestNode);
					mon2.stop();
					
					// if refinements have the same length, we apply the operator again
					// (descending the subsumption hierarchy)
					if(added && length == horizExp) {
						// ... refine node (first check whether we need this as there will
						// the penalty for longer descriptions will be quite hard anyway)
					}

				}
		
			}
			
			// Anzahl Schleifendurchläufe
			loop++;
		}

		if (stop) {
			logger.info("Algorithm stopped ("+descriptionTests+" descriptions tested).\n");
		} else {
			logger.info("Algorithm terminated succesfully ("+descriptionTests+" descriptions tested).\n");
		}
		
		// print solution(s)
//		logger.info("solution : " + bestDescriptionToString());
		logger.info("solutions:\n" + getSolutionString());
		
//		System.out.println(startNode.toTreeString(baseURI));
		
		isRunning = false;
	}

	// expand node horizontically
	private TreeSet<Description> refineNode(OENode node) {
		// we have to remove and add the node since its heuristic evaluation changes through the expansion
		// (you *must not* include any criteria in the heuristic which are modified outside of this method,
		// otherwise you may see rarely occuring but critical false ordering in the nodes set)
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
	private boolean addNode(Description description, OENode parentNode) {
		
		// redundancy check (return if redundant)
		boolean nonRedundant = descriptions.add(description);
		if(!nonRedundant) {
			return false;
		}
		
		// check whether the description is allowed
		if(!isDescriptionAllowed(description)) {
			return false;
		}
		
		// quality of description (return if too weak)
		double accuracy = learningProblem.getAccuracyOrTooWeak(description, minAcc);
		descriptionTests++;
//		System.out.println(description + " " + accuracy);
		if(accuracy == -1) {
			return false;
		}
		
		OENode node = new OENode(parentNode, description, accuracy);
			
		// link to parent (unless start node)
		if(parentNode == null) {
			startNode = node;
		} else {
			parentNode.addChild(node);
		}
	
		nodes.add(node);
		
		// maybe add to best descriptions (method keeps set size fixed);
		// we need to make sure that this does not get called more often than
		// necessary since rewriting is expensive
		boolean isCandidate = !bestEvaluatedDescriptions.isFull();
		if(!isCandidate) {
			EvaluatedDescription worst = bestEvaluatedDescriptions.getWorst();
			double accThreshold = worst.getAccuracy();
			isCandidate = 
				(accuracy > accThreshold ||
				(accuracy >= accThreshold && description.getLength() < worst.getDescriptionLength()));
		}
		
		if(isCandidate) {
			Description niceDescription = rewriteNode(node);
			bestEvaluatedDescriptions.add(niceDescription, accuracy, learningProblem);			
		}
		
		return true;
	}	
	
	// checks whether the description is allowed
	private boolean isDescriptionAllowed(Description description) {
		if(isEquivalenceProblem) {
			// the class to learn must not appear on the outermost property level
			return !occursOnFirstLevel(description, classToDescribe);
		} else {
			// none of the superclasses of the class to learn must appear on the
			// outermost property level
			TreeSet<Description> toTest = new TreeSet<Description>();
			toTest.add(classToDescribe);
			while(!toTest.isEmpty()) {
				Description d = toTest.pollFirst();
				if(occursOnFirstLevel(description, d)) {
					return false;
				}
				toTest.addAll(reasoner.getClassHierarchy().getSuperClasses(d));
			}
			return true;
		}
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
	private Description rewriteNode(OENode node) {
		Description description = node.getDescription();
//		Description niceDescription = description;
		// minimize description (expensive!)
		Description niceDescription = minimizer.minimizeClone(description);
		// replace \exists r.\top with \exists r.range(r) which is easier to read for humans
		ConceptTransformation.replaceRange(niceDescription, reasoner);
		return niceDescription;
	}
	
	private boolean terminationCriteriaSatisfied() {
		return stop || ((System.nanoTime() - nanoStartTime) >= (configurator.getMaxExecutionTimeInSeconds()*1000000000l));
	}
	
	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
//		candidates.clear();
		nodes = new TreeSet<OENode>(new OEHeuristicRuntime());
		descriptions = new TreeSet<Description>(new ConceptComparator());
		bestEvaluatedDescriptions.getSet().clear();
		descriptionTests = 0;
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
//		int max = 10;
		int current = 1;
		String str = "";
		for(EvaluatedDescription ed : bestEvaluatedDescriptions.getSet().descendingSet()) {
			str += current + ": " + descriptionToString(ed.getDescription()) + " " + dfPercent.format(ed.getAccuracy()) + "\n";
			current++;
//			if(current == max) {
//				break;
//			}
		}
		return str;
	}
}
