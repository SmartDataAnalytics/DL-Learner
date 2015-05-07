/**
 * 
 */
package org.dllearner.utilities;

import org.semanticweb.owlapi.model.OWLClassExpression;

import com.google.common.base.Function;

/**
 * Utility class that calls toStringID() method for OWLClass objects instead of
 * toString() when used in Guava Joiner utility class.
 * @author Lorenz Buehmann
 *
 */
public class ToStringIDTransformer implements Function<OWLClassExpression, String> {
	@Override
	public String apply(OWLClassExpression input) {
		return input.asOWLClass().toStringID();
	}
}
