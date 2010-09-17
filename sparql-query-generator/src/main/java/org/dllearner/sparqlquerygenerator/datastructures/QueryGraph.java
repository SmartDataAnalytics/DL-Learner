package org.dllearner.sparqlquerygenerator.datastructures;

import java.util.Set;

public interface QueryGraph {
	
	Set<Node> getNodes();
	
	Set<Edge> getEdges();
	
	Node getRootNode();
	
	Node createNode(String id);
	
	Edge createEdge(Node sourceNode, Node targetNode, String label);
	
	boolean addNode(Node node);
	
	boolean addEdge(Edge edge);
	
	void setRootNode(Node rootNode);

}
