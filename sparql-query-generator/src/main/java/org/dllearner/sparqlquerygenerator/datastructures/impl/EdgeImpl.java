/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
package org.dllearner.sparqlquerygenerator.datastructures.impl;

import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class EdgeImpl implements Edge {
	
	private Node sourceNode;
	private Node targetNode;
	
	private String label;
	
	public EdgeImpl(Node sourceNode, Node targetNode, String label){
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.label = label;
	}
	
	public EdgeImpl(Node targetNode, String label){
		this.targetNode = targetNode;
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Node getSourceNode() {
		return sourceNode;
	}

	@Override
	public Node getTargetNode() {
		return targetNode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(!(obj instanceof EdgeImpl))
			return false;
		EdgeImpl other = (EdgeImpl)obj;
		return other.getLabel().equals(label) && other.getSourceNode().equals(sourceNode) && other.targetNode.equals(targetNode);
	}
	
	@Override
	public int hashCode() {
		return sourceNode.hashCode() + targetNode.hashCode() + label.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Source node: ").append(sourceNode).append("\n");
		sb.append("Target node: ").append(targetNode).append("\n");
		sb.append("Label: ").append(label).append("\n");
		return sb.toString();
	}


}
