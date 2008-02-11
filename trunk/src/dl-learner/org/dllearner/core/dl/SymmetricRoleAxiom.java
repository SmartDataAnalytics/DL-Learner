package org.dllearner.core.dl;

import java.util.Map;

public class SymmetricRoleAxiom extends RBoxAxiom {

	private ObjectProperty role;
	
	public SymmetricRoleAxiom(ObjectProperty role) {
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
