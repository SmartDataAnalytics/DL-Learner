package org.dllearner.utilities;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.Config;
import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.AtomicRole;
import org.dllearner.dl.Concept;
import org.dllearner.dl.Individual;
import org.dllearner.dl.NumberRestriction;
import org.dllearner.dl.Quantification;

/**
 * Die Hilfsmethoden benutzen alle SortedSet, da die Operationen damit schneller sind.
 * @author jl
 *
 */
public class Helper {
     
	// findet alle atomaren Konzepte in einem Konzept
	public static List<AtomicConcept> getAtomicConcepts(Concept concept) {
		List<AtomicConcept> ret = new LinkedList<AtomicConcept>();
		if(concept instanceof AtomicConcept) {
			ret.add((AtomicConcept)concept);
			return ret;
		} else {
			for(Concept child : concept.getChildren()) {
				ret.addAll(getAtomicConcepts(child));
			}
			return ret;
		}
	}
	
	// findet alle atomaren Rollen in einem Konzept
	public static List<AtomicRole> getAtomicRoles(Concept concept) {
		List<AtomicRole> ret = new LinkedList<AtomicRole>();
		
		if(concept instanceof Quantification) {
			ret.add(new AtomicRole(((Quantification)concept).getRole().getName()));
		} else if(concept instanceof NumberRestriction) {
			ret.add(new AtomicRole(((NumberRestriction)concept).getRole().getName()));
		}
		
		// auch NumberRestrictions und Quantifications können weitere Rollen enthalten,
		// deshalb hier kein else-Zweig
		for(Concept child : concept.getChildren()) {
			ret.addAll(getAtomicRoles(child));
		}
		return ret;
		
	}	
	
	// sucht, ob der übergebene String mit einem Prefix beginnt der
	// versteckt werden soll und gibt diesen zurück, ansonsten wird
	// null zurück gegeben
	public static String findPrefixToHide(String name) {
    	for(String prefix : Config.hidePrefixes) {
    		if(name.startsWith(prefix))
    			return prefix;
    	}		
    	return null;
	}
	
	public static String prettyPrintNanoSeconds(long nanoSeconds) {
		return prettyPrintNanoSeconds(nanoSeconds, false, false);
	}
	
	// formatiert Nano-Sekunden in einen leserlichen String
	public static String prettyPrintNanoSeconds(long nanoSeconds, boolean printMicros, boolean printNanos) {
		// String str = "";
		// long seconds = 0;
		// long milliSeconds = 0;
		// long microseconds = 0;
		
		long seconds = nanoSeconds/1000000000;
		nanoSeconds = nanoSeconds % 1000000000;
		
		long milliSeconds = nanoSeconds/1000000;
		nanoSeconds = nanoSeconds % 1000000;

		// Mikrosekunden werden immer angezeigt, Sekunden nur falls größer 0
		String str = "";
		if(seconds > 0)
			str = seconds + "s ";
		str += milliSeconds + "ms";
		
		if(printMicros) {
			long microSeconds = nanoSeconds/1000;
			nanoSeconds = nanoSeconds % 1000;			
			str += " " + microSeconds + "usec";
		}
		if(printNanos) {
			str += " " + nanoSeconds + "ns";
		}
		
		return str;
	}
	
	public static<T1,T2> void addMapEntry(Map<T1, SortedSet<T2>> map,
			T1 keyEntry, T2 setEntry) {
		if (map.containsKey(keyEntry)) {
			map.get(keyEntry).add(setEntry);
		} else {
			SortedSet<T2> newSet = new TreeSet<T2>();
			newSet.add(setEntry);
			map.put(keyEntry, newSet);
		}
	}	
	
