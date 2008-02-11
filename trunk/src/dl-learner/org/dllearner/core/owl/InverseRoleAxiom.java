package org.dllearner.core.owl;

import java.util.Map;

public class InverseRoleAxiom extends RBoxAxiom {

	private ObjectProperty inverseRole;
	private ObjectProperty role;
	
	public InverseRoleAxiom(ObjectProperty inverseRole, ObjectProperty role) {
		this.inverseRole = inverseRole;
		this.role = role;
	}

	public ObjectProperty getInverseRole() {
		return inverseRole;
	}

	public ObjectProperty getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength() + inverseRole.getLength();
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Inverse(" + inverseRole + "," + role.toString(baseURI, prefixes) + ")";
	}	
}
