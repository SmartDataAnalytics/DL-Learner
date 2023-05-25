package org.dllearner.cli.parcel.fortification;

/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


public class JaccardSimilarity {

	private int INDENT = 4;
	OWLReasoner reasoner;
	//PelletReasoner pellet;
	
	Logger logger = Logger.getLogger(this.getClass());

	private OWLDataFactory df = OWLManager.getOWLDataFactory();

    public JaccardSimilarity(OWLReasoner reasoner) {
    	this.reasoner = reasoner;
    	//pellet = new PelletReasonerFactory().createNonBufferingReasoner(reasoner.getRootOntology());
    }

    /** Print the class hierarchy for the given ontology from this class down,
     * assuming this class is at the given level. Makes no attempt to deal
     * sensibly with multiple inheritance. */
    public void printHierarchy(OWLClass clazz) throws OWLException {
        printHierarchy(reasoner, clazz, 0);
    }


    /** Print the class hierarchy from this class down, assuming this class is at
     * the given level. Makes no attempt to deal sensibly with multiple
     * inheritance. */
    private void printHierarchy(OWLReasoner reasoner, OWLClass clazz, int level)
            throws OWLException {
        /*
         * Only print satisfiable classes -- otherwise we end up with bottom
         * everywhere
         */
        if (reasoner.isSatisfiable(clazz)) {
            for (int i = 0; i < level * INDENT; i++) {
                System.out.print(" ");
            }
            System.out.println(clazz);
            /* Find the children and recurse */
            for (OWLClass child : reasoner.getSubClasses(clazz, true).getFlattened()) {
                if (!child.equals(clazz)) {
                    printHierarchy(reasoner, child, level + 1);
                }
            }
        }
    }
    
    
    /**
     * Get all subclasses of a NamedClass (DLLearner API)
     * 
     * @param clazz
     * @return
     */
	Set<OWLClassExpression> getsubClasses(OWLClassExpression clazz) {
		return reasoner.getSubClasses(clazz, false)
				.getNodes().stream()
				.filter(n -> !n.isBottomNode())
				.flatMap(n -> n.getEntities().stream())
				.collect(Collectors.toSet());
	}
    
    
    /**
     * Get all subclasses of a OWLClass (OWLAPI) 
     * @param clazz
     * @return
     */
    Set<OWLClass> getsubClasses(OWLClass clazz) {
    	return reasoner.getSubClasses(clazz, false).getFlattened();
    }
    
   

