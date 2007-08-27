package org.dllearner.dl;


public class All extends Quantification {
    
	public All(Role role, Concept c) {
		super(role, c);
	}
	
	/*
    public All(String roleName) {
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

        // Daten zu R+ und R-
        Map<String, SortedSet<String>> rplus = abox.rolesPos.get(roleName);
        Map<String, SortedSet<String>> rminus = abox.rolesNeg.get(roleName);
        // Daten zu C und C+
        Set<String> cPlus = children.get(0).posSet;
        Set<String> cMinus = children.get(0).negSet;

        // Fallunterscheidungen einbauen, da R+ und R- leer sein können
        // und es nicht für jedes a der Domain ein (a,b) \in R+ bzw. R- geben muss;
        // man beachte, dass viele Regeln nur gelten, weil als Domain die Menge aller
        // Individuen angenommen wird!
        
        // R- ist leer
        if(rminus==null) {
            // falls C die ganze Domain umfasst, dann erüllt jedes Individual
            // All R.C, ansonsten keines (es muss nichts gemacht werden)
            if(cPlus.equals(abox.domain))
                // keine Kopie notwendig, da Domain unveränderlich
                posSet = abox.domain;
        } else {
            for (String a : abox.domain) {
                if(!rminus.containsKey(a)) {
                    // a erfüllt die Bedingung, falls alle b in C+ sind
                    if(cPlus.equals(abox.domain))
                        posSet.add(a);
                }
                else if (checkAll(Helper.difference(abox.domain, rminus.get(a)), cPlus))
                    posSet.add(a);
            }                    
        }
        
        // falls R+ leer ist, dann ist Bedingung nie erfüllt
        if(rplus!=null) {
            for (String a : rplus.keySet()) {
                // falls R+ Schlüssel nicht enthält, ist Bedingung nicht erfüllt
                if (rplus.containsKey(a) && checkExist(rplus.get(a), cMinus))
                    negSet.add(a);
            }
        }



    }

    // aus Exists-Klasse übernommen
    private static boolean checkExist(Set<String> s1, Set<String> s2) {
        for (String b : s1) {
            if (s2.contains(b))
                return true;
        }
        return false;
    }

    private static boolean checkAll(Set<String> s1, Set<String> s2) {
        for (String b : s1) {
            if (!s2.contains(b)) 
                return false;
        }
        return true;
    }    
    */
    
	@Override		
    public String toString() {
        return "ALL " + role + "." + children.get(0).toString();
    }

    /*
	public int getLength() {
		return 2;
	}
	*/         
    
}
