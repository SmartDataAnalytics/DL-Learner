package org.dllearner.learningproblems;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

public interface AccMethodTwoValuedApproximate extends AccMethodApproximate {

	/**
	 * calculate approximate accuracy for an expression, according to method
	 * @param description the expression to test
	 * @param positiveExamples set of positive examples
	 * @param negativeExamples set of negative examples
	 * @param noise noise
	 * @return approximate accuracy value or -1 if too weak
	 */
	public double getAccApprox2(OWLClassExpression description, Collection<OWLIndividual> positiveExamples, Collection<OWLIndividual> negativeExamples, double noise);
	
}
