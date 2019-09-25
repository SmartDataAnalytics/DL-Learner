/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;

@ComponentAnn(name = "DisjunctiveHeuristic", shortName = "disjunctive_heuristic", version = 0.1)
public class DisjunctiveHeuristic implements ELHeuristic {

	ELDescriptionTreeComparator edt = new ELDescriptionTreeComparator();
	
	@Override
	public int compare(SearchTreeNode tree1, SearchTreeNode tree2) {
		double diff = tree1.getScore().getAccuracy()-tree2.getScore().getAccuracy();
		if(diff < 0.00001 && diff > -0.00001) {
			return edt.compare(tree1.getDescriptionTree(), tree2.getDescriptionTree());
		} else if(diff > 0){
			return 1;
//			return (int)Math.signum(diff);
		} else {
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
	}

	@Override
	public double getNodeScore(SearchTreeNode node) {
		return node.getScore().getAccuracy();
	}
}
