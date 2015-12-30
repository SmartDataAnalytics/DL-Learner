package org.dllearner.utilities.datastructures;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.AbstractSearchTreeNode;

public class AbstractSearchTree <T extends AbstractSearchTreeNode> {

	// all nodes in the search tree (used for selecting most promising node)
	protected NavigableSet<T> nodes;
	
	// the sort order on the set
	protected Comparator<T> sortOrderComp;

	// root of search tree
	protected T root;
	
	/**
	 * create a new search tree
	 * @param comparator the comparator to use for the nodes
	 */
	public AbstractSearchTree(Comparator<T> comparator) {
		sortOrderComp = comparator;
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
	public void notifyNode(T node) {
		nodes.add(node);
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
	public void updatePrepare(T node) {
		nodes.remove(node);
	}
	
	/**
	 * must be called after modifying a node, to support immutable set element pattern
	 */
	public void updateDone(T node) {
		notifyNode(node);
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

}
