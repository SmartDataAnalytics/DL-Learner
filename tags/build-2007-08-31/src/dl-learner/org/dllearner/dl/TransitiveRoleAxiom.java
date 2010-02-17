package org.dllearner.dl;

public class TransitiveRoleAxiom implements RBoxAxiom {

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
	
	@Override		
	public String toString() {
		return "Transitive(" + role.toString() + ")";
	}	
}
