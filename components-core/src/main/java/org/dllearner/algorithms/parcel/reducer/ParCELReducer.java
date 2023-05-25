package org.dllearner.algorithms.parcel.reducer;

/**
 * Interface for ParCEL reducer
 * 
 * @author An C. Tran
 * 
 */

import java.util.Set;
import java.util.SortedSet;

import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.semanticweb.owlapi.model.OWLIndividual;

public interface ParCELReducer {

	/**
	 * Compact a set of partial definition
	 * 
	 * @param partialDefinitions
	 *            Set of partial definitions
	 * @param positiveExamples
	 *            Set of positive examples (used to check the completeness)
	 * 
	 * @return "Minimal" set of partial definitions
	 */
	SortedSet<ParCELExtraNode> reduce(SortedSet<ParCELExtraNode> partialDefinitions,
									  Set<OWLIndividual> positiveExamples);

	/**
	 * Compact a set of partial definitions with noise (a number of uncovered positive examples are
	 * allowed)
	 * 
	 * @param partialDefinitions
	 *            Set of partial definitions
	 * @param positiveExamples
	 *            Set of positive examples (used to check the completeness)
	 * @param uncoveredPositiveExamples
	 *            Number of positive examples can be uncovered
	 * 
	 * @return "Minimal" set of partial definitions
	 */
	SortedSet<ParCELExtraNode> reduce(SortedSet<ParCELExtraNode> partialDefinitions,
									  Set<OWLIndividual> positiveExamples, int uncoveredPositiveExamples);
}
