package org.dllearner.refinementoperators;

import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;

import java.util.Set;

/**
 * @author Lorenz Buehmann
 */
public abstract class ComplexityBoundedOperator extends RefinementOperatorAdapter implements OWLClassExpressionVisitorEx<Set<OWLClassExpression>> {

	protected ComplexityModel complexityModel;
	protected AbstractReasonerComponent reasoner;

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description) {
		return description.accept(this);
	}

	public void setComplexityModel(ComplexityModel complexityModel) {
		this.complexityModel = complexityModel;
	}

	public ComplexityModel getComplexityModel() {
		return complexityModel;
	}
}
