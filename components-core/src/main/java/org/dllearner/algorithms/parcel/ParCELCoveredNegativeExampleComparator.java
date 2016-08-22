package org.dllearner.algorithms.parcel;

import com.google.common.collect.ComparisonChain;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;

import java.util.Comparator;

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
		return ComparisonChain.start()
				.compare(node2.getCoveredNegativeExamples().size(), node1.getCoveredNegativeExamples().size())// smaller will be on the top
				.compare(OWLClassExpressionUtils.getLength(node1.getDescription()), OWLClassExpressionUtils.getLength(node2.getDescription()))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}
}
