package org.dllearner.utilities.datastructures;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Sets;

/**
 * 
 * Convenience data structure for keeping positive and negative examples in a single structure.
 * 
 * @author Jens Lehmann
 *
 * @param <T> The datatype (usually Individual or String).
 */
public class SortedSetTuple<T> {

	private SortedSet<T> posSet;
	private SortedSet<T> negSet;
	
	public SortedSetTuple() {
		posSet = new TreeSet<>();
		negSet = new TreeSet<>();
	}
	
	public SortedSetTuple(Set<T> posSet, Set<T> negSet) {
		this.posSet = new TreeSet<>(posSet);
		this.negSet = new TreeSet<>(negSet);
	}

	public SortedSet<T> getPosSet() {
		return posSet;
	}

	public SortedSet<T> getNegSet() {
		return negSet;
	}
	
	public Set<T> getCompleteSet() {
		return Sets.union(posSet, negSet);
	}
	
}
