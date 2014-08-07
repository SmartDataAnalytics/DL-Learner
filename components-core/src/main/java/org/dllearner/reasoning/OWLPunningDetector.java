/**
 * 
 */
package org.dllearner.reasoning;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLPunningDetector {
	
	/**
	 * This object property is used to connect individuals with classes that are also individuals, thus, lead to punning.
	 */
	public static final OWLObjectProperty punningProperty = new OWLObjectPropertyImpl(IRI.create("http://dl-learner.org/punning/relatedTo"));
	
	/**
	 * Checks whether the same IRI denotes both a class and an individual in the ontology.
	 * @param ontology
	 * @param iri
	 * @return
	 */
	public static boolean hasPunning(OWLOntology ontology, OWLClass cls){
		return hasPunning(ontology, cls.getIRI());
	}
	
	/**
	 * Checks whether the same IRI denotes both a class and an individual in the ontology.
	 * @param ontology
	 * @param iri
	 * @return
	 */
	public static boolean hasPunning(OWLOntology ontology, IRI iri){
		boolean isClass = ontology.getClassesInSignature().contains(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(iri));
		boolean isIndividual = ontology.getIndividualsInSignature().contains(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri));
		return isClass && isIndividual;
	}
	
	/**
	 * Checks whether the same IRI denotes both a class and an individual in the ontology.
	 * @param ontology
	 * @param iri
	 * @return
	 */
	public static boolean hasPunning(OWLOntology ontology, String iri){
		return hasPunning(ontology, IRI.create(iri));
	}

}
