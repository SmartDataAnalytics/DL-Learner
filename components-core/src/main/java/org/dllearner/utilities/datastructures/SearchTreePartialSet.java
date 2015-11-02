package org.dllearner.utilities.datastructures;

import java.util.Collection;

public interface SearchTreePartialSet<T> {
	void retainAll(Collection<T> promisingNodes);
}
