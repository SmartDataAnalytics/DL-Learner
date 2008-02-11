package org.dllearner.core.dl;

import java.util.Map;

public class GreaterEqual extends NumberRestriction {

	public GreaterEqual(int number, ObjectPropertyExpression role, Concept c) {
		super(number,role,c);
	}

	@Override
	public int getArity() {
		return 1;
	}

	public String toString(String baseURI, Map<String,String> prefixes) {
		return ">= " + number + " " + role.toString(baseURI, prefixes) + " " + getChild(0).toString(baseURI, prefixes);
	}
}
