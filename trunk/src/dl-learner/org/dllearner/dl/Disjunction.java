package org.dllearner.dl;


public class Disjunction extends Concept {

	/*
    @Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        children.get(0).calculateSets(abox, posSet, negSet);
        children.get(1).calculateSets(abox, posSet, negSet);
        
        posSet = Helper.union(children.get(0).posSet,children.get(1).posSet);
        negSet = Helper.intersection(children.get(0).negSet,children.get(1).negSet);        
    }
    */

	public Disjunction(Concept c1, Concept c2) {
		addChild(c1);
		addChild(c2);
	}
	
    public String toString() {
        return "(" + children.get(0).toString() + " OR " + children.get(1).toString() + ")";
    }

	public int getLength() {
		return 1 + getChild(0).getLength() + getChild(1).getLength();
	}

	@Override
	public int getArity() {
		return 2;
	}        
    
}
