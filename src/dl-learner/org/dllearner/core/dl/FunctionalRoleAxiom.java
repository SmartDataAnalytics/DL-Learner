package org.dllearner.core.dl;

public class FunctionalRoleAxiom implements RBoxAxiom {

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
	
	@Override		
	public String toString() {
		return "Functional(" + role.toString() + ")";
	}
}
