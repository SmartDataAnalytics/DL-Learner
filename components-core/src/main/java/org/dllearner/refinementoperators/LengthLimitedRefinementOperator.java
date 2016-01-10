package org.dllearner.refinementoperators;

import java.util.List;
import java.util.Set;

import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A refinement operator for which the syntactic length of the generated
 * refinements can be limited.
 * 
 * @author Jens Lehmann
 *
 */
public interface LengthLimitedRefinementOperator extends RefinementOperator {

	/**
	 * Optional refinement operation, where the learning algorithm can
	 * specify an additional bound on the length of descriptions. 
	 * 
	 * @param description The description, which will be refined.
	 * @param maxLength The maximum length of returned description, where length is defined by {@link OWLClassExpressionUtils#getLength(OWLClassExpression)} }.
	 * @return A set of refinements obeying the above restrictions.
	 */
	Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength);
		
	/**
	 * Optional refinement operation, where the learning algorithm can
	 * specify an additional bound on the length of descriptions and
	 * a list of known refinements, which do not need to be returned. 
	 * 
	 * @param description The description, which will be refined.
	 * @param maxLength The maximum length of returned description, where length is defined by {@link OWLClassExpressionUtils#getLength(OWLClassExpression)}.
	 * @param knownRefinements A collection of known refinements, which do not need to be returned. 
	 * @return A set of refinements obeying the above restrictions.
	 */
	Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength, List<OWLClassExpression> knownRefinements);
		
}
