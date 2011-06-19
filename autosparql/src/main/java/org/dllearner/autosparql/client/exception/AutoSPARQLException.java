package org.dllearner.autosparql.client.exception;

public class AutoSPARQLException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3418490803650635417L;
	
	public AutoSPARQLException(){
		
	}
	
	public AutoSPARQLException(Exception e){
		super(e);
	}

	public AutoSPARQLException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public AutoSPARQLException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public AutoSPARQLException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	

}
