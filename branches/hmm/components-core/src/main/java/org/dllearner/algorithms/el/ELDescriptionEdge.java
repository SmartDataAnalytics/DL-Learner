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

import org.dllearner.core.owl.ObjectProperty;

/**
 * A (directed) edge in an EL description tree. It consists of an edge
 * label, which is an object property, and the EL description tree
 * the edge points to.
 * 
 * @author Jens Lehmann
 *
 */
public class ELDescriptionEdge {

	private ObjectProperty label;
	
	private ELDescriptionNode node;

	/**
	 * Constructs and edge given a label and an EL description tree.
	 * @param label The label of this edge.
	 * @param tree The tree the edge points to (edges are directed).
	 */
	public ELDescriptionEdge(ObjectProperty label, ELDescriptionNode tree) {
		this.label = label;
		this.node = tree;
	}
	
	/**
	 * @param label the label to set
	 */
	public void setLabel(ObjectProperty label) {
		this.label = label;
	}

	/**
	 * @return The label of this edge.
	 */
	public ObjectProperty getLabel() {
		return label;
	}

	/**
	 * @return The EL description tree 
	 */
	public ELDescriptionNode getNode() {
		return node;
	}
	
	@Override
	public String toString() {
		return "--" + label + "--> " + node.toDescriptionString(); 
	}
	
}
