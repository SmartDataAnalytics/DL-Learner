/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.reasoning;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.FlatABox;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Thing;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SortedSetTuple;

public class FastRetrieval {

	private FlatABox abox;
	
	public FastRetrieval(FlatABox abox) {
		this.abox = abox;
	}
	
	public SortedSetTuple<String> calculateSets(Description concept) {
		return calculateSetsADC(concept, null);
	}
	
	// Algorithmus wird ueber Rekursion und 
	// Delegation zur Helper-Klasse implementiert
	public SortedSetTuple<String> calculateSetsADC(Description concept, SortedSetTuple<String> adcSet) {
		if(concept instanceof Thing) {
			return new SortedSetTuple<String>(abox.top,abox.bottom);
		} else if(concept instanceof Nothing) {
			return new SortedSetTuple<String>(abox.bottom,abox.top);
		} else if(concept instanceof NamedClass) {
			SortedSet<String> pos = abox.getPositiveInstances(((NamedClass)concept).getName());
			SortedSet<String> neg = abox.getNegativeInstances(((NamedClass)concept).getName());
			return new SortedSetTuple<String>(pos,neg);
		} else if(concept instanceof Negation) {
			return calculateNegationSet(calculateSetsADC(concept.getChild(0), adcSet));
		} else if(concept instanceof Intersection) {
			// this should never happen, but it does; we work around the issue
			if(concept.getChildren().size()==1)
				return calculateSetsADC(concept.getChild(0),adcSet);			
			SortedSetTuple<String> res = 
			calculateConjunctionSets(calculateSetsADC(concept.getChild(0),adcSet),calculateSetsADC(concept.getChild(1),adcSet));
			for(int i=2; i < concept.getChildren().size(); i++) {
				res = calculateConjunctionSets(res,calculateSetsADC(concept.getChild(i),adcSet));
			}
			return res;
		} else if(concept instanceof Union) {
			// this should never happen, but it does; we work around the issue
			if(concept.getChildren().size()==1)
				return calculateSetsADC(concept.getChild(0),adcSet);
			
			SortedSetTuple<String> res = 
			calculateDisjunctionSets(calculateSetsADC(concept.getChild(0),adcSet),calculateSetsADC(concept.getChild(1),adcSet));
			for(int i=2; i < concept.getChildren().size(); i++) {
				res = calculateDisjunctionSets(res,calculateSetsADC(concept.getChild(i),adcSet));
			}
			return res;			
		} else if(concept instanceof ObjectAllRestriction) {
			return calculateAllSet(abox,((ObjectAllRestriction)concept).getRole().getName(),calculateSetsADC(concept.getChild(0),adcSet));
		} else if(concept instanceof ObjectSomeRestriction) {
			return calculateExistsSet(abox,((ObjectSomeRestriction)concept).getRole().getName(),calculateSetsADC(concept.getChild(0),adcSet));
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
