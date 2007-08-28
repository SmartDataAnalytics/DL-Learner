package org.dllearner.server;

public class ClientNotKnownException extends Exception {
    static final long serialVersionUID=100;
	String detail;
    
    public ClientNotKnownException (String message, String detail) {
        super (message);
        this.detail = detail;
    }
    
    public String getDetail () {
        return detail;
    }
}
