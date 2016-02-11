package org.dllearner.algorithms.versionspace.operator;

import org.dllearner.algorithms.versionspace.complexity.ComplexityModel;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.refinementoperators.RefinementOperatorAdapter;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;

import java.util.Set;

/**
 * @author Lorenz Buehmann
 */
public abstract class ComplexityBoundedOperator extends RefinementOperatorAdapter implements OWLClassExpressionVisitorEx<Set<OWLClassExpression>> {

	protected ComplexityModel complexityModel;
	protected AbstractReasonerComponent reasoner;

	public ComplexityBoundedOperator(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}

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
