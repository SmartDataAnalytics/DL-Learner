package org.dllearner.core.owl;

public abstract class ObjectCardinalityRestriction extends CardinalityRestriction {

	protected ObjectPropertyExpression role;
	protected int number;
	
	public ObjectCardinalityRestriction(int number, ObjectPropertyExpression role, Description c) {
		super(role, c, number);
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
