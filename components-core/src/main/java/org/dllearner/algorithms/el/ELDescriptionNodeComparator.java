package org.dllearner.algorithms.el;

import java.util.Comparator;
import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

/**
 * Compares two EL OWLClassExpression trees. It is a lexicographic order
 * according to the following criteria:
 * - number of children
 * - size of label
 * - string comparison for each class in the label
 * - recursive call on each child (first compare edge label, then child node)
 * 
 * @author Jens Lehmann
 *
 */
public class ELDescriptionNodeComparator implements Comparator<ELDescriptionNode> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ELDescriptionNode node1, ELDescriptionNode node2) {
		int nrOfChildren1 = node1.getEdges().size();
		int nrOfChildren2 = node2.getEdges().size();
		if(nrOfChildren1 > nrOfChildren2) {
			return 1;
		} else if(nrOfChildren1 < nrOfChildren2) {
			return -1;
		} else {
			int labelSize1 = node1.getLabel().size();
			int labelSize2 = node2.getLabel().size();
			if(labelSize1 > labelSize2) {
				return 1;
			} else if(labelSize1 < labelSize2) {
				return -1;
			} else {
				int compare = 0;
				if(node1.isClassNode() && node2.isClassNode()){
					// navigate through both labels
					Iterator<OWLClass> it1 = node1.getLabel().descendingIterator();
					Iterator<OWLClass> it2 = node2.getLabel().descendingIterator();
					while(it1.hasNext()) {
						OWLClass nc1 = it1.next();
						OWLClass nc2 = it2.next();
						compare = nc1.toStringID().compareTo(nc2.toStringID());
						
					}
				} else if(!node1.isClassNode() && !node2.isClassNode()){
					compare = node1.getDataRange().toString().compareTo(node2.getDataRange().toString());
				} else {
					compare = -1;
				}
				
				if(compare != 0)
					return compare;
				
				// recursively compare all edges
				for(int i=0; i<nrOfChildren1; i++) {
					// compare by edge name
					OWLProperty op1 = node1.getEdges().get(i).getLabel();
					OWLProperty op2 = node2.getEdges().get(i).getLabel();
					int compare1 = op1.toStringID().compareTo(op2.toStringID());
					if(compare1 != 0)
						return compare1;
					
					// compare child nodes
					ELDescriptionNode child1 = node1.getEdges().get(i).getNode();
					ELDescriptionNode child2 = node2.getEdges().get(i).getNode();
					int compare2 = compare(child1, child2);
					if(compare2 != 0)
						return compare2;
				}
				
				// trees are identical
				return 0;
			}
		}		
	}

}
