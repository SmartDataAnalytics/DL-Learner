package org.dllearner.cli.parcel.modeling;

import java.util.Comparator;


/**
 * Comparator for EValInfor objects. This will be used to sort 
 * the EValInfor of a description based on the order of fold
 * 
 * @author An C. Tran
 *
 */
public class FoldInforComparator implements Comparator<FoldInfor> {

	@Override
	public int compare(FoldInfor o1, FoldInfor o2) {			
		if (o1.getFold() < o2.getFold())
			return -1;
		else if (o1.getFold() > o2.getFold())
			return 1;
		else
			return 0;
	}
	
}