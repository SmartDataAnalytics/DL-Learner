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
	
		double diff = o2.getCoveredNegatives() - o1.getCoveredNegatives();
		
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

}
