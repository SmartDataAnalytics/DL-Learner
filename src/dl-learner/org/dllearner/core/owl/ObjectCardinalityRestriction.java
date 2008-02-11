package org.dllearner.core.owl;

public abstract class ObjectCardinalityRestriction extends Description {

	protected ObjectPropertyExpression role;
	protected int number;
	
	public ObjectCardinalityRestriction(int number, ObjectPropertyExpression role, Description c) {
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
