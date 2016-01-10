package org.dllearner.core;

/**
 * 
 * Interface for algorithms, which can be stopped and checked whether they are
 * running. This allows learning algorithms to be run in separate threads or
 * be terminated by the user.
 * 
 * @author Jens Lehmann
 *
 */
public interface StoppableLearningAlgorithm extends LearningAlgorithm {

	/**
	 * Stops the algorithm gracefully. A stopped algorithm cannot be resumed anymore.
	 * Use this method for cleanup and freeing memory.
	 */
	void stop();
	
	/**
	 * Returns whether the learning algorithm is running. Implementation
	 * should use a boolean status variable in their implementations of
	 * the start and resume methods.
	 * @return True if the algorithm is running, false otherwise.
	 */
	boolean isRunning();
	
}
