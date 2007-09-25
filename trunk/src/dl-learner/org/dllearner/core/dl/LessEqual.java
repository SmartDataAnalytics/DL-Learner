package org.dllearner.core.dl;

public class LessEqual extends NumberRestriction {

	public LessEqual(int number, Role role, Concept c) {
		super(number,role,c);
	}

	@Override
	public int getArity() {
		return 1;
	}	

	@Override
	public String toString() {
		return "<= " + number + " " + role + " " + getChild(0);
	}	
}
