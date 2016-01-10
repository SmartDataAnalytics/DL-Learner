package org.dllearner.core.owl;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A flat ABox can be used to store knowledge of a completely dematerialised knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public class FlatABox {
    
    public SortedSet<String> roles = new TreeSet<>();
    public SortedSet<String> concepts = new TreeSet<>();
    public SortedSet<String> domain = new TreeSet<>();
    public SortedSet<String> top = new TreeSet<>();
    public SortedSet<String> bottom = new TreeSet<>();
    
    public Map<String,SortedSet<String>> atomicConceptsPos = new HashMap<>();
    public Map<String,SortedSet<String>> atomicConceptsNeg = new HashMap<>();
    public Map<String,Map<String,SortedSet<String>>> rolesPos = new HashMap<>();
    public Map<String,Map<String,SortedSet<String>>> rolesNeg = new HashMap<>();
    
    public Map<String,SortedSet<String>> exampleConceptsPos = new HashMap<>();
    public Map<String,SortedSet<String>> exampleConceptsNeg = new HashMap<>();
    
    public FlatABox() {
        
    }
    
    public SortedSet<String> getPositiveInstances(String conceptName) {
    	return atomicConceptsPos.get(conceptName);
    }
    
    public SortedSet<String> getNegativeInstances(String conceptName) {
    	return atomicConceptsPos.get(conceptName);
    }
    
	@Override	    
    public String toString() {
        String output = "";
        output += "domain: " + domain.toString() + "\n";
        output += "top: " + top.toString() + "\n";
        output += "bottom: " + bottom.toString() + "\n";
        output += "concept pos: " + atomicConceptsPos.toString() + "\n";    
        output += "concept neg: " + atomicConceptsNeg.toString() + "\n";       
        output += "role pos: " + rolesPos.toString() + "\n";    
        output += "role neg: " + rolesNeg.toString() + "\n"; 
        output += "positive examples: " + exampleConceptsPos.toString() + "\n";
        output += "negative examples: " + exampleConceptsNeg.toString() + "\n";
        return output;
    }
    
    public String getTargetConcept() {
    	return (String) exampleConceptsPos.keySet().toArray()[0];
    }
    
//    public void createExampleABox() {
//        domain = new TreeSet<String>();
//        domain.add("stefan");
//        domain.add("markus");
//        
//        top = domain;
//        bottom = new TreeSet<String>();
//        
//        atomicConceptsPos = new HashMap<String,Set<String>>();
//        Set<String> male = new TreeSet<String>();
//        male.add("stefan");
//        male.add("markus");
//        atomicConceptsPos.put("male",male);
//        
//        atomicConceptsNeg = new HashMap<String,Set<String>>();
//        Set<String> maleNeg = new TreeSet<String>();  
//        atomicConceptsNeg.put("male",maleNeg);
//        
//        rolesPos = new HashMap<String,Map<String,Set<String>>>();
//        Map<String,Set<String>> hasChild = new HashMap<String,Set<String>>();
//        Set<String> childsStefan = new TreeSet<String>();
//        childsStefan.add("markus");
//        hasChild.put("stefan",childsStefan);
//        Set<String> childsMarkus = new TreeSet<String>();
//        hasChild.put("markus", childsMarkus);
//        rolesPos.put("hasChild", hasChild);
//        
//        rolesNeg = new HashMap<String,Map<String,Set<String>>>();
//        Map<String,Set<String>> hasChildNeg = new HashMap<String,Set<String>>();
//        Set<String> childsStefanNeg = new TreeSet<String>();
//        hasChildNeg.put("stefan",childsStefanNeg);
//        Set<String> childsMarkusNeg = new TreeSet<String>();
//        hasChildNeg.put("markus", childsMarkusNeg);
//        rolesNeg.put("hasChild", hasChildNeg);
//    }
    
    
}
