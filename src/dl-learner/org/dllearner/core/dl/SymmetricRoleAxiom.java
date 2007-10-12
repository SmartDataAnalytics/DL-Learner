package org.dllearner.core.dl;

import java.util.Map;

public class SymmetricRoleAxiom extends RBoxAxiom {

	private AtomicRole role;
	
	public SymmetricRoleAxiom(AtomicRole role) {
		this.role = role;
	}

	public AtomicRole getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength();
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Symmetric(" + role.toString(baseURI, prefixes) + ")";
	}	
}
