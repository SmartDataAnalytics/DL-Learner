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

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Represents a class subsumption hierarchy (ignoring equivalent concepts).
 * 
 * @author Jens Lehmann
 * 
 */
public class ClassHierarchy extends AbstractHierarchy<OWLClassExpression> {

	public static Logger logger = LoggerFactory.getLogger(ClassHierarchy.class);
	
	private static final OWLClass OWL_THING = new OWLClassImpl(
            OWLRDFVocabulary.OWL_THING.getIRI());
    private static final OWLClass OWL_NOTHING = new OWLClassImpl(
            OWLRDFVocabulary.OWL_NOTHING.getIRI());
    
    private OWLDataFactory df = new OWLDataFactoryImpl(false, false);
    
	/**
	 * The arguments specify the superclasses and subclasses of each class. This
	 * is used to build the subsumption hierarchy. 
	 * @param subsumptionHierarchyUp Contains super classes for each class.
	 * @param subsumptionHierarchyDown Contains sub classes for each class.
	 */
	public ClassHierarchy(
			SortedMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyUp,
			SortedMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyDown) {
		super(subsumptionHierarchyUp, subsumptionHierarchyDown);
	}

	/**
	 * Returns the all super classes.
	 * @param concept
	 * @return
	 */
	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept) {
		return getSuperClasses(concept, false);
	}
	
	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept, boolean direct) {
		if(concept.isOWLThing()) {
			return new TreeSet<OWLClassExpression>();
		}
		return getParents(concept, direct);
	}

	/**
	 * Returns the all subclasses.
	 * @param concept
	 * @return
	 */
	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept) {
		return getSubClasses(concept, false);
	}
	
	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept, boolean direct) {
		if(concept.isOWLNothing()) {
			return new TreeSet<OWLClassExpression>();
		}
		return getChildren(concept, direct);
	}

	public SortedSet<OWLClassExpression> getSiblingClasses(OWLClassExpression concept) {
		return getSiblings(concept);
	}
	
	public boolean isSubclassOf(OWLClass subClass, OWLClass superClass) {
		return isChildOf(subClass, superClass);
	}
	
	public boolean isSubclassOf(OWLClassExpression subClass, OWLClassExpression superClass) {
		return isChildOf(subClass, superClass);
	}
	
	@Override
	public ClassHierarchy clone() {
		return new ClassHierarchy(getHierarchyUp(), getHierarchyDown());		
	}
	
	public Set<OWLAxiom> toOWLAxioms(){
		return toOWLAxioms(OWL_THING);
	}
	
	public Set<OWLAxiom> toOWLAxioms(OWLClassExpression concept){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<OWLClassExpression> subConcepts = getChildren(concept);
		if (subConcepts != null) {
			for (OWLClassExpression sub : subConcepts){
				axioms.add(df.getOWLSubClassOfAxiom(sub, concept));
				axioms.addAll(toOWLAxioms(sub));
			}
		}
		return axioms;
	}
	
	public int getDepth2Root(OWLClassExpression concept){
		SortedSet<OWLClassExpression> superClasses = getParents(concept);
		int depth = 0;
		if(superClasses != null){
			depth = 1;
			for(OWLClassExpression superClass : superClasses){
				depth += getDepth2Root(superClass);
			}
		}
		return depth;
	}
	
	public SortedSet<OWLClassExpression> getMostGeneralClasses(){
		return getRoots();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#getTopConcept()
	 */
	@Override
	public OWLClassExpression getTopConcept() {
		return OWL_THING;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#getBottomConcept()
	 */
	@Override
	public OWLClassExpression getBottomConcept() {
		return OWL_NOTHING;
	}
}
