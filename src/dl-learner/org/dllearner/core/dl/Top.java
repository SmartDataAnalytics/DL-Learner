package org.dllearner.core.dl;

import java.util.Map;

public class Top extends Concept {

	/*
    @Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        posSet = abox.top;
        negSet = abox.bottom;
    }
    */

    public String toString(String baseURI, Map<String,String> prefixes) {
        return "TOP";
    }

	public int getLength() {
		return 1;
	}

	@Override
	public int getArity() {
		return 0;
	}    
}
