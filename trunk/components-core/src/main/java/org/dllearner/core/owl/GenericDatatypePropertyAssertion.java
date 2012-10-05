package org.dllearner.core.owl;

import java.util.Map;

public class GenericDatatypePropertyAssertion extends DatatypePropertyAssertion{
	
	private String lexicalForm;
	private Datatype datatype;
	
	public GenericDatatypePropertyAssertion(DatatypeProperty datatypeProperty, Individual individual, String lexicalForm, Datatype datatype) {
		super(datatypeProperty, individual);
		
		this.lexicalForm = lexicalForm;
		this.datatype = datatype;
	}
	
	public String getLexicalForm() {
		return lexicalForm;
	}
	
	public Datatype getDatatype() {
		return datatype;
	}

	@Override
	public String toString(String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(KBElementVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(AxiomVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

}
