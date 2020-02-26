package org.dllearner.algorithms.parcel.reducer;

import java.util.*;

import org.dllearner.algorithms.parcel.ParCELDefinitionGenerationTimeComparator;
import org.dllearner.algorithms.parcel.ParCELDefinitionLengthComparator;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Compact set of partial definitions using Definition Length Greedy Reduction strategy
 * 
 * @author An C. Tran
 * 
 */
public class ParCELDefinitionLengthReducer implements ParCELReducer {

	@Override
	public SortedSet<ParCELExtraNode> reduce(SortedSet<ParCELExtraNode> partialDefinitions,
											 Set<OWLIndividual> positiveExamples) {
		return reduce(partialDefinitions, positiveExamples, 0);
	}

	@Override
	public SortedSet<ParCELExtraNode> reduce(SortedSet<ParCELExtraNode> partialDefinitions,
                                             Set<OWLIndividual> positiveExamples, int uncoveredPositiveExamples) {
		Set<OWLIndividual> positiveExamplesTmp = new HashSet<>(positiveExamples);

		TreeSet<ParCELExtraNode> newSortedPartialDefinitions = new TreeSet<>(
                new ParCELDefinitionLengthComparator());
		synchronized (partialDefinitions) {
			newSortedPartialDefinitions.addAll(partialDefinitions);
		}

		TreeSet<ParCELExtraNode> minimisedPartialDefinition = new TreeSet<>(
                new ParCELDefinitionGenerationTimeComparator());

		Iterator<ParCELExtraNode> partialDefinitionIterator = newSortedPartialDefinitions.iterator();
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
