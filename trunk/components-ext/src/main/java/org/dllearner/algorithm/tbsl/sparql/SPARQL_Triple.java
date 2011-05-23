package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Triple {
	SPARQL_Term variable = new SPARQL_Term("");
	SPARQL_Property property = new SPARQL_Property("");
	SPARQL_Value value = new SPARQL_Value();
	boolean optional;
	
	public boolean isOptional() {
		return optional;
	}
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	public SPARQL_Term getVariable() {
		return variable;
	}
	public void setVariable(SPARQL_Term variable) {
		this.variable = variable;
	}
	public SPARQL_Property getProperty() {
		return property;
	}
	public void setProperty(SPARQL_Property property) {
		this.property = property;
	}
	public SPARQL_Value getValue() {
		return value;
	}
	public void setValue(SPARQL_Value value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		if (optional) {
			return "OPTIONAL {"+variable.toString()+" "+property.toString()+" "+value.toString()+"}";
			
		}
		return variable.toString()+" "+property.toString()+" "+value.toString();
	}
	
	public SPARQL_Triple(SPARQL_Term variable, SPARQL_Property property,
			SPARQL_Value value) {
		super();
		this.variable = variable;
		this.property = property;
		this.value = value;
	}
	
	public SPARQL_Triple(SPARQL_Term variable, SPARQL_Property property,
			SPARQL_Value value, boolean optional) {
		super();
		this.variable = variable;
		this.property = property;
		this.value = value;
		this.optional = optional;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (optional ? 1231 : 1237);
		result = prime * result
				+ ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result
				+ ((variable == null) ? 0 : variable.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SPARQL_Triple other = (SPARQL_Triple) obj;
		if (optional != other.optional)
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}
	
	
}
