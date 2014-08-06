/**
 * 
 */
package org.dllearner.reasoning;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLPunningDetector {
	
	/**
	 * This object property is used to connect individuals with classes that are also individuals, thus, lead to punning.
	 */
	public static final ObjectProperty punningProperty = new ObjectProperty("http://dl-learner.org/punning/relatedTo");
	
	/**
	 * Checks whether the same IRI denotes both a class and an individual in the ontology.
	 * @param ontology
	 * @param iri
	 * @return
	 */
	public static boolean hasPunning(OWLOntology ontology, NamedClass cls){
		return hasPunning(ontology, IRI.create(cls.getName()));
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
