package org.dllearner.algorithms.ParCEL;

import java.util.Comparator;

import org.dllearner.utilities.owl.ConceptComparator;

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
