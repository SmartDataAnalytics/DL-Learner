package org.dllearner.server;

public class ClientNotKnownException extends Exception {
	
    static final long serialVersionUID=100;
    
    public ClientNotKnownException (long id) {
        super ("Client with id " + id + " is not known.");
    }
    
}
