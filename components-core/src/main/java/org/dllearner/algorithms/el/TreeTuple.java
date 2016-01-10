package org.dllearner.algorithms.el;

/**
 * A tuple of two EL OWLClassExpression trees.
 * 
 * @author Jens Lehmann
 *
 */
public class TreeTuple {

	private ELDescriptionNode tree1;
	
	private ELDescriptionNode tree2;
	
	public TreeTuple(ELDescriptionNode tree1, ELDescriptionNode tree2) {
		this.tree1 = tree1;
		this.tree2 = tree2;
	}

	/**
	 * Gets first tree.
	 * @return - first tree
	 */
	public ELDescriptionNode getTree1() {
		return tree1;
	}

	/**
	 * Gets second tree.
	 * @return - second tree
	 */
	public ELDescriptionNode getTree2() {
		return tree2;
	}
	
}
