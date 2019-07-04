package org.dllearner.algorithms.qtl.operations.traversal;

import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.util.*;

/**
 * An iterator for pre-order resp. depth-first traversal on the nodes in the query tree.
 *
 * 1. Visit the root.
 * 2. Traverse the subtrees
 *
 * @author Lorenz Buehmann
 */
public class PreOrderTreeTraversal<V, T extends GenericTree<V, T>> extends AbstractTreeTraversal<T> {

	private Deque<T> stack = new ArrayDeque<>();

	public PreOrderTreeTraversal(T root) {
		super(root);
		stack.push(root);
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

		// retrieve and remove the head of queue
		T res = stack.pop();

		// add children to stack in reversed order
		List<T> children = res.getChildren();
		Collections.reverse(children);
		children.forEach(c -> stack.push(c));

		return res;
	}
}
