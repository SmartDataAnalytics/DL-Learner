package org.dllearner.algorithms.versionspace;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * The version space. This is basically a directed graph with a root node,
 * i.e. a node without having incoming edges.
 *
 * @author Lorenz Buehmann
 */
public class VersionSpace extends DefaultDirectedGraph<VersionSpaceNode, DefaultEdge>{

	private VersionSpaceNode root;

	public VersionSpace(VersionSpaceNode root) {
		super(DefaultEdge.class);
		this.root = root;

		addVertex(root);
	}

	/**
	 * @return the root node
	 */
	public VersionSpaceNode getRoot() {
		return root;
	}
}
