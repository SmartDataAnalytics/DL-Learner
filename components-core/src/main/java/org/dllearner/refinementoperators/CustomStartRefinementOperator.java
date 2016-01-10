package org.dllearner.refinementoperators;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A refinement operator, which allows to set a start class.
 * 
 * @author Jens Lehmann
 *
 */
public interface CustomStartRefinementOperator extends RefinementOperator {

	void setStartClass(OWLClassExpression description);
	
}
