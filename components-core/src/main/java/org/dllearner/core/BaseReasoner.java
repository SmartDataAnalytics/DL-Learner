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

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;

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
	boolean isSatisfiable();
	
	/**
	 * Checks whether adding the specified axiom leads to an inconsistency.
	 * @param axiom The axiom to be added to the knowledge base.
	 * @return True of the knowledge base including the axiom is satisfiable. False otherwise.
	 */
	boolean remainsSatisfiable(OWLAxiom axiom);
	
	/**
	 * Gets all named classes in the knowledge base, e.g. Person, City, Car.
	 * @return All named classes in KB.
	 */
	Set<OWLClass> getClasses();
	
	/**
	 * Gets all object properties in the knowledge base, e.g. hasChild, isCapitalOf, hasEngine.
	 * @return All object properties in KB.
	 */
	Set<OWLObjectProperty> getObjectProperties();
	
	/**
	 * Gets all data properties in the knowledge base, e.g. hasIncome, height.
	 * @return All data properties in KB.
	 */
	Set<OWLDataProperty> getDatatypeProperties();
	
	/**
	 * Gets all data properties with range xsd:boolean.
	 * @return Boolean data properties in KB.
	 */
	Set<OWLDataProperty> getBooleanDatatypeProperties();
	
	/**
	 * Gets all data properties with a range that describes floating point values, i.e. 
	 * xsd:float, xsd:double and xsd:decimal.
	 * @return Floating point data properties in KB.
	 */
	Set<OWLDataProperty> getDoubleDatatypeProperties();
	
	/**
	 * Gets all data properties with a numeric range 
	 * @return Numeric data properties in KB.
	 */
	Set<OWLDataProperty> getNumericDataProperties();
	
	/**
	 * Gets all integer type data properties, i.e. with range 
	 * xsd:byte, xsd:short, xsd:int, xsd:integer, 
	 * xsd:negativeInteger, xsd:nonNegativeInteger,
	 * xsd:positiveInteger, xsd:nonPositiveInteger.
	 * @see org.dllearner.utilities.OWLAPIUtils#intDatatypes
	 * @return Integer data properties in KB.
	 */
	Set<OWLDataProperty> getIntDatatypeProperties();
	
	/**
	 * Gets all data properties with range xsd:string.
	 * TODO We could extend this to all types, which can be parsed into
	 * strings and even include the properties without any specified datatype.
	 * @see OWLDataProperty
	 * @return String data properties in KB.
	 */
	Set<OWLDataProperty> getStringDatatypeProperties();
	
	/**
	 * Gets all individuals in the knowledge base, e.g. Eric, London, Car829. 
	 * @return All individuals in KB.
	 */
	SortedSet<OWLIndividual> getIndividuals();

	/**
	 * Returns the base URI of the knowledge base. If several knowledge sources are
	 * used, we only pick one of their base URIs.
	 * @return The base URI, e.g. http://dbpedia.org/resource/.
	 */
	String getBaseURI();
	
	/**
	 * Returns the prefixes used in the knowledge base, e.g. foaf for
	 * foaf: <http://xmlns.com/foaf/0.1/>. If several knowledge sources are used,
	 * their prefixes are merged. (In case a prefix is defined twice with different
	 * values, we pick one of those.)
	 * @return The prefix mapping.
	 */
	Map<String, String> getPrefixes();
	
	/**
	 * Returns the RDFS labels of an entity.
	 * @param entity An entity, e.g. Machine.
	 * @return All values of rdfs:label for the entity, e.g. {"Machine"@en, "Maschine"@de}. 
	 */
	Set<OWLLiteral> getLabel(OWLEntity entity);
	
}
