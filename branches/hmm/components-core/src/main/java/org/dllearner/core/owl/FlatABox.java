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
