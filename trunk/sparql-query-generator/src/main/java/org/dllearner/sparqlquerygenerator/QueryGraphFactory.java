package org.dllearner.sparqlquerygenerator;

import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;
import org.dllearner.sparqlquerygenerator.datastructures.QueryGraph;

public interface QueryGraphFactory {
	
	QueryGraph getQueryGraph(Set<Node> nodes, Set<Edge> edges, Node rootNode);
	
	QueryGraph getQueryGraph(Node rootNode);
	
	QueryGraph getQueryGraph();

}
