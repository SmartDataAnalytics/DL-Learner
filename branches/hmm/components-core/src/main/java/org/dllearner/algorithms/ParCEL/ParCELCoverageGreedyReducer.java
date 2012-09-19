package org.dllearner.algorithms.ParCEL;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.Individual;

/**
 * This class implements a simple strategy for compacting the partial definition set In this
 * strategy, the partial definition will be chosen based on their accuracy. The partial definition
 * with the best accuracy will be chosen first and the rests will not be re-calculated before the
 * next reduction
 * 
 * @author An C. Tran
 * 
 */
public class ParCELCoverageGreedyReducer implements ParCELReducer {

	/**
	 * Compact partial definitions
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
	public SortedSet<ParCELExtraNode> compact(SortedSet<ParCELExtraNode> partialDefinitions,
			Set<Individual> positiveExamples) {
		return compact(partialDefinitions, positiveExamples, 0);
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
	public SortedSet<ParCELExtraNode> compact(SortedSet<ParCELExtraNode> partialDefinitions,
			Set<Individual> positiveExamples, int uncoveredPositiveExamples) {

		Set<Individual> positiveExamplesTmp = new HashSet<Individual>();
		positiveExamplesTmp.addAll(positiveExamples);

		TreeSet<ParCELExtraNode> minimisedPartialDefinition = new TreeSet<ParCELExtraNode>(
				new ParCELCorrectnessComparator());

		Iterator<ParCELExtraNode> partialDefinitionIterator = partialDefinitions.iterator();
		while ((positiveExamplesTmp.size() > uncoveredPositiveExamples)
				&& (partialDefinitionIterator.hasNext())) {
			ParCELExtraNode node = partialDefinitionIterator.next();

			int positiveExamplesRemoved = positiveExamplesTmp.size();
			positiveExamplesTmp.removeAll(node.getCoveredPositiveExamples());

			positiveExamplesRemoved -= positiveExamplesTmp.size();

			if (positiveExamplesRemoved > 0) {
				node.setCorrectness(positiveExamplesRemoved);
				minimisedPartialDefinition.add(node);
			}
		}

		return minimisedPartialDefinition;
	}

}
