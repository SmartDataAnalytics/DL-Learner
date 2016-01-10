package org.dllearner.algorithms.el;

import org.dllearner.core.ComponentInitException;


/**
 * A stable comparator for search tree nodes. Stable means that the order
 * of nodes will not change during the run of the learning algorithm. In
 * this implementation, this is ensured by using only covered examples
 * and tree size as criteria.
 * 
 * @author Jens Lehmann
 *
 */
public class StableHeuristic implements ELHeuristic {

	private ELDescriptionTreeComparator cmp = new ELDescriptionTreeComparator();
	
	@Override
	public int compare(SearchTreeNode o1, SearchTreeNode o2) {
	
		int diff = o2.getCoveredNegatives() - o1.getCoveredNegatives();
		diff = Double.compare(o1.getScore().getAccuracy(), o2.getScore().getAccuracy());
		if(diff>0) {		
			return 1;
		} else if(diff<0) {
			return -1;
		} else {
			
			double sizeDiff = o2.getDescriptionTree().size - o1.getDescriptionTree().size;
			
			if(sizeDiff == 0) {
				return cmp.compare(o1.getDescriptionTree(), o2.getDescriptionTree());
			} else if(sizeDiff>0) {
				return 1;
			} else {
				return -1;
			}
			
		}		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
	}

}