    /**
     * Get Jaccard overlap (score) between two concepts.
     * 
     * @return
     * @throws Exception 
     */
    /*
    public double getJaccardSimilaritySimple(Description des1, Description des2) throws Exception {
    	
    	Intersection conjunc = new Intersection(des1, des2);
    	
    	if (!reasoner.isSatisfiable(OWLAPIConverter.getOWLAPIDescription(conjunc))) {    		
    		//PelletExplanation exp = new PelletExplanation(pellet);
    		
    		//System.out.println("Unsatisfiable expression: \"" + conjunc + " --> " + pellet.isSatisfiable(OWLAPIConverter.getOWLAPIDescription(conjunc)));
    		//System.out.println("Explanation: " + exp.getUnsatisfiableExplanation(OWLAPIConverter.getOWLAPIDescription(conjunc)));
    		
    		return 0;
    	}
    	
    	Set<NamedKBElement> flattenD1 = Orthogonality.flattenDescription(des1);
    	Set<NamedKBElement> flattenD2 = Orthogonality.flattenDescription(des2);
    	
    	Set<NamedKBElement> subclassesD1 = new HashSet<NamedKBElement>();
    	Set<NamedKBElement> subclassesD2 = new HashSet<NamedKBElement>();
    	
    	OWLClass nothing = (OWLClass)reasoner.getBottomClassNode().getEntities().toArray()[0];    	
    	
    	for (NamedKBElement d1 : flattenD1) {
    		if (d1 instanceof NamedClass) {    			
    			Set<OWLClass> subClasses = this.getsubClasses((NamedClass)d1);   
    			subClasses.remove(nothing);
    			
    			if (subClasses.size() == 0)
    				subclassesD1.add(d1);
    			
    			for (OWLClass c : subClasses) 
    				subclassesD1.add(new NamedClass(c.getIRI().toString()));    			
    		}
    		else 
    			subclassesD1.add((Property)d1);
    	}
    	
    	for (NamedKBElement d2 : flattenD2) {
    		if (d2 instanceof NamedClass) {    			
    			Set<OWLClass> subClasses = this.getsubClasses((NamedClass)d2);
    			subClasses.remove(nothing);

    			if (subClasses.size() == 0)
    				subclassesD2.add(d2);

    			for (OWLClass c : subClasses) 
    				subclassesD2.add(new NamedClass(c.getIRI().toString()));    			
    		}
    		else
    			subclassesD2.add((Property)d2);
    	}
    	
    	
    	int d1Size = subclassesD1.size();
    	int d2Size = subclassesD2.size();
    	
    	Set<NamedKBElement> commonElements = new HashSet<NamedKBElement>();
    	commonElements.addAll(subclassesD1);
    	commonElements.retainAll(subclassesD2);
    	
    	int noOfCommonElements = commonElements.size();
    	    	
    	//System.out.println("Set 1 (" + subclassesD1.size() + "): " + subclassesD1);
    	//System.out.println("Set 2 (" + subclassesD2.size() + "): " + subclassesD2);

        	
    	//subclassesD1.removeAll(subclassesD2);
    	//int noOfCommonElements = d1Size - subclassesD1.size();
    	
    	//if (noOfCommonElements != commonElements.size()) 
    	//	throw new Exception("jaccard similarity calculation error: intersection works wrong");
    	
    	    	
    	//process the equivalent classes
    	//Set<OWLClass> equivClassess = new TreeSet<OWLClass>();
    	for (NamedKBElement c : subclassesD1) {
    		if (c instanceof NamedClass) {
    			Set<OWLClass> equivClassessTmp = reasoner.getEquivalentClasses(OWLAPIConverter.getOWLAPIDescription((NamedClass)c)).getEntities();
    			
    			if (equivClassessTmp.size() > 1) {
    				Set<NamedClass> tmp = new HashSet<NamedClass>();
    				
    				//convert equiv class into NamedClass(es)
    				for (OWLClass ec : equivClassessTmp)
    					tmp.add(new NamedClass(ec.getIRI().toString()));
    				
    				tmp.removeAll(subclassesD2);
    				if (tmp.size() < equivClassessTmp.size())
    					for (OWLClass ec : equivClassessTmp)        					
    						commonElements.add(new NamedClass(ec.getIRI().toString()));
    			}
    			
    		}
    	}
    	
    	//System.out.println("Common elements (" + commonElements.size() + "): " + commonElements);
    	
    	double score = (commonElements.size())/(double)(d1Size + d2Size - noOfCommonElements);
    	
    	return score;
    }
    
    
    
    /**
     * Get Jaccard overlap (score) between two concepts in normal form.
     * Step: i) get all concepts, ii) get all roles, iii) call function for computation of concept similarity, 
     * 			iv) call function for computation of role similarity, v) combine the results. 
     * 
     * @return
     * @throws Exception 
     */
    public double getJaccardSimilarityComplex(OWLClassExpression des1, OWLClassExpression des2) throws Exception {
    	  	
    	OWLClassExpression conjunc = df.getOWLObjectIntersectionOf(des1, des2);

    	if (!reasoner.isSatisfiable(conjunc))    {
    		//PelletExplanation exp = new PelletExplanation(pellet);
    		
    		//System.out.println("Unsatisfiable expression: \"" + conjunc + " --> " + pellet.isSatisfiable(OWLAPIConverter.getOWLAPIDescription(conjunc)));
    		//System.out.println("Explanation: " + exp.getUnsatisfiableExplanation(OWLAPIConverter.getOWLAPIDescription(conjunc)));
    		
    		return 0; 
    	}
    	
    	
    	//get all concepts and roles
    	Set<OWLClassExpression> conceptD1 = ConceptSimilarity.primSet(des1);
    	Set<OWLClassExpression> conceptD2 = ConceptSimilarity.primSet(des2);
    	
    	Set<OWLPropertyExpression> allPropertiesD1 = ConceptSimilarity.getPropertySet(des1);
    	Set<OWLPropertyExpression> allPropertiesD2 = ConceptSimilarity.getPropertySet(des1);
    	
    	
    	//separate object and datatype properties
    	Set<OWLPropertyExpression> objPropertiesD1 = new HashSet<>();
    	Set<OWLPropertyExpression> objPropertiesD2 = new HashSet<>();
    	   	
    	for (OWLPropertyExpression p : allPropertiesD1)
    		if (!p.isAnonymous())
    			objPropertiesD1.add(p);
    	
    	for (OWLPropertyExpression p : allPropertiesD2)
			if (!p.isAnonymous())
    			objPropertiesD2.add(p);

    	
    	//calculate the similarity for concepts
    	double totalConceptSim = 0;
    	
    	if (conceptD1.size() == 0)			//if a description has no primitive concept, it is considered as TOP
    		conceptD1.add(df.getOWLThing());
    	if (conceptD2.size() == 0)
    		conceptD2.add(df.getOWLThing());
    	
    	for (OWLClassExpression d1 : conceptD1) {
    		for (OWLClassExpression d2 : conceptD2) {
    			totalConceptSim += simPrim(d1, d2);
    		}	//for each flattenD2
    	}	//for each flattenD1
    	
    	//concept similarity = avg. of the total similarity between concepts between two descriptions
    	double conceptScore = totalConceptSim / (conceptD1.size() * conceptD2.size());
    	
    	
    	//calculate the similarity for roles    	
    	Set<OWLPropertyExpression> commonObjProperties = new HashSet<>();
    	commonObjProperties.addAll(objPropertiesD1);
    	commonObjProperties.retainAll(objPropertiesD2);
    	    	
    	double roleScore = 0;    	
    	if (commonObjProperties.size() > 0) {
    		
    		double totalRoleSim = 0;
    		//for each common property of the descriptions, calculate the similarity between their range
    		for (OWLPropertyExpression pro : commonObjProperties) {
				OWLClassExpression rangeD1 = ConceptSimilarity.val(pro, des1);		//get the range of the property
				OWLClassExpression rangeD2 = ConceptSimilarity.val(pro, des2);
    			
    			
    			//get the concepts in the range of the properties
    			Set<OWLClassExpression> conceptValD1 = ConceptSimilarity.primSet(rangeD1);
    	    	Set<OWLClassExpression> conceptValD2 = ConceptSimilarity.primSet(rangeD2);
    	    	
    	    	//calculate the similarity for concepts
    	    	double roleValSim = 0;
    	    	if ((conceptValD1.size() > 0) && (conceptValD2.size() > 0)) {	//when is this false???
	    	    	for (OWLClassExpression d1 : conceptValD1) {
	    	    		for (OWLClassExpression d2 : conceptValD2) {
	    	    			roleValSim += simPrim(d1, d2);
	    	    		}	//for each flattenD2
	    	    	}	//for each flattenD1
	    	    	
	    	    	roleValSim /= conceptValD1.size() * conceptValD2.size();
    	    	}
    	    	
    			
    			//double roleValSim = this.getJaccardSimilarityComplex(rangeD1, rangeD2);
    	    	    	    	
    	    	totalRoleSim += roleValSim; 
    			
    		}  //for each common object property
    		
    		roleScore = totalRoleSim / commonObjProperties.size();
    		
    	}	//if there exists common property  
    	
    	//return (simCon + simRole)/alpha;
    	
    	if (commonObjProperties.size() > 0)
    		return (conceptScore + roleScore)/2d;
    	else 
    		return conceptScore;
    }
    
    
    
