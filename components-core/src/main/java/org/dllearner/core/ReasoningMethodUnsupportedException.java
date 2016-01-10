package org.dllearner.core;

/**
 * Exception indicating that a reasoner implementation cannot support
 * the requested operation. Either the operation itself is not implemented
 * or does not support certain features, e.g. a reasoner could support
 * instance checks but not if the class OWLClassExpression contains datatype
 * constructs.
 * 
 * @author Jens Lehmann
 *
 */
public class ReasoningMethodUnsupportedException extends Exception {

	private static final long serialVersionUID = -7045236443032695475L;
	
	public ReasoningMethodUnsupportedException() {
		super();
	}	
	
	public ReasoningMethodUnsupportedException(String message) {
		super(message);
	}

}
