package org.dllearner.core;

/**
 * Interface for learning algorithms, which can be paused and later continued.
 * 
 * @author Jens Lehmann
 *
 */
public interface ResumableLearningAlgorithm {

	/**
	 * Pauses the algorithm (not all algorithms need to implement
	 * this operation).
	 */
	void pause();
	
	/**
	 * Resumes the algorithm (not all algorithms need to implement
	 * this operation). You can use this method to continue
	 * an algorithm run even after a termination criterion has been
	 * reached. It will run until paused, stopped, or terminated
	 * again.
	 */
	void resume();
	
}
