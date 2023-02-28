package org.dllearner.algorithms.parcel;

import java.util.Comparator;

import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;

/**
 * Use to compare 2 ParCELExtraNode nodes based on their completeness (coverage). The description
 * length and ConceptComparator will be used it they have equal coverage
 * 
 * @author An C. Tran
 * 
 */
public class ParCELCompletenessComparator implements Comparator<ParCELNode> {

	@Override
	public int compare(ParCELNode node1, ParCELNode node2) {

		int v1 = node1.getCoveredPositiveExamples().size();
		int v2 = node2.getCoveredPositiveExamples().size();

		if (v1 > v2)
			return -1;
		else if (v1 < v2)
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
