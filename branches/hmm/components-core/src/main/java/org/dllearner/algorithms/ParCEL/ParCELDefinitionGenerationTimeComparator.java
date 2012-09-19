package org.dllearner.algorithms.ParCEL;

/**
 * Compare two node based on their generation time. 
 * This will be used in the Generation Time Greedy Compactness strategy 
 * GOLR
 * 
 * @author An C. Tran
 */

import java.util.Comparator;

import org.dllearner.utilities.owl.ConceptComparator;

public class ParCELDefinitionGenerationTimeComparator implements
		Comparator<ParCELExtraNode> {

	@Override
	public int compare(ParCELExtraNode node1, ParCELExtraNode node2) {
		double genTime1 = node1.getGenerationTime();
		double genTime2 = node2.getGenerationTime();

		if (genTime1 < genTime2)
			return -1;
		else if (genTime1 > genTime2)
			return 1;
		else {
			if (node1.getDescription().getLength() < node2.getDescription()
					.getLength())
				return -1;
			else if (node1.getDescription().getLength() > node2
					.getDescription().getLength())
				return 1;
			else
				return new ConceptComparator().compare(node1.getDescription(),
						node2.getDescription());
		}
	}

}
