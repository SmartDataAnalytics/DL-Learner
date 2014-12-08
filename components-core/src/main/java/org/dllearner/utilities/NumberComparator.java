package org.dllearner.utilities;

import java.util.Comparator;

/**
 * java.lang.Number does not implement Comparable
 * Compare any type of Number except AtomicInteger and AtomicLong
 * @author Lorenz Buehmann
 *
 * @param <T>
 */
public class NumberComparator<T extends Number & Comparable<T>> implements Comparator<T> {

	public int compare(T a, T b) throws ClassCastException {
		return a.compareTo(b);
	}
}