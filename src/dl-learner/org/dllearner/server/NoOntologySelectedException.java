package org.dllearner.server;

public class NoOntologySelectedException extends Exception {
	 static final long serialVersionUID=101;
	String detail;
    
    public NoOntologySelectedException (String message, String detail) {
        super (message);
        this.detail = detail;
    }
    
    public String getDetail () {
        return detail;
    }
}
