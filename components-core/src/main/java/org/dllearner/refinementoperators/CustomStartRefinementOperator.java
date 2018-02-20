package org.dllearner.refinementoperators;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A refinement operator, which allows to set a start class.
 */
public interface CustomStartRefinementOperator extends RefinementOperator {
	interface Builder<T extends CustomHierarchyRefinementOperator> extends org.dllearner.core.Builder<T> {

		Builder<T> setStartClass(OWLClassExpression description);

	}
}
