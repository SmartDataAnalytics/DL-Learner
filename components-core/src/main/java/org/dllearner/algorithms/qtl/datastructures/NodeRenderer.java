package org.dllearner.algorithms.qtl.datastructures;


/**
 * 
 * @author Lorenz Bühmann
 *
 */
public interface NodeRenderer<N> {
	
	String render(QueryTree<N> node);

}
