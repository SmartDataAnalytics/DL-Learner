/**
 * 
 */
package org.dllearner.algorithms.ccel.data;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * A wrapper class that contains a set of positive and negative examples
 * and represents a partition of an original set of examples.
 * 
 * @author Lorenz Buehmann
 *
 */
public class Partition {
	
	private Set<OWLIndividual> posExamples;
	private Set<OWLIndividual> negExamples;
	
	public Partition(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples) {
		this.posExamples = posExamples;
		this.negExamples = negExamples;
	}
	
	/**
	 * @return the positive examples
	 */
	public Set<OWLIndividual> getPosExamples() {
		return posExamples;
	}
	
	/**
	 * @return the negative examples
	 */
	public Set<OWLIndividual> getNegExamples() {
		return negExamples;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "E+=" + posExamples + "\nE-=" + negExamples;
	}
	
	/**
	 * Print a list of partitions.
	 * @param partitions
	 */
	public static void print(List<Partition> partitions) {
		int i = 1;
		for (Partition partition : partitions) {
			System.out.println("P" + i++ + "{\n" + partition + "\n}");
		}
	}

}
