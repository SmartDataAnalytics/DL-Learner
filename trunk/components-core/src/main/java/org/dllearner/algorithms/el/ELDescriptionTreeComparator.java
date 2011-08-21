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

/**
 * Compares two EL description trees by calling {@link ELDescriptionNodeComparator} 
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
