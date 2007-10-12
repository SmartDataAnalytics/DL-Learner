package org.dllearner.core.dl;

public abstract class Quantification extends Concept {

	Role role;
	
	public Quantification(Role role, Concept c) {
		this.role = role;
		addChild(c);
	}
	
	public Role getRole() {
		return role;
	}
	
	public int getLength() {
		return 1 + role.getLength() + getChild(0).getLength();
	}

	@Override
	public int getArity() {
		return 1;
	}

}
