package org.dllearner.core.owl;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A class OWLClassExpression in its context, i.e. including a parent link (if any).
 * For instance, there is only one OWLClassExpression owl:Thing, but it can occur
 * nested within different descriptions like "createdBy SOME owl:Thing". 
 * Depending on what you want to do, you either need a OWLClassExpression or a
 * ContextDescription. 
 * 
 * @author Jens Lehmann
 *
 */
public class ContextDescription {

	private OWLClassExpression description;
	
	private OWLClassExpression parent;
	
	public ContextDescription(OWLClassExpression description, OWLClassExpression parent) {
		this.description = description;
		this.parent = parent;
	}

	/**
	 * @return the description
	 */
	public OWLClassExpression getDescription() {
		return description;
	}

	/**
	 * @return the parent
	 */
	public OWLClassExpression getParent() {
		return parent;
	}
	
}
