package org.dllearner.utilities.examples;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 */
public class SetBasedExamplesProvider extends AbstractExamplesProvider {

    public SetBasedExamplesProvider(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples) {
        super(posExamples, negExamples);
    }
}
