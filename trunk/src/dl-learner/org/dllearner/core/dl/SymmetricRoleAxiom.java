package org.dllearner.core.dl;

public class SymmetricRoleAxiom implements RBoxAxiom {

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
	
	@Override		
	public String toString() {
		return "Symmetric(" + role.toString() + ")";
	}	
}
