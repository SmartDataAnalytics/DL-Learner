package org.dllearner.algorithms.qtl.operations.traversal;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.util.*;

/**
 * An iterator for level-order resp. breadth-first traversal on the nodes in the query tree.
 * 
 * @author Lorenz Buehmann
 */
public class LevelOrderTreeTraversal implements TreeTraversal{

	private Deque<RDFResourceTree> stack = new ArrayDeque<>();

	public LevelOrderTreeTraversal(RDFResourceTree root) {
		stack.push(root);
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

		// retrieve and remove the head of queue
		RDFResourceTree res = stack.pop();

		// add children to stack
		List<RDFResourceTree> children = res.getChildren();
		children.forEach(c -> stack.push(c));

		return res;
	}
}
