package org.dllearner.refinementoperators;

import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * @author Lorenz Buehmann
 */
public class ComplexityModel {

	private int maxLength = 5;

	public boolean isComplex(OWLClassExpression ce) {
		return OWLClassExpressionUtils.getLength(ce) >= maxLength;
	}
}
