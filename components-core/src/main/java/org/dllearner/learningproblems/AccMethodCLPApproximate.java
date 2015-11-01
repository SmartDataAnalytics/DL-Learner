package org.dllearner.learningproblems;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

public interface AccMethodCLPApproximate extends AccMethodApproximate {
	double getAccApproxCLP(OWLClassExpression description,
						   Collection<OWLIndividual> classInstances,
						   Collection<OWLIndividual> superClassInstances,
						   double coverageFactor, double noise);
}
