package org.dllearner.algorithms.parcel;

import com.google.common.collect.ComparisonChain;
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
		return ComparisonChain.start()
				.compare(node1.getCoveredPositiveExamples().size(), node2.getCoveredPositiveExamples().size())
				.compare(OWLClassExpressionUtils.getLength(node1.getDescription()), OWLClassExpressionUtils.getLength(node2.getDescription()))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}

}
