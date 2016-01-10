package org.dllearner.core;


/**
 * Indicates that an operation is not supported/implemented for
 * a specific ontology file format.
 * 
 * @author Jens Lehmann
 *
 */
public class OntologyFormatUnsupportedException extends Exception {

	private static final long serialVersionUID = 1080949376967068007L;

	public OntologyFormatUnsupportedException(String operation, OntologyFormat format) {
		super("The operation " + operation + " does not support the ontology file format " + format);
	}
	
}
