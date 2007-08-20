package org.dllearner.server.exceptions;

public class NoOntologySelectedException extends Exception {
    String detail;
    
    public NoOntologySelectedException (String message, String detail) {
        super (message);
        this.detail = detail;
    }
    
    public String getDetail () {
        return detail;
    }
}
