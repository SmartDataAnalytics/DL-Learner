package org.dllearner.algorithms.qtl.exception;

public class TimeOutException extends QTLException {

	private static final long serialVersionUID = -6701991056481856177L;

	private static final String MESSAGE = "Timeout of %d ms expired.";
	
	public TimeOutException(int timeoutMillis){
		super(String.format(MESSAGE, timeoutMillis));
	}

}
