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

}
