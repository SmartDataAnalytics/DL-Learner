package org.dllearner.utilities.datastructures;

import java.util.Comparator;

import org.dllearner.core.AbstractSearchTreeNode;

/**
 * A Search Tree which does not maintain weak nodes in its set
 *
 * @param <T>
 */
public class SearchTreeNonWeak<T extends AbstractSearchTreeNode & WeakSearchTreeNode> extends SearchTree<T> {

	public SearchTreeNonWeak(Comparator<T> comparator) {
		super(comparator);
	}

	@Override
	public void notifyNode(T node) {
		if (!node.isTooWeak()) {
			nodes.add(node);
		}
	}

}
