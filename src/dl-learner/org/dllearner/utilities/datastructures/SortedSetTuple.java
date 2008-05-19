package org.dllearner.utilities.datastructures;

import java.util.SortedSet;
import java.util.TreeSet;

public class SortedSetTuple<T> {

	private SortedSet<T> posSet;
	private SortedSet<T> negSet;
	
	public SortedSetTuple() {
		posSet = new TreeSet<T>();
		negSet = new TreeSet<T>();
	}
	
	public SortedSetTuple(SortedSet<T> posSet, SortedSet<T> negSet) {
		this.posSet = posSet;
		this.negSet = negSet;
	}

	public SortedSet<T> getPosSet() {
		return posSet;
	}

	public SortedSet<T> getNegSet() {
		return negSet;
	}
	
}
