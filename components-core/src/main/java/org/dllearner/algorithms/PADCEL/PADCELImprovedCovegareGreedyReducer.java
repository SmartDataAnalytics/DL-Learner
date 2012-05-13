package org.dllearner.algorithms.PADCEL;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.owl.Individual;

/**
 * This class implements "wise" coverage greedy strategy for compacting the partial definitions In
 * this strategy, the partial definitions will be chosen based on their coverage. When a partial
 * definition has been chosen, coverage of other partial definition will be recalculated
 * 
 * @author An C. Tran
 * 
 */

public class PADCELImprovedCovegareGreedyReducer implements PADCELReducer {

	Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Compact partial definition with noise allowed
	 * 
	 * @param partialDefinitions
	 *            Set of partial definitions
	 * @param positiveExamples
	 *            Set of positive examples (used to check whether partial definition is useful
	 * @param uncoveredPositiveExamples
	 *            Number of uncovered positive examples allowed
	 * 
	 * @return Subset of partial definitions that cover all positive examples
	 */
	@Override
	public SortedSet<PADCELExtraNode> compact(SortedSet<PADCELExtraNode> partialDefinitions,
			Set<Individual> positiveExamples) {
		return this.compact(partialDefinitions, positiveExamples, 0);
	}

	/**
	 * Compact partial definition with noise allowed
	 * 
	 * @param partialDefinitions
	 *            Set of partial definitions
	 * @param positiveExamples
	 *            Set of positive examples (used to check whether partial definition is useful
	 * @param uncoveredPositiveExamples
	 *            Number of uncovered positive examples allowed
	 * 
	 * @return Subset of partial definitions that cover (positive examples \ uncovered positive
	 *         examples)
	 */
	@Override
	public SortedSet<PADCELExtraNode> compact(SortedSet<PADCELExtraNode> partialDefinitions,
			Set<Individual> positiveExamples, int uncoveredPositiveExamples) {
		Set<Individual> positiveExamplesTmp = new HashSet<Individual>();
		positiveExamplesTmp.addAll(positiveExamples);

		TreeSet<PADCELExtraNode> reducedPartialDefinition = new TreeSet<PADCELExtraNode>(
				new PADCELCompletenessComparator());

		if (partialDefinitions.size() == 0)
			return reducedPartialDefinition;

		synchronized (partialDefinitions) {
			Object[] partialDefs = partialDefinitions.toArray();

			// the highest accurate partial definition
			// reducedPartialDefinition.add((PDLLExtraNode)partialDefs[0]);
			// positiveExamplesTmp.removeAll(((PDLLExtraNode)partialDefs[0]).getCoveredPositiveExamples());

			for (int i = 0; (positiveExamplesTmp.size() > uncoveredPositiveExamples)
					&& (i < partialDefinitions.size()); i++) {

				// count the number of different positive examples covered
				int counti = 0;
				for (Individual indi : ((PADCELExtraNode) partialDefs[i])
						.getCoveredPositiveExamples()) {
					if (positiveExamplesTmp.contains(indi))
						counti++;
				} // count the number of different covered positive examples by
					// i

				for (int j = i + 1; j < partialDefinitions.size(); j++) {
					int countj = 0;

					for (Individual indj : ((PADCELExtraNode) partialDefs[j])
							.getCoveredPositiveExamples())
						if (positiveExamplesTmp.contains(indj))
							countj++;

					// TODO: revise this code: Swapping should be done only one
					// time at the end
					// swap the partial definition so that the "best" partial
					// definition will be in the top
					if (countj > counti) {
						PADCELExtraNode tmp = (PADCELExtraNode) partialDefs[j];
						partialDefs[j] = partialDefs[i];
						partialDefs[i] = tmp;
						counti = countj;
					}
				}

				reducedPartialDefinition.add((PADCELExtraNode) partialDefs[i]);
				positiveExamplesTmp.removeAll(((PADCELExtraNode) partialDefs[i])
						.getCoveredPositiveExamples());
			}
		}

		return reducedPartialDefinition;
	}

}
