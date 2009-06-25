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
package org.dllearner.algorithms.el;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.configurators.ELLearningAlgorithmConfigurator;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.refinementoperators.ELDown2;

/**
 * A learning algorithm for EL, which will based on an
 * ideal refinement operator.
 * 
 * The algorithm learns disjunctions of EL trees as follows:
 * - given pos. and neg. examples, noise in %, min coverage per tree x %
 * - it searches for an EL tree, which covers at least x % of all positive examples
 *   and at most (coverage_on_positives * noise) negative examples
 * - the covered examples are removed from the pos. and neg. examples
 * - termination: all(?) positive examples covered
 * 
 * ev. besser: feste Suchzeiten pro Baum => es wird dann jeweils der beste Baum gewählt
 * => Terminierung, wenn alles gecovered ist oder kein Baum mit ausreichender Qualität
 * in dem Zeitfenster gefunden wird
 * 
 * In contrast to many other algorithms, only one solution is returned. Additionally,
 * the algorithm is not really an anytime algorithm, since the solution is constructed
 * stepwise as a set of trees. 
 * 
 * TODO redundancy check
 * 
 * @author Jens Lehmann
 *
 */
public class ELLearningAlgorithmDisjunctive extends LearningAlgorithm {

	private static Logger logger = Logger.getLogger(ELLearningAlgorithmDisjunctive.class);	
	private ELLearningAlgorithmConfigurator configurator;
	
	private ELDown2 operator;
	
	private boolean isRunning = false;
	private boolean stop = false;
	
	private SearchTreeNode startNode;
	private ELHeuristic heuristic;
	private TreeSet<SearchTreeNode> candidates;
	
	// tree search
	private List<ELDescriptionTree> currentSolution = new LinkedList<ELDescriptionTree>();
	private EvaluatedDescription bestEvaluatedDescription;
	// how important not cover
	private double posWeight = 5;
	private int startPosExamplesSize;
	private int startNegExamplesSize;
	private SortedSet<Individual> currentPosExamples;
	private SortedSet<Individual> currentNegExamples;
	private ELDescriptionTree bestCurrentTree;
	private double bestCurrentScore = 0;
	private long treeStartTime;
	
	public ELLearningAlgorithmDisjunctive(PosNegLP problem, ReasonerComponent reasoner) {
		super(problem, reasoner);
	}
	
	public static String getName() {
		return "disjunctive EL learning algorithm";
	}	
	
