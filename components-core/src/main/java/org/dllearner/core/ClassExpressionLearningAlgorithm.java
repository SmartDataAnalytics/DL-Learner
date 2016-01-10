package org.dllearner.core;

import java.util.List;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Basic interface for algorithms learning OWL/DL class expressions.
 * 
 * @author Jens Lehmann
 *
 */
public interface ClassExpressionLearningAlgorithm extends LearningAlgorithm {

	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions(int)
	 * @param nrOfDescriptions Limit for the number or returned descriptions.
	 * @return The best class descriptions found by the learning algorithm so far.
	 */
	List<OWLClassExpression> getCurrentlyBestDescriptions(int nrOfDescriptions);
		
	/**
	 * Return the best currently found concepts up to some maximum
	 * count (no minimality filter used).
	 * @param nrOfDescriptions Maximum number of descriptions returned.
	 * @return Return value is getCurrentlyBestDescriptions(nrOfDescriptions, 0.0, false).
	 */
	List<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions);
	
}
