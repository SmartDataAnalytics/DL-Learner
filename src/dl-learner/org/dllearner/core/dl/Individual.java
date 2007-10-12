package org.dllearner.core.dl;

import java.util.Map;

import org.dllearner.utilities.Helper;

/**
 * Indiviualklasse soll ein Mapping von Namen auf Zahlen sein, so
 * dass compare-Methoden schneller werden.
 * 
 * Wenn man individuals nur als Strings repräsentiert, dann funktioniert
 * auch die Kurzdarstellung mit hidePrefix nicht (obwohl das keine so
 * hohe Priorität hat und man an geeigneten Stellen auch anders lösen
 * kann).
 * 
 * => das sollte man erst nach den ersten Papers im Februar einbinden um
 * größere Änderungen im System zu vermeiden
 * 
 * @author jl
 *
 */
public class Individual implements KBElement, Comparable<Individual> {

	// public static int idCounter = 0;
	
	// private int id;
	private String name;

	public String getName() {
		return name;
	}

	public Individual(String name) {
		this.name = name;
		// id = idCounter;
		// idCounter++;
	}
	
	public int getLength() {
		return 1;
	}

    @Override
    public String toString() {
//    	String prefixToHide = Helper.findPrefixToHide(name); 
//    		
//    	if(prefixToHide != null)
//    		return name.substring(prefixToHide.length());
//    	else
    	    return name;
    }

	public int compareTo(Individual o) {
		return name.compareTo(o.name);
	}
    
	@Override
	public boolean equals(Object o) {
		return (compareTo((Individual)o)==0);
	}
	
    public String toString(String baseURI, Map<String,String> prefixes) {
    	return Helper.getAbbreviatedString(name, baseURI, prefixes);
    }

}
