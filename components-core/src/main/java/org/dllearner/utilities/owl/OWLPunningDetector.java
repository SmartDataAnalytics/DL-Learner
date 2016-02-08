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
package org.dllearner.utilities.owl;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLPunningDetector {
	
	/**
	 * This object property is used to connect individuals with classes that are also individuals, thus, lead to punning.
	 */
	public static final OWLObjectProperty punningProperty = 
			new OWLObjectPropertyImpl(IRI.create("http://dl-learner.org/punning/relatedTo"));
	
	/**
	 * Checks whether the class is also used as individual in the ontology.
	 * @param ontology the ontology
	 * @param cls the class
	 * @return whether the class is also used as individual in the ontology
	 */
	public static boolean hasPunning(OWLOntology ontology, OWLClass cls){
		return hasPunning(ontology, cls.getIRI());
	}
	
	/**
	 * Checks whether the same IRI denotes both a class and an individual in the ontology.
	 * @param ontology ontology the ontology
	 * @param iri the IRI
	 * @return whether the IRI denotes both a class and an individual
	 */
	public static boolean hasPunning(OWLOntology ontology, IRI iri){
		boolean isClass = ontology.getClassesInSignature().contains(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(iri));
		boolean isIndividual = ontology.getIndividualsInSignature().contains(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri));
		return isClass && isIndividual;
	}
	
	/**
	 * Returns the classes of the ontology that are also used as individuals, i.e. types of other classes.
	 * @param ontology the ontology
	 * @return the classes
	 */
	public static Set<OWLClass> getPunningClasses(OWLOntology ontology){
		Set<OWLClass> classes = new HashSet<>();
		Set<OWLNamedIndividual> individualsInSignature = ontology.getIndividualsInSignature();
		for (OWLClass cls : ontology.getClassesInSignature()) {
			if(individualsInSignature.contains(new OWLNamedIndividualImpl(cls.getIRI()))){
				classes.add(cls);
			}
//			for (OWLNamedIndividual ind : ontology.getIndividualsInSignature()) {
//				if(cls.getIRI().equals(ind.getIRI())){
//					classes.add(cls);
//					break;
//				}
//			}
		}
		return classes;
	}
	
	/**
	 * Returns the classes of the ontology that are also used as individuals, i.e. types of other classes.
	 * @param ontology the ontology
	 * @return the classes
	 */
	public static Set<IRI> getPunningIRIs(OWLOntology ontology){
		Set<IRI> classes = new HashSet<>();
		Set<OWLNamedIndividual> individualsInSignature = ontology.getIndividualsInSignature();
		for (OWLClass cls : ontology.getClassesInSignature()) {
			if(individualsInSignature.contains(new OWLNamedIndividualImpl(cls.getIRI()))){
				classes.add(cls.getIRI());
			}
//			for (OWLNamedIndividual ind : ontology.getIndividualsInSignature()) {
//				if(cls.getIRI().equals(ind.getIRI())){
//					classes.add(cls);
//					break;
//				}
//			}
		}
		return classes;
	}
	
	/**
	 * Checks whether the same IRI denotes both a class and an individual in the ontology.
	 * @param ontology the ontology
	 * @param iri the IRI
	 * @return whether the same IRI denotes both a class and an individual
	 */
	public static boolean hasPunning(OWLOntology ontology, String iri){
		return hasPunning(ontology, IRI.create(iri));
	}

}