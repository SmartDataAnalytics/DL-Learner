package org.dllearner.algorithms.parcel;



import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.Set;
import java.util.SortedSet;

/**
 * Interface for ParCEL reducer
 *
 * @author An C. Tran
 *
 */
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
	SortedSet<ParCELExtraNode> compact(SortedSet<ParCELExtraNode> partialDefinitions,
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
	SortedSet<ParCELExtraNode> compact(SortedSet<ParCELExtraNode> partialDefinitions,
                                              Set<OWLIndividual> positiveExamples, int uncoveredPositiveExamples);
}
