package org.dllearner.dl;



public class Conjunction extends Concept {

	/*
    @Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        children.get(0).calculateSets(abox, posSet, negSet);
        children.get(1).calculateSets(abox, posSet, negSet);
        
        posSet = Helper.intersection(children.get(0).posSet,children.get(1).posSet);
        negSet = Helper.union(children.get(0).negSet,children.get(1).negSet);         
    }
    */

	public Conjunction(Concept c1, Concept c2) {
		addChild(c1);
		addChild(c2);
	}
	
	@Override		
    public String toString() {
        return "(" + children.get(0).toString() + " AND " + children.get(1).toString() + ")";
    }

	public int getLength() {
		return 1 + getChild(0).getLength() + getChild(1).getLength();
	}

	@Override
	public int getArity() {
		return 2;
	}    
}