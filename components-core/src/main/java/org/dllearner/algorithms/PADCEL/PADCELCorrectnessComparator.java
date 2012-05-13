package org.dllearner.algorithms.PADCEL;

import java.util.Comparator;

import org.dllearner.utilities.owl.ConceptComparator;

/**
 * Use to compare 2 ExtraPLOENode nodes based on their correctness. The description length and
 * ConceptComparator will be used it they have equal coverage
 * 
 * @author An C. Tran
 * 
 */
public class PADCELCorrectnessComparator implements Comparator<PADCELExtraNode> {

	@Override
	public int compare(PADCELExtraNode node1, PADCELExtraNode node2) {
		double correctness1 = node1.getCorrectness();
		double correctness2 = node2.getCorrectness();

		if (correctness1 > correctness2)
			return -1; // smaller will be on the top
		else if (correctness1 < correctness2)
			return 1;
		else {
			int len1 = node1.getDescription().getLength();
			int len2 = node2.getDescription().getLength();

			if (len1 < len2)
				return -1;
			else if (len1 > len2)
				return 1;
			else
				return new ConceptComparator().compare(node1.getDescription(),
						node2.getDescription());
		}
	}
}
