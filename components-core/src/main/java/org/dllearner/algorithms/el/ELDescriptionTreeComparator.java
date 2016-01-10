package org.dllearner.algorithms.el;

import java.util.Comparator;

/**
 * Compares two EL OWLClassExpression trees by calling {@link ELDescriptionNodeComparator} 
 * on their root nodes.
 * 
 * @author Jens Lehmann
 *
 */
public class ELDescriptionTreeComparator implements Comparator<ELDescriptionTree> {

	private ELDescriptionNodeComparator nodeComp;
	
	public ELDescriptionTreeComparator() {
		nodeComp = new ELDescriptionNodeComparator();
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ELDescriptionTree tree1, ELDescriptionTree tree2) {
		// we use the size as first criterion to avoid many comparisons
		int sizeDiff = tree1.size - tree2.size;
		if(sizeDiff == 0) {
			ELDescriptionNode node1 = tree1.getRootNode();
			ELDescriptionNode node2 = tree2.getRootNode();
			return nodeComp.compare(node1, node2);			
		} else { 
			return sizeDiff;
		}
	}

}
