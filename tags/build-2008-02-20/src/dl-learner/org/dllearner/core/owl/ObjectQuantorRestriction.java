package org.dllearner.core.owl;

public abstract class ObjectQuantorRestriction extends QuantorRestriction {

	ObjectPropertyExpression role;
	
	public ObjectQuantorRestriction(ObjectPropertyExpression role, Description c) {
		this.role = role;
		addChild(c);
	}
	
	public ObjectPropertyExpression getRole() {
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
