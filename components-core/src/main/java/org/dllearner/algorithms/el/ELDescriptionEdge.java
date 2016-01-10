package org.dllearner.algorithms.el;

import org.semanticweb.owlapi.model.OWLProperty;

/**
 * A (directed) edge in an EL OWLClassExpression tree. It consists of an edge
 * label, which is an object property, and the EL OWLClassExpression tree
 * the edge points to.
 * 
 * @author Jens Lehmann
 *
 */
public class ELDescriptionEdge {

	private OWLProperty label;
	
	private ELDescriptionNode node;

	/**
	 * Constructs and edge given a label and an EL OWLClassExpression tree.
	 * @param label The label of this edge.
	 * @param tree The tree the edge points to (edges are directed).
	 */
	public ELDescriptionEdge(OWLProperty label, ELDescriptionNode tree) {
		this.label = label;
		this.node = tree;
	}
	
	/**
	 * @param label the label to set
	 */
	public void setLabel(OWLProperty label) {
		this.label = label;
	}

	/**
	 * @return The label of this edge.
	 */
	public OWLProperty getLabel() {
		return label;
	}

	/**
	 * @return The EL OWLClassExpression tree 
	 */
	public ELDescriptionNode getNode() {
		return node;
	}
	
	public boolean isObjectProperty(){
		return label.isOWLObjectProperty();
	}
	
	@Override
	public String toString() {
		return "--" + label + "--> " + node.toDescriptionString(); 
	}
	
}
