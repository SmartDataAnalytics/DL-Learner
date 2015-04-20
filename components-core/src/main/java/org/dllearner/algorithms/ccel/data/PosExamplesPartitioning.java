/**
 * 
 */
package org.dllearner.algorithms.ccel.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Create partitions by splitting only the positive examples and duplicating the
 * negative examples.
 * 
 * @author Lorenz Buehmann
 *
 */
public class PosExamplesPartitioning implements ExamplesPartitioning{

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.ccel.data.ExamplePartitioning#computePartitions(java.util.Set, java.util.Set, int)
	 */
	@Override
	public List<Partition> computePartitions(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples, int nrOfPartitions) {
		// handle positive examples
		int posPartitionSize = Math.max(1, posExamples.size() / nrOfPartitions);
		List<List<OWLIndividual>> posPartitions = Lists.partition(Lists.newArrayList(posExamples), posPartitionSize);
		
		// create partitions
		List<Partition> partitions = new ArrayList<>();
		for(int i = 0; i < nrOfPartitions; i++) {
			List<OWLIndividual> pos = (i < posPartitions.size()) ? posPartitions.get(i) : Collections.<OWLIndividual>emptyList();
			
			partitions.add(new Partition(Sets.newHashSet(pos), Sets.newHashSet(negExamples)));
		}
		
		return partitions;
	}
}
