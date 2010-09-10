package org.dllearner.core.owl;

public abstract class Axiom implements KBElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8064697058621473304L;

	@Override
	public String toString() {
		return toString(null, null);
	}
	
	public abstract void accept(AxiomVisitor visitor);	
}
