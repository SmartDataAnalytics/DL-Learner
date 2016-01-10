package org.dllearner.core;

import java.util.List;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Basic interface for algorithms learning fuzzy OWL/DL class expressions.
 * 
 * TODO: Probably needs to be adapted.
 * 
 * @author Jens Lehmann
 *
 */
public interface FuzzyClassExpressionLearningAlgorithm extends LearningAlgorithm {

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
