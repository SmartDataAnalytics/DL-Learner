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
}
