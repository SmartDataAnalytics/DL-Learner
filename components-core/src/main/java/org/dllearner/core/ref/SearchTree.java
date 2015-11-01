package org.dllearner.core.ref;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Lorenz Buehmann
 *
 */
public class SearchTree<S, T extends SearchTreeNode<S>> {
	
	private SortedSet<T> tree;
	
	public SearchTree(Comparator<T> comparator) {
		tree = new TreeSet<>(comparator);
	}
	
	public boolean addNode(T node) {
		return tree.add(node);
	}
	
	public boolean removeNode(T node) {
		return tree.remove(node);
	}
	
	public SortedSet<T> getNodes() {
		return tree;
	}

}
