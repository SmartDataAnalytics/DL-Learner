package org.dllearner.algorithms.qtl.util;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.util.*;

/**
 * An iterator for depth-first traversal on a query tree.
 *
 * @author Lorenz Buehmann
 */
public class DepthFirstTreeIterator implements Iterator<RDFResourceTree> {

	private Deque<RDFResourceTree> stack = new ArrayDeque<>();

	private RDFResourceTree lastRet;

	public DepthFirstTreeIterator(RDFResourceTree tree) {
		stack.addAll(tree.getChildren());
	}

	@Override
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	@Override
	public RDFResourceTree next() {
		if(stack.isEmpty()) {
			throw new NoSuchElementException();
		}

		// remove first element from stack
		RDFResourceTree nextElement = stack.pop();

		// add children of this element to beginning of stack
		List<RDFResourceTree> children = nextElement.getChildren();
		Collections.reverse(children);
		for (RDFResourceTree child : children) {
			stack.addFirst(child);
		}

		lastRet = nextElement;

		return nextElement;
	}

	@Override
	public void remove() {
		if(lastRet == null) {
			throw new IllegalStateException();
		}

		lastRet.getParent().removeChild(lastRet);
	}
}
