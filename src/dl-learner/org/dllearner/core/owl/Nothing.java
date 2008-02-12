package org.dllearner.core.owl;

import java.util.Map;



public class Nothing extends Description {

	/*
    @Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        posSet = abox.bottom;
        negSet = abox.top;
    }
    */
	
    public String toString(String baseURI, Map<String,String> prefixes) {
        return "BOTTOM";
    }

	public int getLength() {
		return 1;
	}

	@Override
	public int getArity() {
		return 0;
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
