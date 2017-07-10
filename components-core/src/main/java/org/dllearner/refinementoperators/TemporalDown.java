package org.dllearner.refinementoperators;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.Reasoner;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.List;
import java.util.Set;

/**
 * Refinement of temporal relationships
 */
public class TemporalDown extends AbstractComponent implements LengthLimitedRefinementOperator, ReasoningBasedRefinementOperator {
	@Override
	public void setReasoner(Reasoner reasoner) {
		
	}

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description) {
		return null;
	}

	@Override
	public void init() throws ComponentInitException {

	}

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength) {
		return null;
	}

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength, List<OWLClassExpression> knownRefinements) {
		return null;
	}

	@Override
	public void setLengthMetric(OWLClassExpressionLengthMetric lengthMetric) {

	}

	@Override
	public OWLClassExpressionLengthMetric getLengthMetric() {
		return null;
	}
}
