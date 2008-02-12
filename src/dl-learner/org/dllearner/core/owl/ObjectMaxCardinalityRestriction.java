package org.dllearner.core.owl;

import java.util.Map;

public class ObjectMaxCardinalityRestriction extends ObjectCardinalityRestriction {

	public ObjectMaxCardinalityRestriction(int number, ObjectPropertyExpression role, Description c) {
		super(number,role,c);
	}

	@Override
	public int getArity() {
		return 1;
	}	

	public String toString(String baseURI, Map<String,String> prefixes) {
		return "<= " + number + " " + role.toString(baseURI, prefixes) + " " + getChild(0).toString(baseURI, prefixes);
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#accept(org.dllearner.core.owl.DescriptionVisitor)
	 */
	@Override
	public void accept(DescriptionVisitor visitor) {
		visitor.visit(this);
	}	
}
