package org.dllearner.server.exceptions;

public class OntologyURLNotValid extends Exception {
    String detail;
    
    public OntologyURLNotValid (String message, String detail) {
        super (message);
        this.detail = detail;
    }
    
    public String getDetail () {
        return detail;
    }
}
