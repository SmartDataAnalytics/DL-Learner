package org.dllearner.core.owl;

import java.util.Map;

public class TransitiveObjectPropertyAxiom extends PropertyAxiom {

	private ObjectPropertyExpression role;
	
	public TransitiveObjectPropertyAxiom(ObjectPropertyExpression role) {
		this.role = role;
	}

	public int getLength() {
		return 1 + role.getLength();
	}

	public ObjectPropertyExpression getRole() {
		return role;
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Transitive(" + role.toString(baseURI, prefixes) + ")";
	}
}
