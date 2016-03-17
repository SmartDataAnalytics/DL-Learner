package org.dllearner.utilities.datastructures;

import java.util.*;

/**
 * A queue that keeps each element only once.
 * If you try to add an element that already exists - nothing will happen.
 *
 * @author Adamski http://stackoverflow.com/a/2319156/827927
 * @NotThreadSafe
 */
public class UniqueQueue<T> implements Queue<T> {

	private final Queue<T> queue = new ArrayDeque<>();
	private final Set<T> set = new HashSet<T>();

	@Override
	public boolean add(T t) {
		// Only add element to queue if the set does not contain the specified element.
		if (set.add(t))
			queue.add(t);
		return true; // Must always return true as per API def.
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		boolean ret = false;
		for (T t : arg0)
			if (set.add(t)) {
				queue.add(t);
				ret = true;
			}
		return ret;
	}

	@Override
	public T remove() throws NoSuchElementException {
		T ret = queue.remove();
		set.remove(ret);
		return ret;
	}

	@Override
	public boolean remove(Object arg0) {
		boolean ret = queue.remove(arg0);
		set.remove(arg0);
		return ret;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean ret = queue.removeAll(arg0);
		set.removeAll(arg0);
		return ret;
	}

	@Override
	public void clear() {
		set.clear();
		queue.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return set.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return set.containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return queue.iterator();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public Object[] toArray() {
		return queue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return queue.toArray(arg0);
	}

	@Override
	public T element() {
		return queue.element();
	}

	@Override
	public boolean offer(T e) {
		return queue.offer(e);
	}

	@Override
	public T peek() {
		return queue.peek();
	}

	@Override
	public T poll() {
		T element = queue.poll();
		if(element != null) {
			set.remove(element);
		}
		return element;
	}
}
