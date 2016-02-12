package org.dllearner.algorithms.versionspace;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * The version space. This is basically a directed graph with a root node,
 * i.e. a node without having incoming edges.
 *
 * @author Lorenz Buehmann
 */
public class VersionSpace<T extends VersionSpaceNode> extends DefaultDirectedGraph<T, DefaultEdge>{

	private T root;

	public VersionSpace(T root) {
		super(DefaultEdge.class);
		this.root = root;

		addVertex(root);
	}

	/**
	 * @return the root node
	 */
	public T getRoot() {
		return root;
	}
}
