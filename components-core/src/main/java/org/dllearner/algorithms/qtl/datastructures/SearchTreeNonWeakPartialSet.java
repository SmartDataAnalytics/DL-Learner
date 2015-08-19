package org.dllearner.algorithms.qtl.datastructures;

import java.util.Collection;
import java.util.Comparator;

import org.dllearner.core.AbstractSearchTreeNode;
import org.dllearner.utilities.datastructures.SearchTreeNonWeak;
import org.dllearner.utilities.datastructures.SearchTreePartialSet;
import org.dllearner.utilities.datastructures.WeakSearchTreeNode;

public class SearchTreeNonWeakPartialSet<T extends AbstractSearchTreeNode & WeakSearchTreeNode>
	extends SearchTreeNonWeak<T> implements SearchTreePartialSet<T> {

	public SearchTreeNonWeakPartialSet(Comparator<T> comparator) {
		super(comparator);
	}

	public void retainAll(Collection<T> promisingNodes) {
		this.nodes.retainAll(promisingNodes);
	}

}
