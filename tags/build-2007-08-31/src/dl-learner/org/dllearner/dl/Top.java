package org.dllearner.dl;



public class Top extends Concept {

	/*
    @Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        posSet = abox.top;
        negSet = abox.bottom;
    }
    */

	@Override		
    public String toString() {
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
