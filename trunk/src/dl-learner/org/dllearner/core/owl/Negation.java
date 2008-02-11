package org.dllearner.core.owl;

import java.util.Map;


public class Negation extends Concept {

	/*
    @Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        children.get(0).calculateSets(abox, posSet, negSet);
        posSet = children.get(0).negSet;
        negSet = children.get(0).posSet;
    }
    */

	public Negation(Concept c) {
		addChild(c);
	}
	
    public String toString(String baseURI, Map<String,String> prefixes) {
        return "(NOT " +children.get(0).toString(baseURI, prefixes) + ")";
    }

	public int getLength() {
		return 1 + children.get(0).getLength();
	}

	@Override
	public int getArity() {
		return 1;
	}       
    
}
