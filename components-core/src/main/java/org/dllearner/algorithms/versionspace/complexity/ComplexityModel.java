package org.dllearner.algorithms.versionspace.complexity;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A model for the complexity of a class expression.
 *
 * @author Lorenz Buehmann
 */
public interface ComplexityModel {

    /**
     * Checks whether the given class expression is valid in the complexity model, i.e. not too complex.
     * @param ce the class expression
     * @return whether the given class expression is valid in the complexity model
     */
    boolean isValid(OWLClassExpression ce);
}
