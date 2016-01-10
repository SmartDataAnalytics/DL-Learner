package org.dllearner.utilities.owl;

import java.util.Comparator;

import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;

/**
 * Comparator for evaluated descriptions, which orders them by
 * accuracy as first criterion, length as second criterion, and
 * syntactic structure as third criterion.
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescriptionPosNegComparator implements Comparator<EvaluatedDescriptionPosNeg> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(EvaluatedDescriptionPosNeg ed1, EvaluatedDescriptionPosNeg ed2) {
		double acc1 = ed1.getAccuracy();
		double acc2 = ed2.getAccuracy();
		if(acc1 > acc2)
			return 1;
		else if(acc1 < acc2)
			return -1;
		else {
			int length1 = ed1.getDescriptionLength();
			int length2 = ed2.getDescriptionLength();
			if(length1 < length2)
				return 1;
			else if(length1 > length2)
				return -1;
			else
				return ed1.getDescription().compareTo(ed2.getDescription());
		}
	}

}
