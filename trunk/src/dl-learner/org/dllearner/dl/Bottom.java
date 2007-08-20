package org.dllearner.dl;



public class Bottom extends Concept {

	/*
    @Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        posSet = abox.bottom;
        negSet = abox.top;
    }
    */

    public String toString() {
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
