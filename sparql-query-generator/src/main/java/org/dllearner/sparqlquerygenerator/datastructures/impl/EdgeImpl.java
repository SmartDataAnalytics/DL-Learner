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


}
