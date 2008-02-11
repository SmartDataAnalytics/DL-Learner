package org.dllearner.core.owl;

public abstract class NumberRestriction extends Concept {

	protected ObjectPropertyExpression role;
	protected int number;
	
	public NumberRestriction(int number, ObjectPropertyExpression role, Concept c) {
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

	public ObjectPropertyExpression getRole() {
		return role;
	}

}
