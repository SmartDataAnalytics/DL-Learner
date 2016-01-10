package org.dllearner.core;


/**
 * Active Learning algorithms are those, which can use feedback from an oracle.
 * 
 * @author Jens Lehmann
 *
 */
public interface ActiveLearningAlgorithm extends LearningAlgorithm {

	/**
	 * In order to separate/hide the implementation of an oracle from the active learning algorithm itself,
	 * an oracle object is passed to the algorithm.
	 * @param oracle The oracle to be used by the learning algorithm.
	 */
	void setOracle(Oracle oracle);
	
}
