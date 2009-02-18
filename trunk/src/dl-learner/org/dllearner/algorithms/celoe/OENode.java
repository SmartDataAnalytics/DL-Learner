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

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.dllearner.algorithms.refinement2.ExampleBasedNode;
import org.dllearner.algorithms.refinement2.MultiHeuristic;
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
	
	private int horizontalExpansion;
	
	private OENode parent;
	private List<OENode> children = new LinkedList<OENode>();
	
	DecimalFormat dfPercent = new DecimalFormat("0.00%");
	
	public OENode(OENode parentNode, Description description, double accuracy) {
		this.parent = parentNode;
		this.description = description;
		this.accuracy = accuracy;
		horizontalExpansion = description.getLength()-1;
	}

	public void addChild(OENode node) {
		children.add(node);
	}

	public void incHorizontalExpansion() {
		horizontalExpansion++;
	}
	
	public boolean isRoot() {
		return (parent == null);
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

	/**
	 * @return the horizontalExpansion
	 */
	public int getHorizontalExpansion() {
		return horizontalExpansion;
	}
	
	public String getShortDescription(String baseURI) {
		String ret = description.toString(baseURI,null) + " [";
		ret += "acc:" + dfPercent.format(accuracy) + ", ";
		ret += "he:" + horizontalExpansion + ", ";
		ret += "c:" + children.size() + "]";
		return ret;
	}	
	
	public String toTreeString() {
		return toTreeString(0, null).toString();
	}
	
	public String toTreeString(String baseURI) {
		return toTreeString(0, baseURI).toString();
	}	
	
	private StringBuilder toTreeString(int depth, String baseURI) {
		StringBuilder treeString = new StringBuilder();
		for(int i=0; i<depth-1; i++)
			treeString.append("  ");
		if(depth!=0)
			treeString.append("|--> ");
		treeString.append(getShortDescription(baseURI)+"\n");
		for(OENode child : children) {
			treeString.append(child.toTreeString(depth+1,baseURI));
		}
		return treeString;
	}	
}
