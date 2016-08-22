package org.dllearner.algorithms.parcel;

/**
 * Compare two node based on their generation time. 
 * This will be used in the Generation Time Greedy Compactness strategy 
 * GOLR
 * 
 * @author An C. Tran
 */

import com.google.common.collect.ComparisonChain;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;

import java.util.Comparator;

public class ParCELDefinitionGenerationTimeComparator implements
		Comparator<ParCELExtraNode> {

	@Override
	public int compare(ParCELExtraNode node1, ParCELExtraNode node2) {
		return ComparisonChain.start()
				.compare(node1.getGenerationTime(), node2.getGenerationTime())
				.compare(OWLClassExpressionUtils.getLength(node1.getDescription()), OWLClassExpressionUtils.getLength(node2.getDescription()))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}

}
