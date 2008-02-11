package org.dllearner.core.dl;

import java.util.Map;


public class Exists extends Quantification {

    public Exists(ObjectPropertyExpression role, Concept c) {
    	super(role,c);
    }
    
    /*
    public Exists(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
		return roleName;
	}
	*/

    /*
	@Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        children.get(0).calculateSets(abox, posSet, negSet);

        // Daten zu R+
        Map<String, SortedSet<String>> rplus = abox.rolesPos.get(roleName);
        Map<String, SortedSet<String>> rminus = abox.rolesNeg.get(roleName);
        // Daten zu C und C+
        Set<String> cPlus = children.get(0).posSet;
        Set<String> cMinus = children.get(0).negSet;
        
        // es wird r(a,b) untersucht und sobald ein b mit b \in C+ gefunden
        // wird, wird a in posSet aufgenommen
        
        if(rplus!=null) {
            for (String a : rplus.keySet()) {
                if (rplus.containsKey(a) && checkExist(rplus.get(a), cPlus))
                    posSet.add(a);
            }
        }


        // ich muss über die ganze Domain gehen: selbst wenn für ein a gar kein
        // (a,b) in R- existiert, dann kann so ein a trotzdem die Bedingung erfüllen
        
        if(rminus==null) {
            if(cMinus.equals(abox.domain))
                negSet = abox.domain;            
        } else {
            for (String a : abox.domain) {
                if(!rminus.containsKey(a)) {
                    if(cMinus.equals(abox.domain))
                        negSet.add(a);                     
                } else
                    if (checkAll(Helper.difference(abox.domain, rminus.get(a)), cMinus))
                        negSet.add(a);
            }
        }

    }

    // check-Methoden: hier ist s1 jeweils immer die Menge aller b, die für
    // ein festes a in den
    // Updategleichungen die erste Bedingung erfüllen (z.B. alle b
    // mit (a,b) \not\in R-) und s2 die Menge, die die zweite Bedingung erfüllt

    // gibt true zurück, falls es ein b gibt mit b \in s1 und b \in s2,
    // ansonsten false
    private static boolean checkExist(Set<String> s1, Set<String> s2) {
        for (String b : s1) {
            if (s2.contains(b))
                return true;
        }
        return false;
    }

    // gibt false zurück, falls für ein b \in s1 gilt b \in s2,
    // ansonsten true
    private static boolean checkAll(Set<String> s1, Set<String> s2) {
        for (String b : s1) {
            if (!s2.contains(b))
                return false;
        }
        return true;
    }
    */
    
    public String toString(String baseURI, Map<String,String> prefixes) {
        return "EXISTS " + role.toString(baseURI, prefixes) + "." + children.get(0).toString(baseURI, prefixes);
    }

    /*
	public int getLength() {
		return 2;
	}
	*/       
}
