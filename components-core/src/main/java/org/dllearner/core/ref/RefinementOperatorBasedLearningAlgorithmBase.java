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
package org.dllearner.core.ref;

import java.util.SortedSet;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningAlgorithm;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class RefinementOperatorBasedLearningAlgorithmBase<T> implements LearningAlgorithm{
	
	protected SearchTree<T, SearchTreeNode<T>> searchTree;
	
	protected SearchTreeNode<T> startNode;
	
	protected RefinementOperator<T> refinementOperator;
	
	protected SearchTreeHeuristic<T> heuristic;
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		// compute the start node
		startNode = computeStartNode();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#start()
	 */
	@Override
	public void start() {
		// apply some pre-processing
		preProcess();
		
		// start with empty search tree
		searchTree = new SearchTree<>(heuristic);
	
		// add start node to search tree
		searchTree.addNode(startNode);
		
		while(!terminationCriteriaSatisfied()) {
			// pick best node from search tree
			SearchTreeNode<T> currentNode = getNextNodeToExpand();
			
			// refine node
			SortedSet<T> refinements = refineNode(currentNode);
			
			// add each refinement to search tree
			for (T refinement : refinements) {
				addToSearchTree(refinement, currentNode);
			}
		}
		
		// apply some post-processing
		postProcess();
	}
	
	protected SortedSet<T> refineNode(SearchTreeNode<T> node) {
		return refinementOperator.refineNode(node.getData());
	}
	
	/**
	 * Checks whether the refinement is allowed to be added to the search tree 
	 * first by calling {@link #isValid(T refinement)}}, and if yes creates a
	 * new node which is added to the search tree.
	 * @param refinement the refinement
	 * @param parentNode the parent node
	 * @return whether the refinement is allowed to be added to the search tree
	 */
	protected boolean addToSearchTree(T refinement, SearchTreeNode<T> parentNode) {
		// check if the refinement is allowed
		boolean isValid = isValid(refinement);
		
		// only if it's allowed
		if(isValid) {
			// create a new node
			SearchTreeNode<T> node = createNode(refinement, parentNode);
			
			// add to search tree
			return searchTree.addNode(node);
		}
		
		// otherwise return FALSE
		return false;
	}
	
	protected SearchTreeNode<T> createNode(T refinement, SearchTreeNode<T> parentNode) {
		return new SearchTreeNodeSimple<>(refinement, parentNode);
	}
	
	protected void preProcess() {}

	protected void postProcess() {}

	protected abstract SearchTreeNode<T> computeStartNode();
	
	protected abstract SearchTreeNode<T> getNextNodeToExpand();
	
	
	/**
	 * Checks whether the object is valid for further refinement
	 * @param refinement the refinement
	 * @return whether the object is valid for further refinement
	 */
	protected abstract boolean isValid(T refinement);
	
	/**
	 * Checks whether the algorithm has to be terminated.
	 * @return whether the algorithm has to be terminated
	 */
	protected abstract boolean terminationCriteriaSatisfied();
	
	
	

}
