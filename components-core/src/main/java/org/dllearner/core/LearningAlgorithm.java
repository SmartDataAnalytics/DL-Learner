package org.dllearner.core;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * Basic interface for all DL-Learner learning algorithms.
 * 
 * @author Jens Lehmann
 *
 */
public interface LearningAlgorithm extends Component {

	/**
	 * Starts the algorithm. It runs until paused, stopped, or
	 * a termination criterion has been reached.
	 */
    void start();
	
	/**
	 * Get underlying learning problem.
	 * @return Underlying learning problem.
	 */
    LearningProblem getLearningProblem();

    /**
     * Set the learning problem, which the algorithm should solve.
     * @param learningProblem The learning problem to solve.
     */
    @Autowired
    void setLearningProblem(LearningProblem learningProblem);
	
}
