package org.dllearner.core.owl;

import java.util.Map;

public class ObjectMaxCardinalityRestriction extends ObjectCardinalityRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -674570964821143091L;

	public ObjectMaxCardinalityRestriction(int number, ObjectPropertyExpression role, Description c) {
		super(number,role,c);
	}

	@Override
	public int getArity() {
		return 1;
	}	

	public String toString(String baseURI, Map<String,String> prefixes) {
		return "<= " + number + " " + role.toString(baseURI, prefixes) + "." + getChild(0).toString(baseURI, prefixes);
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return "<= " + number + " " + role.toKBSyntaxString(baseURI, prefixes) + "." + getChild(0).toKBSyntaxString(baseURI, prefixes);
	}
	
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return role.toString(baseURI, prefixes) + " max " + number + " " + getChild(0).toManchesterSyntaxString(baseURI, prefixes);
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#accept(org.dllearner.core.owl.DescriptionVisitor)
	 */
	@Override
	public void accept(DescriptionVisitor visitor) {
		visitor.visit(this);
	}	
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
}
