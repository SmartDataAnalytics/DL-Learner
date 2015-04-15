/**
 * 
 */
package org.dllearner.algorithms.ccel.data;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 *
 */
public interface ExamplesPartitioning {
	
	/**
	 * Compute n partitions for the given positive and negative examples.
	 * @param posExamples the positive examples
	 * @param negExamples the negative examples
	 * @param nrOfPartitions the number of partitions
	 */
	List<Partition> computePartitions(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples, int nrOfPartitions);

}
