package org.dllearner.algorithms.ParCEL;

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
		double genTime1 = node1.getGenerationTime();
		double genTime2 = node2.getGenerationTime();

		return ComparisonChain.start()
				.compare(genTime1, genTime2)
				.compare(OWLClassExpressionUtils.getLength(node1.getDescription()), OWLClassExpressionUtils.getLength(node1.getDescription()))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}

}
