package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Property extends SPARQL_Value {
	
	private SPARQL_Prefix prefix = null;

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
	
	public SPARQL_Prefix getPrefix() {
		return prefix;
	}

	public void setPrefix(SPARQL_Prefix prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public String toString() {
		if (isVariable()) {
			return "?" + name;
		}
		if (prefix == null) {
			return name;
		}
		return prefix.getName()+":"+name;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SPARQL_Property other = (SPARQL_Property) obj;
		if (prefix == null) {
			if (other.prefix != null)
				return false;
		} else if (!prefix.equals(other.prefix))
			return false;
		return true;
	}
	
	

}
