package org.dllearner.algorithms.parcel;

import java.util.Comparator;

import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;

/**
 * Use to compare 2 ParCELExtraNode nodes based on the number of covered negative examples. The
 * description length and ConceptComparator will be used it they have equal coverage
 * 
 * @author An C. Tran
 * 
 */
public class ParCELCoveredNegativeExampleComparator implements Comparator<ParCELExtraNode> {

	@Override
	public int compare(ParCELExtraNode node1, ParCELExtraNode node2) {
		int coveredNeg1 = node1.getCoveredNegativeExamples().size();
		int coveredNeg2 = node2.getCoveredPositiveExamples().size();

		if (coveredNeg1 > coveredNeg2)
			return -1; // smaller will be on the top
		else if (coveredNeg1 < coveredNeg2)
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
