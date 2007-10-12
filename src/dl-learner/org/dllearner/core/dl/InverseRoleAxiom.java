package org.dllearner.core.dl;

import java.util.Map;

public class InverseRoleAxiom extends RBoxAxiom {

	private AtomicRole inverseRole;
	private AtomicRole role;
	
	public InverseRoleAxiom(AtomicRole inverseRole, AtomicRole role) {
		this.inverseRole = inverseRole;
		this.role = role;
	}

	public AtomicRole getInverseRole() {
		return inverseRole;
	}

	public AtomicRole getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength() + inverseRole.getLength();
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Inverse(" + inverseRole + "," + role.toString(baseURI, prefixes) + ")";
	}	
}
