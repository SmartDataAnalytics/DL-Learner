package org.dllearner.algorithms.ParCEL;

import org.dllearner.utilities.owl.OWLClassExpressionUtils;

import java.util.Comparator;

/**
 * Comparator for ParCELNode based on the completeness of the description If two
 * nodes has the same completeness, description length will be taken into the
 * consideration and finally the ConceptComparator
 * 
 * @author An C. Tran
 * 
 */

public class ParCELComplenessComparator implements Comparator<ParCELNode> {

	@Override
	public int compare(ParCELNode node1, ParCELNode node2) {
		int v1 = node1.getCoveredPositiveExamples().size();
		int v2 = node2.getCoveredPositiveExamples().size();
		if (v1 > v2)
			return -1;
		else if (v1 < v2)
			return 1;
		else {
			int len1 = OWLClassExpressionUtils.getLength(node1.getDescription());
			int len2 = OWLClassExpressionUtils.getLength(node2.getDescription());

			if (len1 < len2)
				return -1;
			else if (len1 > len2)
				return 1;
			else
				return node1.getDescription().compareTo(node2.getDescription());
		}
	}

}
