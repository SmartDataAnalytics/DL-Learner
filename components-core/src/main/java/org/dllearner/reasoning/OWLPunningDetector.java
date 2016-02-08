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
package org.dllearner.reasoning;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Sets;

import org.semanticweb.owlapi.model.parameters.Imports;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/**
 * OWL 1 DL required a strict separation between the names of, e.g., classes and individuals. 
 * OWL 2 DL relaxes this separation somewhat to allow different uses of the same term, 
 * e.g., Eagle, to be used for both a class, the class of all Eagles, and an individual, 
 * the individual representing the species Eagle belonging to the (meta)class of all plant and
 *  animal species. However, OWL 2 DL still imposes certain restrictions: it requires that a name 
 *  cannot be used for both a class and a datatype and that a name can only be used for one kind 
 *  of property. The OWL 2 Direct Semantics treats the different uses of the same name as 
 *  completely separate, as is required in DL reasoners.
 * @author Lorenz Buehmann
 *
 */
public class OWLPunningDetector {
	
	enum PunningType {
		CLASS_INDIVIDUAL, PROPERTY_INDIVIDUAL, CLASS_PROPERTY 
	}
	
	/**
	 * This object property is used to connect individuals with classes that are also individuals, thus, lead to punning.
	 */
	public static final OWLObjectProperty punningProperty = new OWLObjectPropertyImpl(IRI.create("http://dl-learner.org/punning/relatedTo"));
	
	/**
	 * Checks whether the same IRI denotes both a class and an individual in the ontology.
	 * @param ontology the OWL ontology
	 * @param cls the OWL class
	 * @return TRUE if the class IRI is used also as individual, otherwise FALSE
	 */
	public static boolean hasPunning(OWLOntology ontology, OWLClass cls){
		return hasPunning(ontology, cls.getIRI());
	}
	
	/**
	 * Checks whether the ontology contains punning, i.e. entities declared as both, class and individual.
	 * @param ontology the OWL ontology
	 * @return TRUE if there is at least one entity that is both, class and individual, otherwise FALSE
	 */
	public static boolean hasPunning(OWLOntology ontology){
		Set<OWLClass> classes = ontology.getClassesInSignature(Imports.INCLUDED);
		Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature(Imports.INCLUDED);
		
		Set<IRI> classIRIs = new HashSet<>(classes.size());
		for (OWLClass cls : classes) {
			classIRIs.add(cls.getIRI());
		}
		
		Set<IRI> individualIRIs = new HashSet<>(classes.size());
		for (OWLNamedIndividual ind : individuals) {
			individualIRIs.add(ind.getIRI());
		}
		
		return Sets.intersection(classIRIs, individualIRIs).size() > 0;
	}
	
	/**
	 * Checks whether the same IRI denotes both a class and an individual in the ontology.
	 * @param ontology the OWL ontology
	 * @param iri the entity IRI
	 * @return TRUE if the IRI is used as both, class and individual, otherwise FALSE
	 */
	public static boolean hasPunning(OWLOntology ontology, IRI iri){
		boolean isClass = ontology.getClassesInSignature().contains(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(iri));
		boolean isIndividual = ontology.getIndividualsInSignature().contains(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri));
		return isClass && isIndividual;
	}
}
