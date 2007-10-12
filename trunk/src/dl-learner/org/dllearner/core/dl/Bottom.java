package org.dllearner.core.dl;

import java.util.Map;



public class Bottom extends Concept {

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
}
