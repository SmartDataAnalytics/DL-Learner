package org.dllearner.core;

import java.util.Collection;

/**
 * Exception, which is thrown when an application tries to run 
 * a learning algorithm with a learning problem it does not
 * support.
 * 
 * @author Jens Lehmann
 *
 */
public class LearningProblemUnsupportedException extends Exception {

	private static final long serialVersionUID = 177919265073997460L;

	public LearningProblemUnsupportedException(Class<? extends AbstractClassExpressionLearningProblem> problemClass, Class<? extends LearningAlgorithm> algorithmClass) {
		super("Warning: No suitable constructor registered for algorithm "
				+ algorithmClass.getName() + " and problem " + problemClass.getClass().getName() + ".");		
	}
	
	public LearningProblemUnsupportedException(Class<? extends AbstractClassExpressionLearningProblem> problemClass, Class<? extends LearningAlgorithm> algorithmClass, Collection<Class<? extends AbstractClassExpressionLearningProblem>> supportedProblems) {
		super("Warning: No suitable constructor registered for algorithm "
				+ algorithmClass.getName() + " and problem " + problemClass.getClass().getName()
				+ ". Registered constructors for " + algorithmClass.getName() + ": "
				+ supportedProblems + ".");		
	}
	
}
