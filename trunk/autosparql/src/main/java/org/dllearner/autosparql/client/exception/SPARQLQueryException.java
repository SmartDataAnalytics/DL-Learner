package org.dllearner.autosparql.client.exception;

import java.io.Serializable;

public class SPARQLQueryException extends AutoSPARQLException implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3205559196686634580L;
	
	private String query;
	
	public SPARQLQueryException(){
	}
	
	public SPARQLQueryException(String query){
		this.query = query;
	}
	
	public SPARQLQueryException(Exception e, String query){
		super(e);
		this.query = query;
	}
	
	public String getQuery(){
		return query;
	}

}
