package org.dllearner.algorithms.el;

import org.dllearner.core.ComponentInitException;

public class DisjunctiveHeuristic implements ELHeuristic {

	ELDescriptionTreeComparator edt = new ELDescriptionTreeComparator();
	
	public int compare(SearchTreeNode tree1, SearchTreeNode tree2) {
		double diff = tree1.getScore().getAccuracy()-tree2.getScore().getAccuracy();
		if(diff < 0.00001 && diff > -0.00001) {
			return edt.compare(tree1.getDescriptionTree(), tree2.getDescriptionTree());
		} else if(diff > 0){
			return 1;
//			return (int)Math.signum(diff);
		} else {
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
	}

}
