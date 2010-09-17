package org.dllearner.sparqlquerygenerator.impl;

import java.util.Set;

import org.dllearner.sparqlquerygenerator.QueryGraphFactory;
import org.dllearner.sparqlquerygenerator.datastructures.Edge;
import org.dllearner.sparqlquerygenerator.datastructures.Node;
import org.dllearner.sparqlquerygenerator.datastructures.QueryGraph;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryGraphImpl;

public class QueryGraphFactoryImpl implements QueryGraphFactory {

	@Override
	public QueryGraph getQueryGraph(Set<Node> nodes, Set<Edge> edges,
			Node rootNode) {
		return new QueryGraphImpl(nodes, edges, rootNode);
	}

	@Override
	public QueryGraph getQueryGraph(Node rootNode) {
		return new QueryGraphImpl(rootNode);
	}

	@Override
	public QueryGraph getQueryGraph() {
		return new QueryGraphImpl();
	}

}
