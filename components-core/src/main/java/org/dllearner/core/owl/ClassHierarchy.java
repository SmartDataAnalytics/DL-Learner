/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    
    private OWLDataFactory df = new OWLDataFactoryImpl();
    
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
	 * Returns the all superclasses for the given class.
	 * @param concept the class
	 * @return all superclasses
	 */
	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept) {
		return getSuperClasses(concept, false);
	}

	/**
	 * Returns the all superclasses for the given class.
	 * @param concept the class
	 * @param direct whether to return only direct superclasses or not
	 * @return all superclasses
	 */
	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept, boolean direct) {
		if(concept.isOWLThing()) {
			return new TreeSet<>();
		}
		return getParents(concept, direct);
	}

	/**
	 * Returns the all subclasses.
	 * @param concept the class
	 * @return all subclasses
	 */
	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept) {
		return getSubClasses(concept, false);
	}

	/**
	 * Returns the all subclasses.
	 * @param concept the class
	 * @param direct whether to return only direct subclasses or not
	 * @return all subclasses
	 */
	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept, boolean direct) {
		if(concept.isOWLNothing()) {
			return new TreeSet<>();
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
		return new ClassHierarchy(new TreeMap<>(getHierarchyUp()), new TreeMap<>(getHierarchyDown()));
	}

	/**
	 * Converts the class hierarchy to a set of subclass axioms.
	 *
	 * @return a set of subclass axioms
	 */
	public Set<OWLAxiom> toOWLAxioms() {
		return getEntities().stream().flatMap(cls ->
				Optional.ofNullable(getChildren(cls)).map(Collection::stream).orElseGet(Stream::empty)
						.map(sub -> df.getOWLSubClassOfAxiom(sub, cls))
		).collect(Collectors.toSet());
	}

	/**
	 * Converts the class hierarchy starting from the given concept <code>ce</code> to a set of subclass axioms.
	 *
	 * @param ce the root
	 * @return a set of subclass axioms
	 */
	public Set<OWLAxiom> toOWLAxioms(OWLClassExpression ce) {
		Set<OWLAxiom> axioms = new HashSet<>();
		Set<OWLClassExpression> visited = new HashSet<>();
		Set<OWLClassExpression> subclasses = getChildren(ce);
		if (subclasses != null) {
			for (OWLClassExpression sub : subclasses) {
				axioms.add(df.getOWLSubClassOfAxiom(sub, ce));
				toOWLAxioms(sub, axioms, visited);
			}
		}
		return axioms;
	}

	private void toOWLAxioms(OWLClassExpression ce, Set<OWLAxiom> axioms, Set<OWLClassExpression> visited){
		visited.add(ce);

		Optional.ofNullable(getChildren(ce)).map(Collection::stream).orElseGet(Stream::empty)
				.filter(cls -> !visited.contains(cls))
				.forEach(sub -> {
					axioms.add(df.getOWLSubClassOfAxiom(sub, ce));
					toOWLAxioms(sub, axioms, visited);
				});
	}
	
	public int getDepth2Root(OWLClassExpression concept){
		SortedSet<OWLClassExpression> superClasses = getParents(concept);
		int depth = 1;
		if(superClasses != null){
			depth += superClasses.stream().mapToInt(this::getDepth2Root).max().getAsInt();
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
