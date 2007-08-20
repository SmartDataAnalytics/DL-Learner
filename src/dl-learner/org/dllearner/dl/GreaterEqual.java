package org.dllearner.dl;

public class GreaterEqual extends NumberRestriction {

	public GreaterEqual(int number, Role role, Concept c) {
		super(number,role,c);
	}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public String toString() {
		return ">= " + number + " " + role + " " + getChild(0);
	}
}
