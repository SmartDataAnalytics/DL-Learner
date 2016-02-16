package org.dllearner.algorithms.ParCEL;

import com.google.common.collect.ComparisonChain;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;

import java.util.Comparator;

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
		int len1 = OWLClassExpressionUtils.getLength(node1.getDescription());
		int len2 = OWLClassExpressionUtils.getLength(node2.getDescription());

		return ComparisonChain.start()
				.compare(len1, len2)
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}

}
