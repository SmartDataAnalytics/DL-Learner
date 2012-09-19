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

import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;

/**
 * Contains the following reasoning/query operations:
 * <ul>
 *   <li>queries for elements contained in the knowledge base (classes, properties, ...)</li>
 *   <li>basic reasoning requests related to the knowledge base as a whole (e.g. consistency)</li>
 * </ul>
 * (Many methods in this interface do not require reasoning algorithms, but rather
 * return information about the knowledge base.)
 * 
 * @author Jens Lehmann
 *
 */
public interface BaseReasoner {

	/**
	 * Checks consistency of the knowledge.
	 * @return True if the knowledge base is consistent and false otherwise.
	 */
	public boolean isSatisfiable();
	
	/**
	 * Checks whether adding the specified axiom leads to an inconsistency.
	 * @param axiom The axiom to be added to the knowledge base.
	 * @return True of the knowledge base including the axiom is satisfiable. False otherwise.
	 */
	public boolean remainsSatisfiable(Axiom axiom);
	
	/**
	 * Gets all named classes in the knowledge base, e.g. Person, City, Car.
	 * @return All named classes in KB.
	 */
	public Set<NamedClass> getNamedClasses();
	
	/**
	 * Gets all object properties in the knowledge base, e.g. hasChild, isCapitalOf, hasEngine.
	 * @return All object properties in KB.
	 */
	public Set<ObjectProperty> getObjectProperties();
	
	/**
	 * Gets all data properties in the knowledge base, e.g. hasIncome, height.
	 * @return All data properties in KB.
	 */
	public SortedSet<DatatypeProperty> getDatatypeProperties();
	
	/**
	 * Gets all data properties with range xsd:boolean.
	 * @see org.dllearner.core.owl.Datatype#BOOLEAN
	 * @return Boolean data properties in KB.
	 */
	public SortedSet<DatatypeProperty> getBooleanDatatypeProperties();
	
	/**
	 * Gets all data properties with range xsd:double.
	 * TODO We could extend this to all types, which can be parsed into
	 * a double value, e.g. floats.
	 * @see org.dllearner.core.owl.Datatype#DOUBLE
	 * @return Double data properties in KB.
	 */
	public SortedSet<DatatypeProperty> getDoubleDatatypeProperties();
	
	/**
	 * Gets all data properties with range xsd:int.
	 * TODO We could extend this to all types, which can be parsed into
	 * Integers, e.g. xsd:integer, xsd:negativeInteger, xsd:nonNegativeInteger etc.
	 * @see org.dllearner.core.owl.Datatype#INT
	 * @return Integer data properties in KB.
	 */
	public SortedSet<DatatypeProperty> getIntDatatypeProperties();
	
	/**
	 * Gets all data properties with range xsd:string.
	 * TODO We could extend this to all types, which can be parsed into
	 * strings and even include the properties without any specified datatype.
	 * @see org.dllearner.core.owl.Datatype#String
	 * @return String data properties in KB.
	 */
	public SortedSet<DatatypeProperty> getStringDatatypeProperties();	
	
	/**
	 * Gets all individuals in the knowledge base, e.g. Eric, London, Car829. 
	 * @return All individuals in KB.
	 */	
	public SortedSet<Individual> getIndividuals();

	/**
	 * Returns the base URI of the knowledge base. If several knowledge sources are
	 * used, we only pick one of their base URIs.
	 * @return The base URI, e.g. http://dbpedia.org/resource/.
	 */
	public String getBaseURI();
	
	/**
	 * Returns the prefixes used in the knowledge base, e.g. foaf for
	 * foaf: <http://xmlns.com/foaf/0.1/>. If several knowledge sources are used,
	 * their prefixes are merged. (In case a prefix is defined twice with different
	 * values, we pick one of those.)
	 * @return The prefix mapping.
	 */
	public Map<String, String> getPrefixes();
	
	/**
	 * Returns the RDFS labels of an entity.
	 * @param entity An entity, e.g. Machine.
	 * @return All values of rdfs:label for the entity, e.g. {"Machine"@en, "Maschine"@de}. 
	 */
	public Set<Constant> getLabel(Entity entity);
	
}
