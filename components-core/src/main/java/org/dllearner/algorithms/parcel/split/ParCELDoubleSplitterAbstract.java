package org.dllearner.algorithms.parcel.split;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * This is the interface for data type property splitting strategies in ParCEL: Given a set of
 * positive and negative examples and a reasoner with the ontology loaded, calculate the splits for
 * data type properties in the ontology
 * 
 * @author An C. Tran
 * 
 */

public interface ParCELDoubleSplitterAbstract {

	  
	void init() throws ComponentInitException;
	

	/**
	 * Calculate the split
	 * 
	 * @return A list of mapped data type properties and their splits
	 */
	Map<OWLDataProperty, List<Double>> computeSplits();

	
	/**
	 * Get the reasoner used by the splitter
	 * 
	 * @return The reasoner
	 */
	AbstractReasonerComponent getReasoner();

	
	/**
	 * Set the reasoner for the splitter
	 * 
	 * @param reasoner
	 */
	void setReasoner(AbstractReasonerComponent reasoner);

	
	/**
	 * Get the positive examples used by the splitter
	 * 
	 * @return Set of positive examples
	 */
	Set<OWLIndividual> getPositiveExamples();

	
	/**
	 * Set the positive examples will be used to calculate the splits
	 * 
	 * @param positiveExamples
	 */
	void setPositiveExamples(Set<OWLIndividual> positiveExamples);

	
	/**
	 * Get the positive examples used by the splitter
	 * 
	 * @return Set of negative examples
	 */
	Set<OWLIndividual> getNegativeExamples();

	
	/**
	 * Assign the negative example set
	 * 
	 * @param negativeExamples
	 */
	void setNegativeExamples(Set<OWLIndividual> negativeExamples);

}
