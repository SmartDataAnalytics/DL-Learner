package org.dllearner.core.owl;

import java.util.Map;

public class FunctionalObjectPropertyAxiom extends PropertyAxiom {

	private ObjectProperty role;
	
	public FunctionalObjectPropertyAxiom(ObjectProperty role) {
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
