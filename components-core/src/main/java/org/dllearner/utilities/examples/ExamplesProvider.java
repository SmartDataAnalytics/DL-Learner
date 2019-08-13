package org.dllearner.utilities.examples;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 */
public interface ExamplesProvider {

    Set<OWLIndividual> getPosExamples();

    Set<OWLIndividual> getNegExamples();
}
