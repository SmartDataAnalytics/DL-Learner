package org.dllearner.algorithms.versionspace.complexity;

import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A complexity model based on the depth of a class expression.
 *
 * @author Lorenz Buehmann
 */
public class ClassExpressionDepthComplexityModel implements ComplexityModel {

    private int maxDepth = 7;

    public ClassExpressionDepthComplexityModel(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public boolean isValid(OWLClassExpression ce) {
        return OWLClassExpressionUtils.getDepth(ce) <= maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}
