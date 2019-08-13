package org.dllearner.utilities.examples;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 */
public class AbstractExamplesProvider implements ExamplesProvider {

    private final Set<OWLIndividual> posExamples;
    private final Set<OWLIndividual> negExamples;

    public AbstractExamplesProvider(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples) {
        this.posExamples = posExamples;
        this.negExamples = negExamples;
    }

    @Override
    public Set<OWLIndividual> getPosExamples() {
        return posExamples;
    }

    @Override
    public Set<OWLIndividual> getNegExamples() {
        return negExamples;
    }
}
