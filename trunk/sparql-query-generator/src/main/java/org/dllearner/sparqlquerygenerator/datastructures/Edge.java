package org.dllearner.sparqlquerygenerator.datastructures;


public interface Edge {
	
	String getLabel();
	
	Node getSourceNode();
	
	Node getTargetNode();

}
