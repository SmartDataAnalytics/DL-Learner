/**
 * 
 */
package org.dllearner.algorithms.properties;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class DataPropertyAxiomLearner<T extends OWLDataPropertyAxiom> extends PropertyAxiomLearner<OWLDataProperty, T, OWLDataPropertyAssertionAxiom> {
	
}