	public static Collection<Class<? extends LearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends LearningProblem>> problems = new LinkedList<Class<? extends LearningProblem>>();
		problems.add(PosNegLP.class);
		return problems;
	}
	
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(CommonConfigOptions.getNoisePercentage());
		options.add(new StringConfigOption("startClass", "the named class which should be used to start the algorithm (GUI: needs a widget for selecting a class)"));
		return options;
	}	
	
	// we can assume a PosNegLP, because it is the only supported one
	private PosNegLP getLearningProblem() {
		return (PosNegLP) learningProblem;
	}
	
	@Override
	public Configurator getConfigurator() {
		return configurator;
	}	
	
	@Override
	public void init() throws ComponentInitException {
		heuristic = new DisjunctiveHeuristic();
		candidates = new TreeSet<SearchTreeNode>(heuristic);
		
		operator = new ELDown2(reasoner);
	}	
	
	@Override
	public void start() {
		stop = false;
		isRunning = true;
		reset();
		int treeCount = 0;
		
		while(!stop && !stoppingCriteriaSatisfied()) {
			
			treeStartTime = System.nanoTime();
			// create start node
			ELDescriptionTree top = new ELDescriptionTree(reasoner, Thing.instance);
			addDescriptionTree(top, null);
			bestCurrentTree = top;
			bestCurrentScore = Double.NEGATIVE_INFINITY;
			
			// main loop
			int loop = 0;
			while(!stop && !treeCriteriaSatisfied()) {
				// pick the best candidate according to the heuristic
				SearchTreeNode best = candidates.pollLast();
				// apply operator
				List<ELDescriptionTree> refinements = operator.refine(best.getDescriptionTree());
				// add all refinements to search tree, candidates, best descriptions
				for(ELDescriptionTree refinement : refinements) {
					addDescriptionTree(refinement, best);
				}
				loop++;
				// logging
				if(logger.isTraceEnabled()) {
					logger.trace("Choosen node " + best);
					logger.trace(startNode.getTreeString());
					logger.trace("Loop " + loop + " completed.");
				}
			}
			
			// we found a tree (partial solution)
			currentSolution.add(bestCurrentTree);
			Description bestDescription = bestCurrentTree.transformToDescription();
			// form union of trees found so far with 
			if(treeCount==0) {
				bestEvaluatedDescription = learningProblem.evaluate(bestDescription);
			} else {
				Union union = new Union(bestEvaluatedDescription.getDescription(), bestDescription);
				bestEvaluatedDescription = learningProblem.evaluate(union);
			}
			
			// remove already covered examples
			Iterator<Individual> it = currentPosExamples.iterator();
			int posCov = 0;
			while(it.hasNext()) {
				Individual ind = it.next();
				if(reasoner.hasType(bestDescription, ind)) {
					it.remove();
					posCov++;
				}
			}
			it = currentNegExamples.iterator();
			int negCov = 0;
			while(it.hasNext()) {
				Individual ind = it.next();
				if(reasoner.hasType(bestDescription, ind)) {
					it.remove();
					negCov++;
				}
			}
			logger.info("tree found: " + bestDescription + " (" + posCov + " pos covered, " + currentPosExamples.size() + " remaining, " + negCov + " neg covered, " + currentNegExamples.size() + " remaining");
			
			treeCount++;
		}
		
		// print solution
		logger.info("solution : " + bestEvaluatedDescription);
		
		isRunning = false;
	}

	// evaluates a description in tree form
	private void addDescriptionTree(ELDescriptionTree descriptionTree, SearchTreeNode parentNode) {
		
		// create search tree node
		SearchTreeNode node = new SearchTreeNode(descriptionTree);
		
		// compute score
		double score = getTreeScore(descriptionTree);
		node.setScore(score);
		
		// link to parent (unless start node)
		if(parentNode == null) {
			startNode = node;
		} else {
			parentNode.addChild(node);
		}
		
		// TODO: define "too weak" as a coverage on negative examples, which is
		// too high for the tree to be considered
		
		candidates.add(node);
		
		// check whether this is the best tree
		if(score > bestCurrentScore) {
			bestCurrentTree = descriptionTree;
			bestCurrentScore = score;
		}
	}
	
	private double getTreeScore(ELDescriptionTree tree) {
		
		Description d = tree.transformToDescription();
		
		double score = 0;
		
		// test coverage on current positive examples
		int posCovered = 0;
		for(Individual ind : currentPosExamples) {
			if(reasoner.hasType(d, ind)) {
				posCovered++;
				score += 1;
			}
		}
//		double posPercentage = posCovered/(double)currentPosExamples.size();
		
		// penalty if a minimum coverage is not achieved (avoids too many trees where
		// each tree has only little impact)
		if(startPosExamplesSize > 10 && posCovered<3 || posCovered < 1) {
			score -= 10;
		}
		
		// test coverage on current negative examples
		int negCovered = 0;
		for(Individual ind : currentNegExamples) {
			if(reasoner.hasType(d, ind)) {
				negCovered++;
				score -= posWeight;
			}
		}
//		double negPercentage = negCovered/(double)currentNegExamples.size();
		
		// length penalty
		score -= 0.1*tree.getSize();
		
		return score;
	}
	
	private boolean treeCriteriaSatisfied() {
		long runTime = System.nanoTime() - treeStartTime;
		// more than one second has passed
		if(runTime / 1000000000 > 1) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean stoppingCriteriaSatisfied() {
		// stop if we have a node covering all positives and none of the negatives
//		SearchTreeNode bestNode = candidates.last();
//		return (bestNode.getCoveredNegatives() == 0);
		
		// stop whan all positive examples have been covered
		return (currentPosExamples.size()==0);
	}
	
	private void reset() {
		// set all values back to their default values (used for running
		// the algorithm more than once)
		candidates.clear();
		currentSolution.clear();
		bestEvaluatedDescription = learningProblem.evaluate(Thing.instance);
		currentPosExamples = getLearningProblem().getPositiveExamples();
		currentNegExamples = getLearningProblem().getNegativeExamples();
		startPosExamplesSize = currentPosExamples.size();
		startNegExamplesSize = currentNegExamples.size();
	}
	
	@Override
	public void stop() {
		stop = true;
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}	
	
	@Override
	public Description getCurrentlyBestDescription() {
		return bestEvaluatedDescription.getDescription();
	}
	
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return bestEvaluatedDescription;
	}			
	
	/**
	 * @return the startNode
	 */
	public SearchTreeNode getStartNode() {
		return startNode;
	}	

}
