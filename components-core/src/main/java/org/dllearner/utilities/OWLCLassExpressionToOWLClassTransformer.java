/**
 * 
 */
package org.dllearner.utilities;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import com.google.common.base.Function;

/**
 * Utility class that returns an OWLClass instead of an OWLClassExpression object.
 * @author Lorenz Buehmann
 *
 */
public class OWLCLassExpressionToOWLClassTransformer implements Function<OWLClassExpression, OWLClass> {
	@Override
	public OWLClass apply(OWLClassExpression input) {
		return input.asOWLClass();
	}
}
