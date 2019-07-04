package org.dllearner.algorithms.qtl.operations.traversal;

import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator for level-order resp. breadth-first traversal on the nodes in the query tree.
 * 
 * @author Lorenz Buehmann
 */
public class LevelOrderTreeTraversal<V, T extends GenericTree<V, T>> extends AbstractTreeTraversal<T> {

	private Deque<T> stack = new ArrayDeque<>();

	public LevelOrderTreeTraversal(T root) {
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
		T res = stack.removeFirst();

		// add children to stack
		List<T> children = res.getChildren();
		if(children != null) {
			stack.addAll(children);
		}

		return res;
	}
}
