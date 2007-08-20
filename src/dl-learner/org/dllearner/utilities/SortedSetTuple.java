package org.dllearner.utilities;

import java.util.SortedSet;

public class SortedSetTuple<T> {

	private SortedSet<T> posSet;
	private SortedSet<T> negSet;
	
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
