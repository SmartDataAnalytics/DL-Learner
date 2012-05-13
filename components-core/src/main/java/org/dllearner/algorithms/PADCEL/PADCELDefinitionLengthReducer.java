package org.dllearner.algorithms.PADCEL;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.Individual;

/**
 * Compact set of partial definitions using Definition Length Grredy Reduction strategy
 * 
 * @author An C. Tran
 * 
 */
public class PADCELDefinitionLengthReducer implements PADCELReducer {

	@Override
	public SortedSet<PADCELExtraNode> compact(SortedSet<PADCELExtraNode> partialDefinitions,
			Set<Individual> positiveExamples) {
		return compact(partialDefinitions, positiveExamples, 0);
	}

	@Override
	public SortedSet<PADCELExtraNode> compact(SortedSet<PADCELExtraNode> partialDefinitions,
			Set<Individual> positiveExamples, int uncoveredPositiveExamples) {
		Set<Individual> positiveExamplesTmp = new HashSet<Individual>();
		positiveExamplesTmp.addAll(positiveExamples);

		TreeSet<PADCELExtraNode> newSortedPartialDefinitions = new TreeSet<PADCELExtraNode>(
				new PADCELDefinitionLengthComparator());
		synchronized (partialDefinitions) {
			newSortedPartialDefinitions.addAll(partialDefinitions);
		}

		TreeSet<PADCELExtraNode> minimisedPartialDefinition = new TreeSet<PADCELExtraNode>(
				new PADCELDefinitionGenerationTimeComparator());

		Iterator<PADCELExtraNode> partialDefinitionIterator = newSortedPartialDefinitions.iterator();
		while ((positiveExamplesTmp.size() > uncoveredPositiveExamples)
				&& (partialDefinitionIterator.hasNext())) {
			PADCELExtraNode node = partialDefinitionIterator.next();

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
