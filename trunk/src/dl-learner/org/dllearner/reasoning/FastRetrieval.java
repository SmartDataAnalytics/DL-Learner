package org.dllearner.reasoning;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.dl.All;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.Bottom;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Conjunction;
import org.dllearner.core.dl.Disjunction;
import org.dllearner.core.dl.Exists;
import org.dllearner.core.dl.FlatABox;
import org.dllearner.core.dl.MultiConjunction;
import org.dllearner.core.dl.MultiDisjunction;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.Top;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.SortedSetTuple;

public class FastRetrieval {

	private FlatABox abox;
	
	public FastRetrieval(FlatABox abox) {
		this.abox = abox;
	}
	
	public SortedSetTuple<String> calculateSets(Concept concept) {
		return calculateSetsADC(concept, null);
	}
	
	// Algorithmus wird ueber Rekursion und 
	// Delegation zur Helper-Klasse implementiert
	public SortedSetTuple<String> calculateSetsADC(Concept concept, SortedSetTuple<String> adcSet) {
		if(concept instanceof Top) {
			return new SortedSetTuple<String>(abox.top,abox.bottom);
		} else if(concept instanceof Bottom) {
			return new SortedSetTuple<String>(abox.bottom,abox.top);
		} else if(concept instanceof AtomicConcept) {
			SortedSet<String> pos = abox.getPositiveInstances(((AtomicConcept)concept).getName());
			SortedSet<String> neg = abox.getNegativeInstances(((AtomicConcept)concept).getName());
			return new SortedSetTuple<String>(pos,neg);
		} else if(concept instanceof Negation) {
			return calculateNegationSet(calculateSetsADC(concept.getChild(0), adcSet));
		} else if(concept instanceof Conjunction) {
			return calculateConjunctionSets(calculateSetsADC(concept.getChild(0),adcSet),calculateSetsADC(concept.getChild(1),adcSet));
		} else if(concept instanceof Disjunction) {
			return calculateDisjunctionSets(calculateSetsADC(concept.getChild(0),adcSet),calculateSetsADC(concept.getChild(1),adcSet));
		} else if(concept instanceof MultiConjunction) {
			SortedSetTuple<String> res = 
			calculateConjunctionSets(calculateSetsADC(concept.getChild(0),adcSet),calculateSetsADC(concept.getChild(1),adcSet));
			for(int i=2; i < concept.getChildren().size(); i++) {
				res = calculateConjunctionSets(res,calculateSetsADC(concept.getChild(i),adcSet));
			}
			return res;
		} else if(concept instanceof MultiDisjunction) {
			SortedSetTuple<String> res = 
			calculateDisjunctionSets(calculateSetsADC(concept.getChild(0),adcSet),calculateSetsADC(concept.getChild(1),adcSet));
			for(int i=2; i < concept.getChildren().size(); i++) {
				res = calculateDisjunctionSets(res,calculateSetsADC(concept.getChild(i),adcSet));
			}
			return res;			
		} else if(concept instanceof All) {
			return calculateAllSet(abox,((All)concept).getRole().getName(),calculateSetsADC(concept.getChild(0),adcSet));
		} else if(concept instanceof Exists) {
			return calculateExistsSet(abox,((Exists)concept).getRole().getName(),calculateSetsADC(concept.getChild(0),adcSet));
		}
			
		throw new Error("Unknown concept type " + concept);
	}

    
	public static SortedSetTuple<String> calculateConjunctionSets(SortedSetTuple<String> child1, SortedSetTuple<String> child2) {
		return new SortedSetTuple<String>(
		        Helper.intersection(child1.getPosSet(),child2.getPosSet()),
		        Helper.union(child1.getNegSet(),child2.getNegSet()));
	}    
	
	public static SortedSetTuple<String> calculateDisjunctionSets(SortedSetTuple<String> child1, SortedSetTuple<String> child2) {
		return new SortedSetTuple<String>(
		        Helper.union(child1.getPosSet(),child2.getPosSet()),
		        Helper.intersection(child1.getNegSet(),child2.getNegSet()));
	} 	
	
	public static SortedSetTuple<String> calculateNegationSet(SortedSetTuple<String> child) {
		return new SortedSetTuple<String>(child.getNegSet(),child.getPosSet());
	}
	
