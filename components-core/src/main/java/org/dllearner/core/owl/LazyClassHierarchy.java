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
import java.util.TreeMap;

import org.dllearner.core.AbstractReasonerComponent;
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
public class LazyClassHierarchy extends ClassHierarchy {

	public static Logger logger = LoggerFactory.getLogger(LazyClassHierarchy.class);
	
	private static final OWLClass OWL_THING = new OWLClassImpl(
            OWLRDFVocabulary.OWL_THING.getIRI());
    private static final OWLClass OWL_NOTHING = new OWLClassImpl(
            OWLRDFVocabulary.OWL_NOTHING.getIRI());
    
    private OWLDataFactory df = new OWLDataFactoryImpl(false, false);

	private AbstractReasonerComponent rc;
	
	public LazyClassHierarchy(AbstractReasonerComponent rc) {
		super(new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>(), new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>());
		this.rc = rc;
	}

	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept) {
		return getSuperClasses(concept, false);
	}
	
	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept, boolean direct) {
		return rc.getSuperClasses(concept);
	}

	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept) {
		return getSubClasses(concept, false);
	}
	
	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept, boolean direct) {
		return rc.getSubClasses(concept);
	}

	public SortedSet<OWLClassExpression> getSiblingClasses(OWLClassExpression concept) {
		return getSiblings(concept);
	}
	
	public boolean isSubclassOf(OWLClass subClass, OWLClass superClass) {
		return isSubclassOf((OWLClassExpression)subClass, (OWLClassExpression)superClass);
	}
	
	public boolean isSubclassOf(OWLClassExpression subClass, OWLClassExpression superClass) {
		if (subClass.equals(superClass)) {
			return true;
		} else {
			SortedSet<OWLClassExpression> parents = getSuperClasses(subClass);
			
			if(parents != null){
				// search the upper classes of the subclass
				for (OWLClassExpression parent : parents) {
					if (isChildOf(parent, superClass)) {
						return true;
					}
				}
			}
			// we cannot reach the class via any of the upper classes,
			// so it is not a super class
			return false;
		}
	}
	
	@Override
	public LazyClassHierarchy clone() {
		return new LazyClassHierarchy(rc);		
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
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#cloneAndRestrict(java.util.Set)
	 */
	@Override
	public AbstractHierarchy<OWLClassExpression> cloneAndRestrict(Set<OWLClassExpression> allowedEntities) {
		return new LazyClassHierarchy(rc);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#thinOutSubsumptionHierarchy()
	 */
	@Override
	public void thinOutSubsumptionHierarchy() {
	}
}
