/**
 * 
 */
package org.dllearner.algorithms.ccel.data;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.google.common.collect.Sets;

/**
 * 
 * @author Lorenz Buehmann
 *
 */
public class PosNegExamplePartitioningTest {
	
	private static final int nrOfPosExamples = 20;
	private static final int nrOfNegExamples = 10;
	
	private static final int nrOfPartitions = 20;
	
	private static final Set<OWLIndividual> posExamples = Sets.newTreeSet();
	private static final Set<OWLIndividual> negExamples = Sets.newTreeSet();

	@BeforeClass
	public static void createData() {
		// pos examples
		for(int i = 0; i <= nrOfPosExamples ; i++) {
			posExamples.add(new OWLNamedIndividualImpl(IRI.create("p" + i)));
		}
		// neg examples
		for(int i = 0; i <= nrOfNegExamples ; i++) {
			negExamples.add(new OWLNamedIndividualImpl(IRI.create("n" + i)));
		}
	}

	/**
	 * Test method for
	 * {@link org.dllearner.algorithms.ccel.data.PosNegExamplesPartitioning#computePartitions(java.util.Set, java.util.Set, int)}
	 * .
	 */
	@Test
	public void testComputePartitionsPosNeg() {
		ExamplesPartitioning partitioning = new PosNegExamplesPartitioning();
		
		List<Partition> partitions = partitioning.computePartitions(posExamples, negExamples, nrOfPartitions);
		print(partitions);
		
		assertTrue(partitions.size() == nrOfPartitions);
		
	}

	/**
	 * Test method for
	 * {@link org.dllearner.algorithms.ccel.data.PosNegExamplesPartitioning#computePartitions(java.util.Set, java.util.Set, int)}
	 * .
	 */
	@Test
	public void testComputePartitionsPosOnly() {
		ExamplesPartitioning partitioning = new PosExamplesPartitioning();
		
		List<Partition> partitions = partitioning.computePartitions(posExamples, negExamples, nrOfPartitions);
		print(partitions);
		
		assertTrue(partitions.size() == nrOfPartitions);
		
	}
	
	public static void print(List<Partition> partitions) {
		int i = 1;
		for (Partition partition : partitions) {
			System.out.println("P" + i++ + "{\n" + partition + "\n}");
		}
	}

}
