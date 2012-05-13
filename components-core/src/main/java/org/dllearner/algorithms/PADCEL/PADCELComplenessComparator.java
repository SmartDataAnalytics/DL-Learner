package org.dllearner.algorithms.PADCEL;

import java.util.Comparator;

import org.dllearner.utilities.owl.ConceptComparator;

/**
 * Comparator for PDLLNode based on the completeness of the description If two
 * nodes has the same completeness, description length will be taken into the
 * consideration and finally the ConceptComparator
 * 
 * @author An C. Tran
 * 
 */

public class PADCELComplenessComparator implements Comparator<PADCELNode> {

	@Override
	public int compare(PADCELNode node1, PADCELNode node2) {
		int v1 = node1.getCoveredPositiveExamples().size();
		int v2 = node2.getCoveredPositiveExamples().size();
		if (v1 > v2)
			return -1;
		else if (v1 < v2)
			return 1;
		else {
			if (node1.getDescription().getLength() < node2.getDescription().getLength())
				return -1;
			else if (node1.getDescription().getLength() > node2.getDescription().getLength())
				return 1;
			else
				return new ConceptComparator().compare(node1.getDescription(),
						node2.getDescription());
		}
	}

}
