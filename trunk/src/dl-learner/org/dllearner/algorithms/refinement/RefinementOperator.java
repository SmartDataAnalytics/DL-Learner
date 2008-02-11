package org.dllearner.algorithms.refinement;

import java.util.List;
import java.util.Set;

import org.dllearner.core.owl.Concept;

public interface RefinementOperator {

	public Set<Concept> refine(Concept concept);
	// SortedSet zu erzwingen ist nicht so elegant
	public Set<Concept> refine(Concept concept, int maxLength, List<Concept> knownRefinements);
	
}
