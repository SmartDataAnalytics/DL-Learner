package org.dllearner.sparqlquerygenerator.datastructures.impl;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;
import org.dllearner.sparqlquerygenerator.datastructures.QueryGraph;

public class QueryGraphImpl implements QueryGraph {
	
	private Set<Node> nodes;
	private Set<Edge> edges;
	private Node rootNode;
	
	private Map<String, Node> id2NodeMap;
	
	public QueryGraphImpl(){
		nodes = new HashSet<Node>();
		edges = new HashSet<Edge>();
		id2NodeMap = new Hashtable<String, Node>();
	}
	
	public QueryGraphImpl(Node rootNode){
		this.rootNode = rootNode;
		
		nodes = new HashSet<Node>();
		edges = new HashSet<Edge>();
		id2NodeMap = new Hashtable<String, Node>();
		
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

	@Override
	public Node createNode(String id) {
		Node node = id2NodeMap.get(id);
		if(node == null){
			node = new NodeImpl(id);
			id2NodeMap.put(id, node);
			nodes.add(node);
		}
		return node;
	}

	@Override
	public Edge createEdge(Node sourceNode, Node targetNode, String label) {
		Edge edge = new EdgeImpl(sourceNode, targetNode, label);
		edges.add(edge);
		sourceNode.addOutEdge(edge);
		targetNode.addInEdge(edge);
		
		return edge;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Edge edge : edges){
			sb.append(edge);
			sb.append("\n");
		}
		return sb.toString();
	}

}
