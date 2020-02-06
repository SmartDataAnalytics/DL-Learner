package org.dllearner.algorithms.parcel;

import java.util.Comparator;

import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;

/**
 * Used to compare 2 ParCELExtraNode nodes based on their correctness. The description length and
 * ConceptComparator will be used it they have equal coverage
 * 
 * @author An C. Tran
 */
public class ParCELCorrectnessComparator implements Comparator<ParCELExtraNode> {

	@Override
	public int compare(ParCELExtraNode node1, ParCELExtraNode node2) {
		double correctness1 = node1.getCorrectness();
		double correctness2 = node2.getCorrectness();

		if (correctness1 > correctness2)
			return -1; // smaller will be on the top
		else if (correctness1 < correctness2)
			return 1;
		else {
			int len1 = new OWLClassExpressionLengthCalculator().getLength(node1.getDescription());
			int len2 = new OWLClassExpressionLengthCalculator().getLength(node2.getDescription());

			if (len1 < len2)
				return -1;
			else if (len1 > len2)
				return 1;
			else
				return node1.getDescription().compareTo(node2.getDescription());
		}
	}
}