    /**
     * Das ist eine "generic method", d.h. die Methode hat einen bestimmten Typ.
     * Ich habe das benutzt um allen beteiligten Mengen den gleichen Typ zu geben,
     * denn ansonsten ist es nicht möglich der neu zu erzeugenden Menge (union) den
     * gleichen Typ wie den Argumenten zu geben. 
     * 
     * Die Methode hat gegenüber addAll den Vorteil, dass sie ein neues Objekt
     * erzeugt.
     * 
     * @param <T>
     * @param set1
     * @param set2
     * @return
     */
    public static<T> Set<T> unionAlt(Set<T> set1, Set<T> set2) {
        // TODO: effizientere Implementierung (längere Liste klonen und Elemente
        // anhängen)
        Set<T> union = new TreeSet<T>();
        union.addAll(set1);
        union.addAll(set2);
        return union;
        /*
        Set union;
        if(set1.size()>set2.size()) {
            union = set1.clone();
        } else {
            
        }
        return union;
        */
    }
    
    public static<T> SortedSet<T> union(SortedSet<T> set1, SortedSet<T> set2) {
    	//Set<T> union = set1.clone();
    	//((Cloneable) set1).clone();
    	
        // TODO: effizientere Implementierung (längere Liste klonen und Elemente
        // anhängen)
    	
    	// f�r TreeSet gibt es einen Konstruktor, der eine Collection entgegennimmt
    	// und einen weiteren, der ein SortedSet entgegennimmt; vermutlich ist
    	// letzterer schneller
    	
    	SortedSet<T> union;
    	if(set1.size()>set2.size()) {
    		union = new TreeSet<T>(set1);
    		union.addAll(set2);
    	} else {
    		union = new TreeSet<T>(set2);
    		union.addAll(set1);    		
    	}
        // SortedSet<T> union = new TreeSet<T>(set1);
        // union.addAll(set1);
        // union.addAll(set2);
        return union;

    }
    
    public static<T> SortedSet<T> intersection(SortedSet<T> set1, SortedSet<T> set2) {
        // TreeSet<T> intersection = (TreeSet<T>) set1.clone();
        // TODO: effizienter implementieren d.h. lange Liste klonen und dann
        // retainAll
        SortedSet<T> intersection = new TreeSet<T>(set1);
        // intersection.addAll(set1);
        intersection.retainAll(set2);
        return intersection;
    }
    
    public static<T> SortedSet<T> intersectionTuple(SortedSet<T> set, SortedSetTuple<T> tuple) {
    	SortedSet<T> ret = intersection(set,tuple.getPosSet());
    	ret.retainAll(tuple.getNegSet());
    	return ret;
    }
    
    public static<T> SortedSet<T> difference(SortedSet<T> set1, SortedSet<T> set2) {
        // TODO: effizienter implementieren 
        SortedSet<T> difference = new TreeSet<T>(set1);
        // difference.addAll(set1);
        difference.removeAll(set2);
        return difference;
    }

	// Umwandlung von Menge von Individuals auf Menge von Strings
	public static SortedSet<Individual> getIndividualSet(Set<String> individuals) {
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for(String s : individuals) {
			ret.add(new Individual(s));
		}
		return ret;
	}	
	
	public static SortedSetTuple<Individual> getIndividualTuple(SortedSetTuple<String> tuple) {
		return new SortedSetTuple<Individual>(getIndividualSet(tuple.getPosSet()),getIndividualSet(tuple.getNegSet()));
	}
	
	public static SortedSetTuple<String> getStringTuple(SortedSetTuple<Individual> tuple) {
		return new SortedSetTuple<String>(getStringSet(tuple.getPosSet()),getStringSet(tuple.getNegSet()));
	}	
	
	// Umwandlung von Menge von Individuals auf Menge von Strings
	public static SortedSet<String> getStringSet(Set<Individual> individuals) {
		SortedSet<String> ret = new TreeSet<String>();
		for(Individual i : individuals) {
			ret.add(i.getName());
		}
		return ret;
	}
	
	public static Map<String,SortedSet<String>> getStringMap(Map<Individual, SortedSet<Individual>> roleMembers) {
		Map<String,SortedSet<String>> ret = new TreeMap<String,SortedSet<String>>();
		for(Individual i : roleMembers.keySet()) {
			ret.put(i.getName(), getStringSet(roleMembers.get(i)));
		}
		return ret;
	}	
}
