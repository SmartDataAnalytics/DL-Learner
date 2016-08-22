package org.dllearner.algorithms.parcel;

import com.google.common.collect.ComparisonChain;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;

import java.util.Comparator;

/**
 * Use to compare 2 ParCELExtraNode nodes based on their completeness (coverage). The description
 * length and ConceptComparator will be used it they have equal coverage
 * 
 * @author An C. Tran
 * 
 */
public class ParCELCompletenessComparator implements Comparator<ParCELExtraNode> {

	@Override
	public int compare(ParCELExtraNode node1, ParCELExtraNode node2) {
		return ComparisonChain.start()
				.compare(node1.getCoveredPositiveExamples().size(), node2.getCoveredPositiveExamples().size())
				.compare(OWLClassExpressionUtils.getLength(node1.getDescription()), OWLClassExpressionUtils.getLength(node2.getDescription()))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}
}
