package org.dllearner.dl;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * TODO: Später mal eine Singleton-Klasse daraus machen und get-und set-Methoden
 * für alle Klassenvariablen.
 * TODO: Diese Klasse soll später mal alles geparstes DL-Wissen enthalten. Es muss
 * also irgendwie die "geparste ABox" (für eigenen Algorithmus) und das unveränderte
 * Wissen enthalten (für KAON2). Eventuell ist auch eine Trennung in 2 Klassen möglich.
 * 
 * TODO: Singleton wieder rueckgaengig machen, da FlatABox nur noch
 * Mittel zum Zweck fuer die neue abstrakte Reasoner-Klasse ist
 * 
 * @author Jens Lehmann
 *
 */
public class FlatABox {

    // singleton-instance
    // private static FlatABox singleton = new FlatABox();
    
    public SortedSet<String> roles = new TreeSet<String>();
    public SortedSet<String> concepts = new TreeSet<String>();
    public SortedSet<String> domain = new TreeSet<String>();
    public SortedSet<String> top = new TreeSet<String>();
    public SortedSet<String> bottom = new TreeSet<String>();
    
    public Map<String,SortedSet<String>> atomicConceptsPos = new HashMap<String,SortedSet<String>>();
    public Map<String,SortedSet<String>> atomicConceptsNeg = new HashMap<String,SortedSet<String>>();
    public Map<String,Map<String,SortedSet<String>>> rolesPos = new HashMap<String,Map<String,SortedSet<String>>>();
    public Map<String,Map<String,SortedSet<String>>> rolesNeg = new HashMap<String,Map<String,SortedSet<String>>>();
    
    public Map<String,SortedSet<String>> exampleConceptsPos = new HashMap<String,SortedSet<String>>();
    public Map<String,SortedSet<String>> exampleConceptsNeg = new HashMap<String,SortedSet<String>>();
    
    // f�r bessere GP-Initialisierungs-Performance
    private Object[] roleArray;
    private Object[] conceptArray;    
    
    public FlatABox() {
        
    }
    
    /*
    public static FlatABox getInstance() {
        return singleton;
    }
    */
    
    // ABox vorbereiten f�r Algorithmus, hier wird nur eine Listenansicht auf
    // Konzepte und Rollen erstellt, damit diese bei der GP-Initialisierung
    // verwendet werden kann (es ist ansonsten nicht m�glich auf ein zuf�lliges
    // Konzept zuzugreifen)
    public void prepare() {
    	roleArray = roles.toArray();
    	conceptArray = concepts.toArray();
    }
    
    public SortedSet<String> getPositiveInstances(String conceptName) {
    	return atomicConceptsPos.get(conceptName);
    }
    
    public SortedSet<String> getNegativeInstances(String conceptName) {
    	return atomicConceptsPos.get(conceptName);
    }
    
    public String getConcept(int nr) {
    	return (String) conceptArray[nr];
    }        
    
    public String getRole(int nr) {
    	return (String) roleArray[nr];
    }    
    
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
    
    /*
    public void createExampleABox() {
        domain = new TreeSet<String>();
        domain.add("stefan");
        domain.add("markus");
        
        top = domain;
        bottom = new TreeSet<String>();
        
        atomicConceptsPos = new HashMap<String,Set<String>>();
        Set<String> male = new TreeSet<String>();
        male.add("stefan");
        male.add("markus");
        atomicConceptsPos.put("male",male);
        
        atomicConceptsNeg = new HashMap<String,Set<String>>();
        Set<String> maleNeg = new TreeSet<String>();  
        atomicConceptsNeg.put("male",maleNeg);
        
        rolesPos = new HashMap<String,Map<String,Set<String>>>();
        Map<String,Set<String>> hasChild = new HashMap<String,Set<String>>();
        Set<String> childsStefan = new TreeSet<String>();
        childsStefan.add("markus");
        hasChild.put("stefan",childsStefan);
        Set<String> childsMarkus = new TreeSet<String>();
        hasChild.put("markus", childsMarkus);
        rolesPos.put("hasChild", hasChild);
        
        rolesNeg = new HashMap<String,Map<String,Set<String>>>();
        Map<String,Set<String>> hasChildNeg = new HashMap<String,Set<String>>();
        Set<String> childsStefanNeg = new TreeSet<String>();
        hasChildNeg.put("stefan",childsStefanNeg);
        Set<String> childsMarkusNeg = new TreeSet<String>();
        hasChildNeg.put("markus", childsMarkusNeg);
        rolesNeg.put("hasChild", hasChildNeg);
    }
    */
    
}
