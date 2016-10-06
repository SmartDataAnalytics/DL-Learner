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
package org.dllearner.core;

import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.Hierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;

/**
 * Reasoning requests related to the schema of the knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public interface SchemaReasoner {
	
	/**
	 * Returns all named classes, which are not satisfiable, i.e. cannot 
	 * have instances.
	 * @return The set of inconsistent classes.
	 */
	Set<OWLClass> getInconsistentClasses();
	
	/**
	 * Returns the domain of this object property. (Theoretically, there could
	 * be more than one domain axiom. However, this can be considered a modelling
	 * error.)
	 * @param objectProperty An object property in the knowledge base.
	 * @return The rdfs:domain of <code>objectProperty</code>
	 */
	OWLClassExpression getDomain(OWLObjectProperty objectProperty);
	
	/**
	 * Returns the domain of this data property.
	 * @param datatypeProperty An data property in the knowledge base.
	 * @return The rdfs:domain of <code>datatypeProperty</code>
	 */
	OWLClassExpression getDomain(OWLDataProperty datatypeProperty);
	
	/**
	 * Returns the range of this object property.
	 * @param objectProperty An object property in the knowledge base.
	 * @return The rdfs:range of <code>objectProperty</code>
	 */
	OWLClassExpression getRange(OWLObjectProperty objectProperty);
	
	/**
	 * Returns the range of this data property.
	 * @param datatypeProperty An data property in the knowledge base.
	 * @return The rdfs:range of <code>datatypeProperty</code>
	 */
	OWLDataRange getRange(OWLDataProperty datatypeProperty);
	
	/**
	 * Checks whether <code>superClass</code> is a super class of <code>subClass</code>.
	 * @param superClass The (supposed) super class.
	 * @param subClass The (supposed) sub class.
	 * @return Whether <code>superClass</code> is a super class of <code>subClass</code>.
	 */
	boolean isSuperClassOf(OWLClassExpression superClass, OWLClassExpression subClass);
	
	/**
	 * Checks whether <code>class1</code> is equivalent to <code>class2</code>.
	 * @param class1 The first class.
	 * @param class2 The second class2.
	 * @return Whether <code>class1</code> is equivalent to <code>class2</code>.
	 */
	boolean isEquivalentClass(OWLClassExpression class1, OWLClassExpression class2);
	
	/**
	 * Checks whether <code>class1</code> is disjoint with <code>class2</code>.
	 * @param class1 The first class.
	 * @param class2 The second class2.
	 * @return Whether <code>class1</code> is disjoint with <code>class2</code>.
	 */
	boolean isDisjoint(OWLClass class1, OWLClass class2);
		
	/**
	 * Returns all asserted owl:equivalence class axioms for the given class.
	 * @param namedClass A named class in the background knowledge.
	 * @return A set of descriptions asserted to be equal to the named class.
	 */
	Set<OWLClassExpression> getAssertedDefinitions(OWLClass namedClass);
	
	/**
	 * Checks which of <code>superClasses</code> are super classes of <code>subClass</code>
	 * @param superClasses A set of (supposed) super classes.
	 * @param subClasses The (supposed) sub class.
	 * @return The subset of <code>superClasses</code>, which satisfy the superclass-subclass relationship.
	 */
	Set<OWLClassExpression> isSuperClassOf(Set<OWLClassExpression> superClasses, OWLClassExpression subClasses);

	/**
	 * Computes and returns the class hierarchy of the knowledge base.
	 *
	 * @return The subsumption hierarchy of this knowledge base.
	 */
	Hierarchy<OWLClassExpression> getClassHierarchy();
	
	/**
	 * Returns direct super classes in the class hierarchy.
	 * 
	 * @param description
	 *            Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression description);

	/**
	 * Returns direct sub classes in the class hierarchy.
	 * 
	 * @param description
	 *            Atomic concept, top, or bottom.
	 * @return A set of more special concepts.
	 */
	SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression description);

	/**
	 * Computes and returns the object property hierarchy of the knowledge base.
	 * @return The object property hierarchy of the knowlege base.
	 */
	ObjectPropertyHierarchy getObjectPropertyHierarchy();
	
	/**
	 * Returns more general concepts in the subsumption hierarchy.
	 * 
	 * @see ObjectPropertyHierarchy#getMoreGeneralRoles(OWLObjectProperty)
	 * @param objectProperty
	 *            Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	SortedSet<OWLObjectProperty> getSuperProperties(OWLObjectProperty objectProperty);

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @see ObjectPropertyHierarchy#getMoreSpecialRoles(OWLObjectProperty)
	 * @param objectProperty
	 *            Atomic concept, top, or bottom.
	 * @return A set of more special concepts.
	 */
	SortedSet<OWLObjectProperty> getSubProperties(OWLObjectProperty objectProperty);

	/**
	 * TODO Outdated in OWL 2, because the universal role is the most general.
	 * @see ObjectPropertyHierarchy#getMostGeneralRoles()
	 * @return The most general roles.
	 */
	SortedSet<OWLObjectProperty> getMostGeneralProperties();

	/**
	 * TODO Outdated in OWL, because the bottom role is the most specific.
	 * @see ObjectPropertyHierarchy#getMostSpecialRoles()
	 * @return The most special roles.
	 */
	SortedSet<OWLObjectProperty> getMostSpecialProperties();

	/**
	 * Computes and returns the data property hierarchy of the knowledge base.
	 * @return The data property hierarchy of the knowlege base.
	 */
	DatatypePropertyHierarchy getDatatypePropertyHierarchy();
	
	/**
	 * Returns more general concepts in the subsumption hierarchy.
	 * 
	 * @see DatatypePropertyHierarchy#getMoreGeneralRoles(OWLDataProperty)
	 * @param dataProperty
	 *            Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	SortedSet<OWLDataProperty> getSuperProperties(OWLDataProperty dataProperty);

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @see DatatypePropertyHierarchy#getMoreSpecialRoles(OWLDataProperty)
	 * @param dataProperty
	 *            Atomic concept, top, or bottom.
	 * @return A set of more special concepts.
	 */
	SortedSet<OWLDataProperty> getSubProperties(OWLDataProperty dataProperty);

	/**
	 * @see DatatypePropertyHierarchy#getMostGeneralRoles()
	 * @return The most general roles.
	 */
	SortedSet<OWLDataProperty> getMostGeneralDatatypeProperties();

	/**
	 * @see DatatypePropertyHierarchy#getMostSpecialRoles()
	 * @return The most special roles.
	 */
	SortedSet<OWLDataProperty> getMostSpecialDatatypeProperties();

	/**
	 * Computes all super properties for the given property.
	 * @param property the property
	 * @return all super properties
	 */
	<T extends OWLProperty> SortedSet<T> getSuperProperties(T property);

	/**
	 * Computes all sub properties for the given property.
	 * @param property the property
	 * @return all sub properties
	 */
	<T extends OWLProperty> SortedSet<T> getSubProperties(T property);
	
}
