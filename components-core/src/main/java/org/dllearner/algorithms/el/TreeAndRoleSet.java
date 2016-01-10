package org.dllearner.algorithms.el;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLProperty;

/**
 * Convenience class representing an EL OWLClassExpression tree and a set of roles.
 * 
 * @author Jens Lehmann
 *
 */
public class TreeAndRoleSet {

	private ELDescriptionTree tree;
	private Set<OWLProperty> roles;
	
	public TreeAndRoleSet(ELDescriptionTree tree, Set<OWLProperty> roles) {
		this.tree = tree;
		this.roles = roles;
	}

	/**
	 * @return the tree
	 */
	public ELDescriptionTree getTree() {
		return tree;
	}

	/**
	 * @return the roles
	 */
	public Set<OWLProperty> getRoles() {
		return roles;
	}
	
	@Override
	public String toString() {
		return "("+tree.toDescriptionString() + "," + roles.toString()+")";
	}
	
}
