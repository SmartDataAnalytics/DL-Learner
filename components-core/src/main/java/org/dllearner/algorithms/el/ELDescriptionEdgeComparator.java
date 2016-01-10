package org.dllearner.algorithms.el;

import java.util.Comparator;

/**
 * @author Jens Lehmann
 *
 */
public class ELDescriptionEdgeComparator implements Comparator<ELDescriptionEdge> {

	private ELDescriptionNodeComparator nodeComp;
	
	public ELDescriptionEdgeComparator() {
		nodeComp = new ELDescriptionNodeComparator();
	}	
	
	@Override
	public int compare(ELDescriptionEdge edge1, ELDescriptionEdge edge2) {
		// perform string comparison on node labels
		int comp = edge1.getLabel().compareTo(edge2.getLabel());
		if(comp==0) {
			return nodeComp.compare(edge1.getNode(), edge2.getNode());
		} else {
			return comp;
		}
	}

}
