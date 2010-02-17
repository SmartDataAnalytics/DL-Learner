package org.dllearner.algorithms.refinement;

import java.util.List;
import java.util.Set;

import org.dllearner.core.owl.Description;

public interface RefinementOperator {

	public Set<Description> refine(Description concept);
	// SortedSet zu erzwingen ist nicht so elegant
	public Set<Description> refine(Description concept, int maxLength, List<Description> knownRefinements);
	
}
