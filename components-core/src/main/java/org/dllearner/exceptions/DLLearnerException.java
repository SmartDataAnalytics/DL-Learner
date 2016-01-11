package org.dllearner.exceptions;

/**
 * An exception thrown by the DL-Learner framework.
 * @author Lorenz Buehmann
 *
 */
public class DLLearnerException extends Exception{
	
	private static final long serialVersionUID = 8926306932002748984L;

	public DLLearnerException(String message) {
		super(message);
	}

	public DLLearnerException(Throwable cause) {
		super(cause);
	}

}
