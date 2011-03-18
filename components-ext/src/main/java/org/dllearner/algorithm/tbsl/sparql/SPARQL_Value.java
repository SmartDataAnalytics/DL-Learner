package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Value {
	protected String name;
	private boolean isVariable = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setIsVariable(boolean b) {
		isVariable = b;
	}

	public SPARQL_Value(String name) {
		super();
		this.name = name;
	}

	public SPARQL_Value() {
	}
	
	public String toString() {
		if (isVariable) {
			return "?"+name.toLowerCase();
		} else {
			return name;
		}
	}

}
