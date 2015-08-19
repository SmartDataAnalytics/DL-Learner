package org.dllearner.utilities.datastructures;

import java.util.Collection;

public interface SearchTreePartialSet<T> {
	public void retainAll(Collection<T> promisingNodes);
}
