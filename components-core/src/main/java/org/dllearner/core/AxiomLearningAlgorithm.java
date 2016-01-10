package org.dllearner.core;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;

public interface AxiomLearningAlgorithm<T extends OWLAxiom> extends LearningAlgorithm {
	
	
	
	/**
	 * @return The best axioms found by the learning algorithm so far.
	 */
	List<T> getCurrentlyBestAxioms();
	
	/**
	 * @param nrOfAxioms Limit for the number or returned axioms.
	 * @return The best axioms found by the learning algorithm so far.
	 */
	List<T> getCurrentlyBestAxioms(int nrOfAxioms);
	
	/**
	 * @return The best evaluated axioms found by the learning algorithm so far.
	 */
	List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms();
	
	/**
	 * @param nrOfAxioms Limit for the number or returned evaluated axioms.
	 * @return The best evaluated axioms found by the learning algorithm so far.
	 */
	List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms);
	
	List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms,
															double accuracyThreshold);

}
