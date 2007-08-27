package org.dllearner.server.exceptions;

public class OntologyURLNotValid extends Exception {
	 static final long serialVersionUID=102;
    String detail;
    
    public OntologyURLNotValid (String message, String detail) {
        super (message);
        this.detail = detail;
    }
    
    public String getDetail () {
        return detail;
    }
}
