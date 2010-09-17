package org.dllearner.sparqlquerygenerator.datastructures.impl;

import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;

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
