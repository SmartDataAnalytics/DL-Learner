package org.dllearner.algorithms.properties;

import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;

/**
 * A learning algorithm for object property axioms. 
 * @author Lorenz Buehmann
 *
 */
public abstract class ObjectPropertyAxiomLearner<T extends OWLObjectPropertyAxiom> extends PropertyAxiomLearner<OWLObjectProperty, T, OWLObjectPropertyAssertionAxiom> {
	

}
