package org.dllearner.algorithms.versionspace;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A directed graph with a root node, i.e. a node without having incoming edges.
 *
 * @author Lorenz Buehmann
 */
public class RootedDirectedGraph extends DefaultDirectedGraph<OWLClassExpression, DefaultEdge>{

	private OWLClassExpression root;

	public RootedDirectedGraph(OWLClassExpression root) {
		super(DefaultEdge.class);
		this.root = root;

		addVertex(root);
	}

	/**
	 * @return the root concept
	 */
	public OWLClassExpression getRoot() {
		return root;
	}
}
