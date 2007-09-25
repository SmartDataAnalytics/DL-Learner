package org.dllearner.core.dl;

public abstract class NumberRestriction extends Concept {

	protected Role role;
	protected int number;
	
	public NumberRestriction(int number, Role role, Concept c) {
		addChild(c);
		this.role = role;
		this.number = number;
	}
	
	public int getLength() {
		return 1 + role.getLength() + getChild(0).getLength();
	}

	public int getNumber() {
		return number;
	}

	public Role getRole() {
		return role;
	}

}
