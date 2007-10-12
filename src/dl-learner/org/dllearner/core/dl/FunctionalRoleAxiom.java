package org.dllearner.core.dl;

import java.util.Map;

public class FunctionalRoleAxiom extends RBoxAxiom {

	private AtomicRole role;
	
	public FunctionalRoleAxiom(AtomicRole role) {
		this.role = role;
	}

	public Role getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength();
	}
		
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Functional(" + role.toString(baseURI, prefixes) + ")";
	}
}
