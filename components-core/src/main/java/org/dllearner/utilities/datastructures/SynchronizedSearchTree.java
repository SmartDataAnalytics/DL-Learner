package org.dllearner.utilities.datastructures;

import java.util.Comparator;
import java.util.TreeSet;

import org.dllearner.core.AbstractSearchTreeNode;

import com.google.common.collect.Sets;

public class SynchronizedSearchTree<T extends AbstractSearchTreeNode> extends AbstractSearchTree<T> {

	public SynchronizedSearchTree(Comparator<T> comparator) {
		super(comparator);
		nodes = Sets.synchronizedNavigableSet(new TreeSet<T>(sortOrderComp));
	}

}
