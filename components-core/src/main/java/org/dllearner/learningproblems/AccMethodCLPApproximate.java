package org.dllearner.learningproblems;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

public interface AccMethodCLPApproximate extends AccMethodApproximate {

	/**
	 * calculate approximate accuracy for an expression, according to method
	 * @param description the expression to test
	 * @param positiveExamples set of positive examples
	 * @param negativeExamples set of negative examples
	 * @param noise noise
	 * @return approximate accuracy value or -1 if too weak
	 */
    double getAccApproxCLP(OWLClassExpression description,
			   Collection<OWLIndividual> classInstances,
			   Collection<OWLIndividual> superClassInstances,
			   double coverageFactor, double noise);
	
}
