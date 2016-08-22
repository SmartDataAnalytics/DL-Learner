package org.dllearner.algorithms.parcel;

import com.google.common.collect.ComparisonChain;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;

import java.util.Comparator;

/**
 * Use to compare 2 ParCELExtraNode nodes based on their correctness. The description length and
 * ConceptComparator will be used it they have equal coverage
 * 
 * @author An C. Tran
 * 
 */
public class ParCELCorrectnessComparator implements Comparator<ParCELExtraNode> {

	@Override
	public int compare(ParCELExtraNode node1, ParCELExtraNode node2) {
		return ComparisonChain.start()
				.compare(node1.getCorrectness(), node2.getCorrectness())
				.compare(OWLClassExpressionUtils.getLength(node1.getDescription()), OWLClassExpressionUtils.getLength(node2.getDescription()))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}
}
