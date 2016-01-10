package org.dllearner.utilities.datastructures;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Interface for search tree nodes, which are used in various algorithms.
 * 
 * @author Jens Lehmann
 *
 */
public interface SearchTreeNode {

	/**
	 * Gets the OWL 2 class expression at this search tree node.
	 * @return The expression at this node.
	 */
	OWLClassExpression getExpression();
	
	/**
	 * The children of this node.
	 * @return The children of this node.
	 */
	Collection<? extends SearchTreeNode> getChildren();
}
