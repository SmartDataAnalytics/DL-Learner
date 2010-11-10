package org.dllearner.algorithms.el;

public class DisjunctiveHeuristic implements ELHeuristic {

	ELDescriptionTreeComparator edt = new ELDescriptionTreeComparator();
	
	public int compare(SearchTreeNode tree1, SearchTreeNode tree2) {
		double diff = tree1.getScore()-tree2.getScore();
		if(diff < 0.00001 && diff > -0.00001) {
			return edt.compare(tree1.getDescriptionTree(), tree2.getDescriptionTree());
		} else if(diff > 0){
			return 1;
//			return (int)Math.signum(diff);
		} else {
			return -1;
		}
	}

}
