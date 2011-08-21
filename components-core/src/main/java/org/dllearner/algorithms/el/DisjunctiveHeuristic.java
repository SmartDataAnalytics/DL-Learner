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
