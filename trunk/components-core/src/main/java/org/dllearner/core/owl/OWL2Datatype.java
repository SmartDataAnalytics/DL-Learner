package org.dllearner.core.owl;

import java.net.URI;

public enum OWL2Datatype {

    DOUBLE ( "http://www.w3.org/2001/XMLSchema#double"),
    INT ("http://www.w3.org/2001/XMLSchema#int"),
    INTEGER ("http://www.w3.org/2001/XMLSchema#integer"),
    BOOLEAN   ("http://www.w3.org/2001/XMLSchema#boolean"),
    STRING ("http://www.w3.org/2001/XMLSchema#string"),
    DATE ("http://www.w3.org/2001/XMLSchema#date"),
    DATETIME ("http://www.w3.org/2001/XMLSchema#dateTime");	
	
    private Datatype datatype;
    
	private OWL2Datatype(String str) {
		datatype = new Datatype(str);
	}    
    
	public Datatype getDatatype() {
		return datatype;
	}

	public URI getURI() {
		return datatype.getURI();
	}

}
