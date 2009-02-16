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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.dllearner.core.owl.Thing;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;

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
	
	// all nodes in the search tree (used for selecting most promising node)
	private TreeSet<OENode> nodes;
	// root of search tree
	private OENode startNode;
	
	// all descriptions in the search tree plus those which were too weak (for fast redundancy check)
	private TreeSet<Description> descriptions;
	
	private EvaluatedDescriptionSet bestEvaluatedDescriptions = new EvaluatedDescriptionSet(LearningAlgorithm.MAX_NR_OF_RESULTS);
	
	private NamedClass classToDescribe;
	private boolean isEquivalenceProblem;
	
	private long nanoStartTime;
	
	// important parameters
	private double minAcc;
	
	@Override
	public Configurator getConfigurator() {
		return configurator;
	}
	
	public CELOE(ClassLearningProblem problem, ReasonerComponent reasoner) {
		super(problem, reasoner);
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
		options.add(CommonConfigOptions.maxExecutionTimeInSeconds());
		options.add(CommonConfigOptions.getNoisePercentage());
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
		
		// create refinement operator
		operator = new RhoDRDown(reasoner, classHierarchy, configurator);
		
		// we put important parameters in class variables
		minAcc = configurator.getNoisePercentage()/100d;
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
		
		// start class: intersection of super classes for definitions (since it needs to
		// capture all instances), but owl:Thing for learning subclasses (since it is
		// superfluous to add super classes in this case)
		Description startClass;
		if(isEquivalenceProblem) {
			Set<Description> superClasses = reasoner.getClassHierarchy().getSuperClasses(classToDescribe);
			startClass = new Intersection(new LinkedList<Description>(superClasses));
		} else {
			startClass = Thing.instance;
		}
		addNode(startClass, null);
		
		
		// print solution(s)
		logger.info("solution : " + bestEvaluatedDescriptions.getBest());		
		
		isRunning = false;
	}

	// expand node horizontically
	private void expandNode(OENode node) {
		
	}
	
	// add node to search tree if it is not too weak
	// returns true if node was added and false otherwise
	private boolean addNode(Description description, OENode parentNode) {
		
		// redundancy check (return if redundant)
		boolean nonRedundant = descriptions.add(description);
		if(!nonRedundant) {
			return false;
		}
		
		// quality of description (return if too weak)
		double accuracy = learningProblem.getAccuracyOrTooWeak(description, minAcc);
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
		
		// maybe add to best descriptions (method keeps set size fixed)
		bestEvaluatedDescriptions.add(description, accuracy, learningProblem);
	
		return true;
	}	
	
	// check whether the node is a potential solution candidate
	// (sufficient accuracy; minimal; rewriting steps?)
	private boolean checkNode(OENode node) {
		return true;
	}
	
	private boolean terminationCriteriaSatisfied() {
		return !stop && (System.nanoTime() - nanoStartTime >= (configurator.getMaxExecutionTimeInSeconds()/1000000));
	}
	
	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
//		candidates.clear();
		nodes = new TreeSet<OENode>(new OEHeuristicRuntime());
		descriptions = new TreeSet<Description>(new ConceptComparator());
		bestEvaluatedDescriptions.getSet().clear();
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}	
	
	@Override
	public void stop() {
		stop = true;
	}

}
