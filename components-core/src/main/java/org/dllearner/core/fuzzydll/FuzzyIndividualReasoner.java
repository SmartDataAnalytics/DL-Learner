package org.dllearner.core.fuzzydll;

import java.util.SortedSet;

import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Reasoning requests/queries related to fuzzy reasoning over individuals in the knowledge base.
 * 
 * @author Josue Iglesias
 *
 */
public interface FuzzyIndividualReasoner {
	
	/**
	 * Checks the fuzzy membership degree of <code>individual</code> over <code>description</code>.
	 * For instance, "Peter" may be an instance of "TallPerson" with fuzzy membership degree = 0.8.
	 * individual
	 * @param description An OWL class description.
	 * @param individual An individual.
	 * @return fuzzy membership degree of <code>individual</code> satisfying <code>description</code> [0-1].
	 */
	double hasTypeFuzzyMembership(OWLClassExpression description, FuzzyIndividual individual);
	
	SortedSet<FuzzyIndividual> getFuzzyIndividuals(OWLClassExpression concept);
}
