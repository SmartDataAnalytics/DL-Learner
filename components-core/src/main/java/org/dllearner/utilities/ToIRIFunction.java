/**
 * 
 */
package org.dllearner.utilities;

import org.semanticweb.owlapi.model.OWLEntity;

import com.google.common.base.Function;

/**
 * Utility class that calls toStringID() method for OWLEntity objects instead of
 * toString() when used in Guava Joiner utility class.
 * @author Lorenz Buehmann
 *
 */
public class ToIRIFunction implements Function<OWLEntity, String> {
	
	private boolean inAngleBrackets;

	public ToIRIFunction(boolean inAngleBrackets) {
		this.inAngleBrackets = inAngleBrackets;
	}
	
	public ToIRIFunction() {
		this(false);
	}
	
	@Override
	public String apply(OWLEntity input) {
		return inAngleBrackets ? ("<" + input.toStringID() + ">") : input.toStringID();
	}
}
