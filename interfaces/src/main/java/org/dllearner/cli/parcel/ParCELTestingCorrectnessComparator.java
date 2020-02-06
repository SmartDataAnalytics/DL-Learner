package org.dllearner.cli.parcel;

import java.util.Comparator;


/**
 * This is a comparator for ParCELExtraTestinhNode based on the correctness
 * It is mainly used in fortified strategy to select the counter partial definition
 * to form fortified partial definitions. 
 * 
 * @author An C. Tran
 *
 */
public class ParCELTestingCorrectnessComparator implements Comparator<ParCELExtraTestingNode> {

	
	/**
	 * Note: this is a simple version of the comparator. The total number of positive and negative examples
	 * should be taken into consideration.
	 * 
	 * Currently, the counter partial definitions that cover no positive examples will be placed on the top
	 * based on their coverage of negative examples.
	 */
	@Override
	public int compare(ParCELExtraTestingNode node1, ParCELExtraTestingNode node2) {
		
		int cp1 = node1.getCoveredPositiveExamplesTestSet().size();
		int cn1 = node1.getCoveredNegativeExamplestestSet().size();
		int cp2 = node2.getCoveredPositiveExamplesTestSet().size();
		int cn2 = node2.getCoveredNegativeExamplestestSet().size();
		
		if (cp1 == 0) {
			if (cp2 != 0)
				return -1;
			else 
				return compareInt(cn1, cn2);
		}
		else {
			if (cp2 == 0)
				return 1;
			else 
				return compareInt(cn1, cn2);			
		}
		
	}
	
	
	private int compareInt(int v1, int v2) {
		if (v1 < v2)
			return -1;
		else if (v1 > v2)
			return 1;
		else
			return 0;
			
	}
	
}
