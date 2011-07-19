package org.autosparql.shared;

public class AutoSPARQLException extends Exception {

	
	private static final long serialVersionUID = 23309289967630244L;

	public AutoSPARQLException() {
	}

	public AutoSPARQLException(String message) {
		super(message);
	}

	public AutoSPARQLException(Throwable e) {
		super(e);
	}

	public AutoSPARQLException(String message, Throwable e) {
		super(message, e);
	}

}
