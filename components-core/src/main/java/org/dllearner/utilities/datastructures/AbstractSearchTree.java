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
package org.dllearner.utilities.datastructures;

import org.dllearner.core.AbstractSearchTreeNode;
import org.dllearner.core.Heuristic;

import java.util.*;

public class AbstractSearchTree <T extends AbstractSearchTreeNode> {

	// all nodes in the search tree (used for selecting most promising node)
	protected NavigableSet<T> nodes;

	// the sort order on the set
	protected Heuristic<T> sortOrderComp;

	// root of search tree
	protected T root;
	
	/**
	 * create a new search tree
	 * @param heuristic the comparator to use for the nodes
	 */
	public AbstractSearchTree(Heuristic<T> heuristic) {
		sortOrderComp = heuristic;
	}

	/**
	 * add node to the search tree
	 * @param parentNode the parent node or null if root
	 * @param node the node to add
	 */
	public void addNode(T parentNode, T node) {
		// link to parent (unless start node)
		if(parentNode == null) {
			this.setRoot(node);
		} else {
			parentNode.addChild(node);
		}
	}
	
	/**
	 * internally used by tree<->node contract to notify a tree about an added node
	 * @param node the node
	 */
	public final void notifyNode(T node) {
		if (node.getParent() == null || nodes.contains(node.getParent())) {
			if (allowedNode(node))
				nodes.add(node);
		}
	}

	/**
	 * filter certain nodes to be permitted in the node-set
	 * @param node node to test
	 * @return whether node is allowed in the node-set
	 */
	protected boolean allowedNode(T node) {
		return true;
	}

	/**
	 * set the tree root to a node
	 * @param node the node
	 */
	public void setRoot(T node) {
		if (this.root != null || !this.nodes.isEmpty()) {
			throw new Error("Tree Root already set");
		}
		this.root = node;
		node.notifyTree(this);
	}

	/**
	 * must be called before modifying a node, to support immutable set element pattern
	 * @param node the node
	 */
	public final void updatePrepare(T node) {
		for (T child : (Collection<T>)node.getChildren()) {
			if (allowedNode(child))
				updatePrepare(child);
		}
		nodes.remove(node);
	}
	
	/**
	 * must be called after modifying a node, to support immutable set element pattern
	 */
	public final void updateDone(T node) {
		if (allowedNode(node)) {
			nodes.add(node);
			for (T child : (Collection<T>)node.getChildren()) {
				updateDone(child);
			}
		}
	}

	/**
	 * @return an iterator over the elements in this search tree in descending comparison order
	 */
	public Iterator<T> descendingIterator() {
		return nodes.descendingIterator();
	}

	/**
	 * @return a set of the nodes in the search tree ordered in descending comparison order
	 */
	public SortedSet<T> descendingSet() {
		return nodes.descendingSet();
	}

	/**
	 * @return best node according to comparator
	 */
	public T best() {
		return nodes.last();
	}

	/**
	 * @return the underlying set of all tree nodes
	 */
	public Set<T> getNodeSet() {
		return nodes;
	}

	/**
	 * @return the tree size
	 */
	public int size() {
		return nodes.size();
	}

	/**
	 * @return the tree root node
	 */
	public T getRoot() {
		return root;
	}

	public Heuristic<T> getHeuristic() {
		return (Heuristic<T>)sortOrderComp;
	}
}
