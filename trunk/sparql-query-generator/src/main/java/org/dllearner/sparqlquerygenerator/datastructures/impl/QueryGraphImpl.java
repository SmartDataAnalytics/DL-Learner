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


import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;
import org.dllearner.sparqlquerygenerator.datastructures.QueryGraph;
import org.dllearner.sparqlquerygenerator.impl.QueryGraphFactoryImpl;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
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
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		QueryGraph graph = new QueryGraphFactoryImpl().getQueryGraph();
		Node rootNodeCopy= graph.createNode(rootNode.getLabel());
		graph.setRootNode(rootNodeCopy);
		Node sourceNode;
		Node targetNode;
		for(Edge edge : edges){
			sourceNode = graph.createNode(edge.getSourceNode().getLabel());
			targetNode = graph.createNode(edge.getTargetNode().getLabel());
			graph.createEdge(sourceNode, targetNode, edge.getLabel());
		}
		return graph;
	}
	

}
