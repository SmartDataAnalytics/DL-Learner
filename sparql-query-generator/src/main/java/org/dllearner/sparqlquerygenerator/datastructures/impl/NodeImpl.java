package org.dllearner.sparqlquerygenerator.datastructures.impl;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;

public class NodeImpl implements Node {
	
	private Set<Edge> inEdges;
	private Set<Edge> outEdges;
	private String label;
	
	public NodeImpl(){
		inEdges = new HashSet<Edge>();
		outEdges = new HashSet<Edge>();
	}
	
	public NodeImpl(String label){
		this.label = label;
		
		inEdges = new HashSet<Edge>();
		outEdges = new HashSet<Edge>();
	}
	
	public NodeImpl(String label, Set<Edge> outEdges){
		this.label = label;
		this.outEdges = outEdges;
		
		inEdges = new HashSet<Edge>();
	}

	public NodeImpl(String label, Set<Edge> outEdges, Set<Edge> inEdges){
		this.label = label;
		this.outEdges = outEdges;
		this.inEdges = inEdges;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Set<Edge> getInEdges() {
		return inEdges;
	}

	@Override
	public Set<Edge> getOutEdges() {
		return outEdges;
	}

	@Override
	public boolean addInEdge(Edge edge) {
		return inEdges.add(edge);
	}

	@Override
	public boolean addOutEdge(Edge edge) {
		return outEdges.add(edge);
	}

}
