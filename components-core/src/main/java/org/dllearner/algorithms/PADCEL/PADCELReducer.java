package org.dllearner.algorithms.PADCEL;

/**
 * Interface for PDLL reducer
 * 
 * @author An C. Tran
 * 
 */

import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.owl.Individual;

public interface PADCELReducer {

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
	public SortedSet<PADCELExtraNode> compact(SortedSet<PADCELExtraNode> partialDefinitions,
			Set<Individual> positiveExamples);

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
	public SortedSet<PADCELExtraNode> compact(SortedSet<PADCELExtraNode> partialDefinitions,
			Set<Individual> positiveExamples, int uncoveredPositiveExamples);
}
