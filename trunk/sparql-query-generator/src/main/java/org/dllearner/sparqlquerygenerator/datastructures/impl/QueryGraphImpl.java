package org.dllearner.sparqlquerygenerator.datastructures.impl;

import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;
import org.dllearner.sparqlquerygenerator.datastructures.QueryGraph;

public class QueryGraphImpl implements QueryGraph {
	
	private Set<Node> nodes;
	private Set<Edge> edges;
	private Node rootNode;
	
	public QueryGraphImpl(){
		
	}
	
	public QueryGraphImpl(Node rootNode){
		this.rootNode = rootNode;
	}
	
	public QueryGraphImpl(Set<Node> nodes, Set<Edge> edges, Node rootNode){
		this.nodes = nodes;
		this.edges = edges;
		this.rootNode = rootNode;
	}

	@Override
	public Set<Node> getNodes() {
		return nodes;
	}

	@Override
	public Set<Edge> getEdges() {
		return edges;
	}
	
	@Override
	public Node getRootNode() {
		return rootNode;
	}

	@Override
	public boolean addNode(Node node) {
		return nodes.add(node);
	}

	@Override
	public boolean addEdge(Edge edge) {
		return edges.add(edge);
	}

	@Override
	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}

}