    /**
     * Compute jaccard similarity between two disjunctive normal concepts
     * 1. normalise
     * 2. compute max of the overlap
     * 
     * @param d1
     * @param d2
     * @return
     * @throws Exception 
     */
    public double getJaccardDisjunctiveSimilarity(OWLClassExpression d1, OWLClassExpression d2) throws Exception {
		OWLClassExpression normalisedC = FortificationUtils.normalise(d1);
		OWLClassExpression normalisedD = FortificationUtils.normalise(d2);
    	
    	List<OWLClassExpression> flattenedC = FortificationUtils.flattenDisjunctiveNormalDescription(normalisedC);
    	List<OWLClassExpression> flattenedD = FortificationUtils.flattenDisjunctiveNormalDescription(normalisedD);
    	
    	
    	double maxJSim = 0;
    	for (OWLClassExpression c : flattenedC) {
    		for (OWLClassExpression d : flattenedD) {
    			double sim = this.getJaccardSimilarityComplex(c, d);
    			if (maxJSim < sim)
    				maxJSim = sim;
    		}
    	}
    	
    	return maxJSim;
    }
    
    /**
     * Calculate the Jaccard similarity between two primitive concepts
     * 
     * @param d1
     * @param d2
     * 
     * @return Jaccard similarity between d1 and d2
     * @throws Exception 
     */
    public double simPrim(OWLClassExpression d1, OWLClassExpression d2) throws Exception {
    	//if (!(d1 instanceof NamedClass) || !(d2 instanceof NamedClass))
    	//	throw new Exception("simPrim() requires two NamedClass (OWLClass) objects (d1: " + d1.getClass() + ", d2: " + d2.getClass());
    	
    	if (d1 instanceof OWLObjectComplementOf)
    		return 1 - simPrim(((OWLObjectComplementOf) d1).getOperand(), d2);
    	
    	if (d2 instanceof OWLObjectComplementOf)
    		return 1 - simPrim(d1, ((OWLObjectComplementOf) d2).getOperand());
    	
    	Set<OWLClassExpression> subClassesD1 = getsubClasses(d1);
    	Set<OWLClassExpression> subClassesD2 = getsubClasses(d2);
    	
		if (subClassesD1.size() == 0)
			subClassesD1.add(d1);
		
		if (subClassesD2.size() == 0)
			subClassesD2.add(d2);
			
		int d1Size = subClassesD1.size();
    	int d2Size = subClassesD2.size();

		Set<OWLClassExpression> commonElements = new HashSet<>(subClassesD1);
    	commonElements.retainAll(subClassesD2);
    	
    	//common element without equivalent
    	int noOfCommonElements = commonElements.size();
    	
    	//System.out.println("Set 1 (" + subClassesD1.size() + "): " + subClassesD1);
    	//System.out.println("Set 2 (" + subClassesD2.size() + "): " + subClassesD2);
		
	
	   	//process the equivalent classes
    	//if a set of equivalent classes 
    	for (OWLClassExpression c : subClassesD1) {
    		Set<OWLClass> equivClassessTmp = reasoner.getEquivalentClasses(c).getEntities();
    			
    		if (equivClassessTmp.size() > 1) {

				Set<OWLClass> tmp = new HashSet<>(equivClassessTmp);
    			tmp.removeAll(subClassesD2);
    				
    			if (tmp.size() < equivClassessTmp.size()) {
					commonElements.addAll(equivClassessTmp);
    			}
    			
    		}
    	}	//for (process equivalent classes
    	    	
    	double score = (commonElements.size())/(double)(d1Size + d2Size - noOfCommonElements);
    	
    	//System.out.println("Common elements (" + commonElements.size() + "): " + commonElements);
    	//System.out.println("simPrim (" + d1 + ", " + d2 + ") = " + score);
    	
    	return score;
    }
    
    
    /**
     * 
     * @param pro1
     * @param pro2
     * @return
     */
    /*
    public double simRoles(PropertyExpression pro1, PropertyExpression pro2) {
    	
    	return 0;
    }
    */
    /**
     * Get Jaccard distance of two descriptions (= 1 - jaccard overlap(d1, d2))
     * 
     * @param des1
     * @param des2
     * @return
     * @throws Exception
     */
    /*
    public double getJaccardDistance(Description des1, Description des2) throws Exception {
    	return 1 - getJaccardSimilaritySimple(des1, des2); 
    }
    */
    
}

