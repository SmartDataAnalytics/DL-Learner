package org.dllearner.core.dl;

public abstract class Quantification extends Concept {

	ObjectPropertyExpression role;
	
	public Quantification(ObjectPropertyExpression role, Concept c) {
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
