/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.algorithms.el;

import java.util.Comparator;
import java.util.Iterator;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;

/**
 * Compares two EL description trees. It is a lexicographic order
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
				// navigate through both labels
				Iterator<NamedClass> it1 = node1.getLabel().descendingIterator();
				Iterator<NamedClass> it2 = node2.getLabel().descendingIterator();
				while(it1.hasNext()) {
					NamedClass nc1 = it1.next();
					NamedClass nc2 = it2.next();
					int compare = nc1.getName().compareTo(nc2.getName());
					if(compare != 0)
						return compare;
				}
				
				// recursively compare all edges
				for(int i=0; i<nrOfChildren1; i++) {
					// compare by edge name
					ObjectProperty op1 = node1.getEdges().get(i).getLabel();
					ObjectProperty op2 = node2.getEdges().get(i).getLabel();
					int compare = op1.getName().compareTo(op2.getName());
					if(compare != 0)
						return compare;
					
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
