/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
 *
 */
package org.dllearner.algorithms.celoe;

import java.util.List;

import org.dllearner.core.owl.Description;

/**
 * A node in the search tree of the ontology engineering algorithm.
 * 
 * Differences to the node structures in other algorithms (this may change):
 * - covered examples are not stored in node (i.e. coverage needs to be recomputed
 * for child nodes, which costs time but saves memory)
 * - only evaluated nodes are stored
 * - too weak nodes are not stored
 * - redundant nodes are not stored (?)
 * - only accuracy is stored to make the node structure reusable for different 
 *   learning problems and -algorithms
 * 
 * @author Jens Lehmann
 *
 */
public class OENode {

	private Description description;
	
	private double accuracy;
	
	private OENode parent;
	private List<OENode> children;
	
	public OENode(OENode parentNode, Description description, double accuracy) {
		this.parent = parentNode;
		this.description = description;
		this.accuracy = accuracy;
	}

	public void addChild(OENode node) {
		children.add(node);
	}

	/**
	 * @return the description
	 */
	public Description getDescription() {
		return description;
	}

	/**
	 * @return the accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * @return the parent
	 */
	public OENode getParent() {
		return parent;
	}

	/**
	 * @return the children
	 */
	public List<OENode> getChildren() {
		return children;
	}
	
}
