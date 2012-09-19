package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Entity extends SPARQL_Value {
	private SPARQL_Prefix prefix = null;

	public SPARQL_Entity(String name, SPARQL_Prefix prefix) {
		super(name);
		this.prefix = prefix;
	}
	
	@Override
	public String toString() {
		if(prefix == null) {
			return name;
		}
		return prefix.getName()+":"+name;
		
		
	}
}
