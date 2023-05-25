package org.dllearner.algorithms.parcel.reducer;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.ParCELCompletenessComparator;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * This class implements "wise" coverage greedy strategy for compacting the partial definitions In
 * this strategy, the partial definitions will be chosen based on their coverage. When a partial
 * definition has been chosen, coverage of other partial definition will be recalculated
 * 
 * @author An C. Tran
 * 
 */

public class ParCELCorrectnessGreedyReducer {

	Logger logger = Logger.getLogger(this.getClass());


	/**
	 * Compact partial definition with noise allowed
	 * 
	 * @param counterPartialDefinitions
	 *            Set of partial definitions
	 * @param negativeExamples
	 *            Set of positive examples (used to check whether partial definition is useful
	 *
	 * @return Subset of partial definitions that cover (positive examples \ uncovered positive
	 *         examples)
	 */
	public SortedSet<ParCELExtraNode> reduce(SortedSet<ParCELExtraNode> counterPartialDefinitions,
											 Set<OWLIndividual> negativeExamples)
	{

		Set<OWLIndividual> positiveExamplesTmp = new HashSet<>(negativeExamples);

		TreeSet<ParCELExtraNode> reducedPartialDefinition = new TreeSet<>(
                new ParCELCompletenessComparator());

		if (counterPartialDefinitions.size() == 0)
			return reducedPartialDefinition;

		synchronized (counterPartialDefinitions) {
			Object[] partialDefs = counterPartialDefinitions.toArray();

			// the highest accurate partial definition
			// reducedPartialDefinition.add((PDLLExtraNode)partialDefs[0]);
			// positiveExamplesTmp.removeAll(((PDLLExtraNode)partialDefs[0]).getCoveredPositiveExamples());

			for (int i = 0; (positiveExamplesTmp.size() > 0)
					&& (i < counterPartialDefinitions.size()); i++) {

				// count the number of different positive examples covered
				int counti = 0;
				for (OWLIndividual indi : ((ParCELExtraNode) partialDefs[i]).getCoveredPositiveExamples()) {
					if (positiveExamplesTmp.contains(indi))
						counti++;
				} // count the number of different covered positive examples by i

				
				for (int j = i + 1; j < counterPartialDefinitions.size(); j++) {
					int countj = 0;

					for (OWLIndividual indj : ((ParCELExtraNode) partialDefs[j]).getCoveredPositiveExamples())
						if (positiveExamplesTmp.contains(indj))
							countj++;

					// swap the partial definition so that the "best" partial
					// definition will be in the top
					if (countj > counti) {
						ParCELExtraNode tmp = (ParCELExtraNode) partialDefs[j];
						partialDefs[j] = partialDefs[i];
						partialDefs[i] = tmp;
						counti = countj;
					}
				}

				reducedPartialDefinition.add((ParCELExtraNode) partialDefs[i]);
				positiveExamplesTmp.removeAll(((ParCELExtraNode) partialDefs[i])
						.getCoveredPositiveExamples());
			}
		}

		return reducedPartialDefinition;
	}

}
