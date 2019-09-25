package org.dllearner.algorithms.qtl.operations.traversal;

import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;

import java.util.*;

/**
 * An iterator for post-order resp. depth-first traversal on the nodes in the query tree.
 *
 * 1. Traverse the subtrees
 * 2. Visit the root.
 *
 * @author Lorenz Buehmann
 */
public class PostOrderTreeTraversal2<V, T extends GenericTree<V, T>> extends AbstractTreeTraversal<T> {

	private Deque<T> stack = new ArrayDeque<>();

	public PostOrderTreeTraversal2(T root) {
		super(root);
		buildPostOrder(root, stack);
	}

	@Override
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException("All nodes have been visited!");
		}

		T res = stack.pop();

		return res;
	}

	private void buildPostOrder(T node, Deque<T> postOrder) {
		for (T child : node.getChildren()) {
			buildPostOrder(child, postOrder);
		}
		postOrder.add(node);
	}
}
