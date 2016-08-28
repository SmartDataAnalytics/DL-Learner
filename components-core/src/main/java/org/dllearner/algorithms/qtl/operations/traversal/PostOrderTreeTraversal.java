package org.dllearner.algorithms.qtl.operations.traversal;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.util.*;

/**
 * An iterator for post-order resp. depth-first traversal on the nodes in the query tree.
 *
 * 1. Traverse the subtrees
 * 2. Visit the root.
 *
 * @author Lorenz Buehmann
 */
public class PostOrderTreeTraversal implements TreeTraversal{

	private Deque<RDFResourceTree> stack = new ArrayDeque<>();

	public PostOrderTreeTraversal(RDFResourceTree root) {
		findNextLeaf(root);
	}

	@Override
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	@Override
	public RDFResourceTree next() {
		if (!hasNext()) {
			throw new NoSuchElementException("All nodes have been visited!");
		}

		RDFResourceTree res = stack.pop();
		if (!stack.isEmpty()) {
			RDFResourceTree top = stack.peek();
			List<RDFResourceTree> children = top.getChildren();
			int pos = children.indexOf(res);
			if (pos < children.size() - 1) {
				findNextLeaf(children.get(pos + 1)); // find next leaf in right sub-tree
			}
		}

		return res;
	}

	/** find the first leaf in a tree rooted at cur and store intermediate nodes */
	private void findNextLeaf(RDFResourceTree cur) {
		while (cur != null) {
			stack.push(cur);
			List<RDFResourceTree> children = cur.getChildren();
			if(!children.isEmpty()) {
				cur = children.get(0);
			}
		}
	}
}
