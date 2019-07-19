package org.dllearner.utilities.examples;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 */
public class ListBasedExamplesProvider extends AbstractExamplesProvider {

    ListBasedExamplesProvider(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples) {
        super(posExamples, negExamples);
    }
}
