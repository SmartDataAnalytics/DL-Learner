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
 * A tuple of two EL description trees.
 * 
 * @author Jens Lehmann
 *
 */
public class TreeTuple {

	private ELDescriptionNode tree1;
	
	private ELDescriptionNode tree2;
	
	public TreeTuple(ELDescriptionNode tree1, ELDescriptionNode tree2) {
		this.tree1 = tree1;
		this.tree2 = tree2;
	}

	/**
	 * Gets first tree.
	 * @return - first tree
	 */
	public ELDescriptionNode getTree1() {
		return tree1;
	}

	/**
	 * Gets second tree.
	 * @return - second tree
	 */
	public ELDescriptionNode getTree2() {
		return tree2;
	}
	
}
