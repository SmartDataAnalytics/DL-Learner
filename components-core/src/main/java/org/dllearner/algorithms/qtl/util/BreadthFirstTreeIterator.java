package org.dllearner.algorithms.qtl.util;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * An iterator for breadth-first traversal on a query tree.
 *
 * @author Lorenz Buehmann
 */
public class BreadthFirstTreeIterator implements Iterator<RDFResourceTree> {

	private Queue<RDFResourceTree> queue = new ArrayDeque<>();

	private RDFResourceTree lastRet;

	public BreadthFirstTreeIterator(RDFResourceTree tree) {
		queue.addAll(tree.getChildren());
	}

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public RDFResourceTree next() {
		if(queue.isEmpty()) {
			throw new NoSuchElementException();
		}

		// remove first element from queue
		RDFResourceTree nextElement = queue.poll();

		// add children of this element to end of queue
		queue.addAll(nextElement.getChildren());


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
