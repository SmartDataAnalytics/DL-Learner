package org.dllearner.sparqlquerygenerator.datastructures;

import java.util.Set;

public interface QueryGraph {
	
	Set<Node> getNodes();
	
	Set<Edge> getEdges();
	
	Node getRootNode();
	
	boolean addNode(Node node);
	
	boolean addEdge(Edge edge);
	
	void setRootNode(Node rootNode);

}
