package org.dllearner.algorithm.qtl.exception;

public class TimeOutException extends QTLException {

	private static final long serialVersionUID = -6701991056481856177L;
	
	private int timeoutMillis;
	
	public TimeOutException(){
	}
	
	public TimeOutException(String message){
		super(message);
	}
	
	public TimeOutException(int timeoutMillis){
		this.timeoutMillis = timeoutMillis;
	}

}
