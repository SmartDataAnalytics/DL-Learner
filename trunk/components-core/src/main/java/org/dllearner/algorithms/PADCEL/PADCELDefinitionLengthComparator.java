package org.dllearner.algorithms.PADCEL;

import java.util.Comparator;

import org.dllearner.utilities.owl.ConceptComparator;

/**
 * Compare two node based on their definition length. This will be used in the Definition Length
 * Greedy Compactness strategy
 * 
 * @author An C. Tran
 * 
 */

public class PADCELDefinitionLengthComparator implements Comparator<PADCELExtraNode> {

	@Override
	public int compare(PADCELExtraNode node1, PADCELExtraNode node2) {
		int len1 = node1.getDescription().getLength();
		int len2 = node2.getDescription().getLength();
		if (len1 < len2)
			return -1;
		else if (len1 > len2)
			return 1;
		else
			return new ConceptComparator().compare(node1.getDescription(), node2.getDescription());
	}

}
