package org.dllearner.algorithms.versionspace.complexity;

import org.dllearner.algorithms.versionspace.complexity.ComplexityModel;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A complexity model based on the length of a class expression.
 *
 * @author Lorenz Buehmann
 */
public class ClassExpressionLengthComplexityModel implements ComplexityModel{

	private int maxLength = 7;

	public ClassExpressionLengthComplexityModel(int maxLength) {
		this.maxLength = maxLength;
	}

	@Override
	public boolean isValid(OWLClassExpression ce) {
		return OWLClassExpressionUtils.getLength(ce) <= maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
}
