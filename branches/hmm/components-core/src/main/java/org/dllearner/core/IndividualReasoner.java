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

package org.dllearner.core;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.utilities.datastructures.SortedSetTuple;

/**
 * Reasoning requests/queries related to individuals in the knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public interface IndividualReasoner {

	/**
	 * Returns types of an individual, i.e. those classes where the individual
	 * is instance of. For instance, the individual eric could have type Person. 
	 * 
	 * @param individual An individual in the knowledge base.
	 * @return Types this individual is instance of.
	 */
	public Set<NamedClass> getTypes(Individual individual);
	
	/**
	 * Checks whether <code>individual</code> is instance of <code>description</code>.
	 * For instance, "Leipzig" may be an instance of "City".
	 * 
	 * @param description An OWL class description.
	 * @param individual An individual.
	 * @return True if the instance has the description as type and false otherwise.
	 */
	public boolean hasType(Description description, Individual individual);
	
	/**
	 * Performs instance checks on a set of instances (reasoners might be more
	 * efficient than handling each check separately).
	 * @param description An OWL class description.
	 * @param individuals An individual.
	 * @return The subset of those instances, which have the given type.
	 */
	public SortedSet<Individual> hasType(Description description, Set<Individual> individuals);
	
	/**
	 * Gets all instances of a given class description in the knowledge base.
	 * @param description An OWL class description.
	 * @return All instances of the class description.
	 */
	public SortedSet<Individual> getIndividuals(Description description);
	
	/**
	 * Performs a query for all instances of the given class description and
	 * its negation. (Note that in OWL it is possible that the reasoner can
	 * neither deduce that an individual is instance of a class nor its 
	 * negation.) This method might be more efficient that performing a 
	 * two retrievals.
	 * 
	 * @param description An OWL class description.
	 * @return All instances of the class description and its negation.
	 */
	public SortedSetTuple<Individual> doubleRetrieval(Description description);	
	
	/**
	 * Returns the set of individuals, which are connect to the given individual
	 * with the specified object property.
	 * @param individual An individual, e.g. eric.
	 * @param objectProperty An object property, e.g. hasChild.
	 * @return A set of individuals, e.g. {anna, maria}.
	 */
	public Set<Individual> getRelatedIndividuals(Individual individual,
			ObjectProperty objectProperty);
	
	/**
	 * Returns the set of individuals, which are connect to the given individual
	 * with the specified data property.
	 * @param individual An individual, e.g. eric.
	 * @param datatyoeProperty A data property, e.g. hasIncome.
	 * @return A set of individuals, e.g. {48000^^xsd:int}.
	 */	
	public Set<Constant> getRelatedValues(Individual individual, DatatypeProperty datatypeProperty);
	
	/**
	 * A map of properties related to an individual, e.g. 
	 * {hasChild => {eric,anna}, hasSibling => {sebastian}}.
	 * 
	 * @param individual An individual.
	 * @return A map of of properties connected to the individual as keys and the individuals
	 * they point to as values.
	 */
	public Map<ObjectProperty,Set<Individual>> getObjectPropertyRelationships(Individual individual); 
	
	/**
	 * Computes and returns all connections between individuals through the specified
	 * property, e.g. {eric => {maria, anna}, anna => {eric}}.
	 * @param objectProperty An object property.
	 * @return The mapping of individuals to other individuals through this object property.
	 */
	public Map<Individual, SortedSet<Individual>> getPropertyMembers(ObjectProperty objectProperty);

	/**
	 * Computes and returns all connections between individuals and values through the
	 * specified property, e.g. {eric => {48000^^xsd:int}, sarah => {56000^^xsd:int}}.
	 * @param datatypeProperty  A data property.
	 * @return The mapping between individuals and values through the given property.
	 */
	public Map<Individual, SortedSet<Constant>> getDatatypeMembers(DatatypeProperty datatypeProperty);
	
	/**
	 * Convenience method, which can be used if it is known that the property has 
	 * values which can be parsed as double.
	 * @see #getDatatypeMembers(DatatypeProperty)
	 * @see Double#valueOf(String)
	 * @param datatypeProperty A data property.
	 * @return The mapping between individuals and double values through the given property.
	 */
	public Map<Individual, SortedSet<Double>> getDoubleDatatypeMembers(DatatypeProperty datatypeProperty);
	
	/**
	 * Convenience method, which can be used if it is known that the property has 
	 * values which can be parsed as integer.
	 * @see #getDatatypeMembers(DatatypeProperty)
	 * @see Integer#valueOf(String)
	 * @param datatypeProperty A data property.
	 * @return The mapping between individuals and integer values through the given property.
	 */
	public Map<Individual, SortedSet<Integer>> getIntDatatypeMembers(DatatypeProperty datatypeProperty);
	
	/**
	 * Convenience method, which can be used if it is known that the property has 
	 * values which can be parsed as boolean value. Only "true" or "false" are 
	 * accepted. If other values occur, a warning will be issued.
	 * @see #getDatatypeMembers(DatatypeProperty)
	 * @param datatypeProperty A data property.
	 * @return The mapping between individuals and boolean values through the given property.
	 */
	public Map<Individual, SortedSet<Boolean>> getBooleanDatatypeMembers(DatatypeProperty datatypeProperty);

	/**
	 * Convenience method, which can be used to get all individuals, which have value
	 * "true" for the given property. Usually, data properties can have several values
	 * for a given individual, but this method will throw a runtime exception if this
	 * is the case (i.e. the set of values is {"true", "false"}). 
	 * @see #getDatatypeMembers(DatatypeProperty)
	 * @param datatypeProperty A data property.
	 * @return The set of individuals for which the boolean property holds.
	 */
	public SortedSet<Individual> getTrueDatatypeMembers(DatatypeProperty datatypeProperty);
	
	/**
	 * Convenience method, which can be used to get all individuals, which have value
	 * "false" for the given property. Usually, data properties can have several values
	 * for a given individual, but this method will throw a runtime exception if this
	 * is the case (i.e. the set of values is {"true", "false"}).
	 * @see #getDatatypeMembers(DatatypeProperty)
	 * @param datatypeProperty A data property.
	 * @return The set of individuals for which the boolean property does not hold.
	 */
	public SortedSet<Individual> getFalseDatatypeMembers(DatatypeProperty datatypeProperty);

	/**
	 * Convenience method, which can be used which returns the property values as
	 * strings (note that any literal can be represented as string, even numbers).
	 * @see #getDatatypeMembers(DatatypeProperty)
	 * @param datatypeProperty A data property.
	 * @return The mapping between individuals and string values through the given property.
	 */
	public Map<Individual, SortedSet<String>> getStringDatatypeMembers(DatatypeProperty datatypeProperty);
	
}
