package org.dllearner.utilities.datastructures;

import java.util.Comparator;
import java.util.TreeSet;

import org.dllearner.core.AbstractSearchTreeNode;

public class SearchTree<T extends AbstractSearchTreeNode> extends AbstractSearchTree<T> {

	public SearchTree(Comparator<T> comparator) {
		super(comparator);
		nodes = new TreeSet<T>(sortOrderComp);
	}
	
}
