/**
 * 
 */
package org.dllearner.algorithms.tdts.refinementoperators;

import java.util.Set;
import java.util.SortedSet;

import org.dllearner.refinementoperators.ReasoningBasedRefinementOperator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * A reasoning based refinement operator which consider the available instances in the 
 * training step for the generation of the candidates
 * @author Utente
 *
 */
public interface InstanceBasedRefinementOperator extends ReasoningBasedRefinementOperator {
	
	

	Set<OWLClassExpression> refine(OWLClassExpression definition, SortedSet<OWLIndividual> posExs,
			SortedSet<OWLIndividual> negExs);

}