	public static SortedSetTuple<String> calculateExistsSet(FlatABox abox, String roleName, SortedSetTuple<String> child) {
		// FlatABox abox = FlatABox.getInstance();
		
		// zu beschreibende Mengen
		SortedSet<String> posSet = new TreeSet<String>();
		SortedSet<String> negSet = new TreeSet<String>();
		
        // Daten zu R+
        Map<String, SortedSet<String>> rplus = abox.rolesPos.get(roleName);
        Map<String, SortedSet<String>> rminus = abox.rolesNeg.get(roleName);
        
        // es wird r(a,b) untersucht und sobald ein b mit b \in C+ gefunden
        // wird, wird a in posSet aufgenommen
        
        if(rplus!=null) {
            for (String a : rplus.keySet()) {
                if (rplus.containsKey(a) && checkExist(rplus.get(a), child.getPosSet()))
                    posSet.add(a);
            }
        }

        // ich muss über die ganze Domain gehen: selbst wenn für ein a gar kein
        // (a,b) in R- existiert, dann kann so ein a trotzdem die Bedingung erfüllen
        if(rminus==null) {
            if(child.getNegSet().equals(abox.domain))
                negSet = abox.domain;            
        } else {
            for (String a : abox.domain) {
                if(!rminus.containsKey(a)) {
                    if(child.getNegSet().equals(abox.domain))
                        negSet.add(a);                     
                } else
                    if (checkAll(Helper.difference(abox.domain, rminus.get(a)), child.getNegSet()))
                        negSet.add(a);
            }
        }
        
        return new SortedSetTuple<String>(posSet,negSet);
	}
	
	public static SortedSetTuple<String> calculateAllSet(FlatABox abox, String roleName, SortedSetTuple<String> child) {
		// FlatABox abox = FlatABox.getInstance();
		
		// zu beschreibende Mengen
		SortedSet<String> posSet = new TreeSet<String>();
		SortedSet<String> negSet = new TreeSet<String>();		
		
        // Daten zu R+ und R-
        Map<String, SortedSet<String>> rplus = abox.rolesPos.get(roleName);
        Map<String, SortedSet<String>> rminus = abox.rolesNeg.get(roleName);

        // Fallunterscheidungen einbauen, da R+ und R- leer sein können
        // und es nicht für jedes a der Domain ein (a,b) \in R+ bzw. R- geben muss;
        // man beachte, dass viele Regeln nur gelten, weil als Domain die Menge aller
        // Individuen angenommen wird!
        
        // R- ist leer
        if(rminus==null) {
            // falls C die ganze Domain umfasst, dann erüllt jedes Individual
            // All R.C, ansonsten keines (es muss nichts gemacht werden)
            if(child.getPosSet().equals(abox.domain))
                // keine Kopie notwendig, da Domain unveränderlich
                posSet = abox.domain;
        } else {
            for (String a : abox.domain) {
                if(!rminus.containsKey(a)) {
                    // a erfüllt die Bedingung, falls alle b in C+ sind
                    if(child.getPosSet().equals(abox.domain))
                        posSet.add(a);
                }
                else if (checkAll(Helper.difference(abox.domain, rminus.get(a)), child.getPosSet()))
                    posSet.add(a);
            }                    
        }
        
        // falls R+ leer ist, dann ist Bedingung nie erfüllt
        if(rplus!=null) {
            for (String a : rplus.keySet()) {
                // falls R+ Schlüssel nicht enthält, ist Bedingung nicht erfüllt
                if (rplus.containsKey(a) && checkExist(rplus.get(a), child.getNegSet()))
                    negSet.add(a);
            }
        }	
        
        return new SortedSetTuple<String>(posSet,negSet);
	}
	
    // gibt true zurueck, falls es ein b gibt mit b \in s1 und b \in s2,
    // ansonsten false
    private static boolean checkExist(SortedSet<String> s1, SortedSet<String> s2) {
        for (String b : s1) {
            if (s2.contains(b))
                return true;
        }
        return false;
    }
    
    // gibt false zurueck, falls fuer ein b \in s1 gilt b \in s2,
    // ansonsten true
    private static boolean checkAll(SortedSet<String> s1, SortedSet<String> s2) {
        for (String b : s1) {
            if (!s2.contains(b))
                return false;
        }
        return true;
    }    	
	
	public FlatABox getAbox() {
		return abox;
	}
}
