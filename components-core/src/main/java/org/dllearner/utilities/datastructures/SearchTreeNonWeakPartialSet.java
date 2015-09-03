package org.dllearner.utilities.datastructures;

import java.util.Collection;
import java.util.Comparator;

import org.dllearner.core.AbstractSearchTreeNode;

public class SearchTreeNonWeakPartialSet<T extends AbstractSearchTreeNode & WeakSearchTreeNode>
	extends SearchTreeNonWeak<T> implements SearchTreePartialSet<T> {

	public SearchTreeNonWeakPartialSet(Comparator<T> comparator) {
		super(comparator);
	}

	public void retainAll(Collection<T> promisingNodes) {
		this.nodes.retainAll(promisingNodes);
	}

}
