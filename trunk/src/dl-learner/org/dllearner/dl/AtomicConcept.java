package org.dllearner.dl;

import org.dllearner.utilities.Helper;



public class AtomicConcept extends Concept {

    String name;
    // List<String> subs
    
	public AtomicConcept(String name) {
        this.name = name;
    }    
    
    public String getName() {
		return name;
	}
    
    /*
    @Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        posSet = abox.atomicConceptsPos.get(conceptName);
        negSet = abox.atomicConceptsNeg.get(conceptName);
        
        if(posSet == null)
            posSet = new TreeSet<String>();
        if(negSet == null)
            negSet = new TreeSet<String>();       
    }
    */
    
    @Override
    public String toString() {
    	// es soll ein Prefix ausgeblendet werden um Konzepte
    	// lesbarer zu machen (z.B. http://blabla.org/bla/bla#eigentlicher Name
    	String prefixToHide = Helper.findPrefixToHide(name); 
    		
    	if(prefixToHide != null)
    		return name.substring(prefixToHide.length());
    	else
    	    return name;
    }

	public int getLength() {
		return 1;
	}

	@Override
	public int getArity() {
		return 0;
	}

}
