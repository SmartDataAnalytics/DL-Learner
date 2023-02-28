package org.dllearner.algorithms.parcel;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;

/**
 * Compare two node based on their definition length. This will be used in the Definition Length
 * Greedy Compactness strategy
 * 
 * @author An C. Tran
 * 
 */

public class ParCELDefinitionLengthComparator implements Comparator<ParCELExtraNode> {

	@Override
	public int compare(ParCELExtraNode node1, ParCELExtraNode node2) {
		ComparisonChain.start().compare(OWLClassExpressionUtils.getLength(node1.getDescription()),
										OWLClassExpressionUtils.getLength(node2.getDescription()))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
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
