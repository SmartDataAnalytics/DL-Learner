package org.dllearner.core.owl;

import java.util.Map;

public class FunctionalRoleAxiom extends RBoxAxiom {

	private ObjectProperty role;
	
	public FunctionalRoleAxiom(ObjectProperty role) {
		this.role = role;
	}

	public ObjectPropertyExpression getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength();
	}
		
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Functional(" + role.toString(baseURI, prefixes) + ")";
	}
}
