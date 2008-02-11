package org.dllearner.core.owl;

import java.util.Map;

public class SymmetricObjectPropertyAxiom extends PropertyAxiom {

	private ObjectProperty role;
	
	public SymmetricObjectPropertyAxiom(ObjectProperty role) {
		this.role = role;
	}

	public ObjectProperty getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength();
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Symmetric(" + role.toString(baseURI, prefixes) + ")";
	}	
}
