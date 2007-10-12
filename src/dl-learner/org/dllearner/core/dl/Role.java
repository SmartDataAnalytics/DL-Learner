package org.dllearner.core.dl;

public abstract class Role implements KBElement {

	protected String name;
	
	public Role(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
