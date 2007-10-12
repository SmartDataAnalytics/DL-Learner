package org.dllearner.core.dl;

import java.util.Map;

public class TransitiveRoleAxiom extends RBoxAxiom {

	private Role role;
	
	public TransitiveRoleAxiom(Role role) {
		this.role = role;
	}

	public int getLength() {
		return 1 + role.getLength();
	}

	public Role getRole() {
		return role;
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Transitive(" + role.toString(baseURI, prefixes) + ")";
	}
}
