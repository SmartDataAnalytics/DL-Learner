package org.dllearner.core.owl;

public abstract class Axiom implements KBElement {

	@Override
	public String toString() {
		return toString(null, null);
	}
	
	public abstract void accept(AxiomVisitor visitor);	
}
