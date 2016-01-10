package org.dllearner.core;

/**
 * Exception which is thrown when a component cannot be intialised,
 * e.g. due to bad configuration parameters, or unforeseen 
 * circumstances, e.g. unreachable web files. It can encapsulate arbitrary
 * exceptions occurring during initialisation.
 * 
 * @author Jens Lehmann
 *
 */
public class ComponentInitException extends Exception {
	         
	private static final long serialVersionUID = -3550079897929658317L;

	/**
	 * Creates a <code>ComponentInitException</code> with the specified message.
	 * @param message The specified detail message.
	 */
	public ComponentInitException(String message) {
		super(message);
	}
	
	/**
	 * Creates a <code>ComponentInitException</code> with the
	 * specified cause.
	 * @param cause The cause of this exception.
	 */
	public ComponentInitException(Throwable cause) {
		super(cause);
	}	
	
	/**
	 * Creates a <code>ComponentInitException</code> with the
	 * specified message and cause.
	 * @param message The specified detail message.
	 * @param cause The cause of this exception.
	 */
	public ComponentInitException(String message, Throwable cause) {
		super(message, cause);
	}

}
