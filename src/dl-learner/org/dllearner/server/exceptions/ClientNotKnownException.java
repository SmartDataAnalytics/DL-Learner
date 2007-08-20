package org.dllearner.server.exceptions;

public class ClientNotKnownException extends Exception {
    String detail;
    
    public ClientNotKnownException (String message, String detail) {
        super (message);
        this.detail = detail;
    }
    
    public String getDetail () {
        return detail;
    }
}
