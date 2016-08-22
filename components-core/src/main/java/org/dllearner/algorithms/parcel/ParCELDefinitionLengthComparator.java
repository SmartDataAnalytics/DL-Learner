package org.dllearner.algorithms.parcel;

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
		return ComparisonChain.start()
				.compare(OWLClassExpressionUtils.getLength(node1.getDescription()), OWLClassExpressionUtils.getLength(node2.getDescription()))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}

}
