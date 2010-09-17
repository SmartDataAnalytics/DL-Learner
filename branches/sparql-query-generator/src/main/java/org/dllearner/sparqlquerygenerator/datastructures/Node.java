package org.dllearner.sparqlquerygenerator.datastructures;

import java.util.Set;

public interface Node {
	
	String getLabel();
	
	Set<Edge> getInEdges();
	
	Set<Edge> getOutEdges();
	
	boolean addInEdge(Edge edge);
	
	boolean addOutEdge(Edge edge);
	
	

}
