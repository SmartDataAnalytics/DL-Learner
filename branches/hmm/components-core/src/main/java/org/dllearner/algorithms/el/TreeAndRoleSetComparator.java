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
import java.util.Set;

import org.dllearner.core.owl.ObjectProperty;

/**
 * A comparator implementation for the tree and role set convenience structure.
 * 
 * @author Jens Lehmann
 *
 */
public class TreeAndRoleSetComparator implements Comparator<TreeAndRoleSet> {

	private ELDescriptionTreeComparator treeComp = new ELDescriptionTreeComparator();
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(TreeAndRoleSet o1, TreeAndRoleSet o2) {
		int comp = treeComp.compare(o1.getTree(), o2.getTree());
		if(comp == 0) {
			Set<ObjectProperty> op1 = o1.getRoles();
			Set<ObjectProperty> op2 = o2.getRoles();
			int sizeDiff = op1.size() - op2.size();
			if(sizeDiff == 0) {
				Iterator<ObjectProperty> it1 = op1.iterator();
				Iterator<ObjectProperty> it2 = op2.iterator();
				while(it1.hasNext()) {
					int stringComp = it1.next().compareTo(it2.next());
					if(stringComp != 0) {
						return stringComp;
					}
				}
				return 0;
			} else {
				return sizeDiff;
			}
		} else {
			return comp;
		}
	}

}
