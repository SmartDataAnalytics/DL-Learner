package org.dllearner.autosparql.server.exception;

public class TimeOutException extends NBRException {

	private static final long serialVersionUID = -6701991056481856177L;
	
	private int timeoutMillis;
	
	public TimeOutException(){
		
	}
	
	public TimeOutException(int timeoutMillis){
		this.timeoutMillis = timeoutMillis;
	}

}
