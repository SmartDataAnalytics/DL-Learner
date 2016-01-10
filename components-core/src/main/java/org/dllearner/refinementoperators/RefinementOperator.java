package org.dllearner.refinementoperators;

import java.util.Set;

import org.dllearner.core.Component;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Interface for all refinement operators based on OWL/Description Logics.
 * A refinement operator
 * maps a description to a set of descriptions. For downward refinement
 * operators those descriptions are more special. For upward refinement
 * operators, those descriptions are more general. 
 * 
 * @author Jens Lehmann
 *
 */
public interface RefinementOperator extends Component {

	/**
	 * Standard refinement operation.
	 * @param description The description, which will be refined.
	 * @return A set of refinements.
	 */
	Set<OWLClassExpression> refine(OWLClassExpression description);
	
}
