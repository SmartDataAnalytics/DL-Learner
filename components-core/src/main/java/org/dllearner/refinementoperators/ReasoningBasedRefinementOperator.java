package org.dllearner.refinementoperators;

import org.dllearner.core.Reasoner;

/**
 * A refinement operator, which uses an underlying reasoner, which is a typical
 * scenario.
 * 
 * @author Jens Lehmann
 *
 */
public interface ReasoningBasedRefinementOperator extends RefinementOperator {

	void setReasoner(Reasoner reasoner);
	
}
