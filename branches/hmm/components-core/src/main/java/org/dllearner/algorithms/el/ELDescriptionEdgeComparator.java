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
