package org.dllearner.algorithms.ccel.data;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class PosNegExamplePartitioning implements ExamplePartitioning {
	/**
	 * Compute n partitions for the given positive and negative examples.
	 * @param posExamples
	 * @param negExamples
	 * @param nrOfPartitions
	 */
	public abstract List<Partition> computePartitions(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples, int nrOfPartitions);

}
