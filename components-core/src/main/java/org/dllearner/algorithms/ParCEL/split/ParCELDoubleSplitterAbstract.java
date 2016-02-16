package org.dllearner.algorithms.ParCEL.split;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the interface for data type property splitting strategies in PDCCEL: Given a set of
 * positive and negative examples and a reasoner with the ontology loaded, calculate the splits for
 * using in data type properties
 * 
 * @author An C. Tran
 * 
 */

public interface ParCELDoubleSplitterAbstract {

	public void init() throws ComponentInitException;

	/**
	 * Calculate the split
	 * 
	 * @return A list of mapped data type properties and their splits
	 */
	public Map<OWLDataProperty, List<OWLLiteral>> computeSplits();

	/**
	 * Get the reasoner used by the splitter
	 * 
	 * @return The reasoner
	 */
	public AbstractReasonerComponent getReasoner();

	/**
	 * Set the reasoner for the splitter
	 * 
	 * @param reasoner
	 */
	public void setReasoner(AbstractReasonerComponent reasoner);

	/**
	 * Get the positive examples used by the splitter
	 * 
	 * @return Set of positive examples
	 */
	public Set<OWLIndividual> getPositiveExamples();

	/**
	 * Set the positive examples will be used to calculate the splits
	 * 
	 * @param positiveExamples
	 */
	public void setPositiveExamples(Set<OWLIndividual> positiveExamples);

	/**
	 * Get the positive examples used by the splitter
	 * 
	 * @return Set of negative examples
	 */
	public Set<OWLIndividual> getNegativeExamples();

	/**
	 * Assign the set of negative examples
	 * 
	 * @param negativeExamples
	 */
	public void setNegativeExamples(Set<OWLIndividual> negativeExamples);

}
