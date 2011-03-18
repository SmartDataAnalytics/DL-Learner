package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Property extends SPARQL_Value {
	
	private SPARQL_Prefix prefix = null;
	private boolean isVariable = false;
	

	public SPARQL_Property(String name) {
		super();
		this.prefix = null;
		this.name = name;
	}
	public SPARQL_Property(String name, SPARQL_Prefix prefix) {
		super();
		this.name = name;
		this.prefix = prefix;
	}
	
	public void setIsVariable(boolean b) {
		isVariable = b;
	}

	public SPARQL_Prefix getPrefix() {
		return prefix;
	}

	public void setPrefix(SPARQL_Prefix prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public String toString() {
		if (isVariable) {
			return "?" + name;
		}
		if (prefix == null) {
			return name;
		}
		return prefix.getName()+":"+name;
	}
	
	

}
