package org.dllearner.algorithms.qtl.operations.traversal;

import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator for post-order resp. depth-first traversal on the nodes in the query tree.
 *
 * 1. Traverse the subtrees
 * 2. Visit the root.
 *
 * @author Lorenz Buehmann
 */
public class PostOrderTreeTraversal<V, T extends GenericTree<V, T>> extends AbstractTreeTraversal<T> {

	private Deque<T> stack = new ArrayDeque<>();

	public PostOrderTreeTraversal(T root) {
		super(root);
		findNextLeaf(root);
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
		if (!stack.isEmpty()) {
			T top = stack.peek();
			List<T> children = top.getChildren();
			int pos = children.indexOf(res);
			if (pos < children.size() - 1) {
				findNextLeaf(children.get(pos + 1)); // find next leaf in right sub-tree
			}
		}

		return res;
	}

	/** find the first leaf in a tree rooted at cur and store intermediate nodes */
	private void findNextLeaf(T cur) {
		while (cur != null) {
			stack.push(cur);
			List<T> children = cur.getChildren();
			if(!children.isEmpty()) {
				cur = children.get(0);
			}
		}
	}
}
