package org.dllearner.algorithms.ccel.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Create partitions by splitting up both the positive and negative examples.
 * 
 * @author Lorenz Buehmann
 *
 */
public class PosNegExamplesPartitioning implements ExamplesPartitioning {
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.ccel.data.ExamplePartitioning#computePartitions(java.util.Set, java.util.Set, int)
	 */
	@Override
	public List<Partition> computePartitions(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples, int nrOfPartitions) {
		// handle positive examples
		int posPartitionSize = Math.max(1, posExamples.size() / nrOfPartitions);
		List<List<OWLIndividual>> posPartitions = Lists.partition(Lists.newArrayList(posExamples), posPartitionSize);
		
		// handle negative examples
		int negPartitionSize = Math.max(1, negExamples.size() / nrOfPartitions);
		List<List<OWLIndividual>> negPartitions = Lists.partition(Lists.newArrayList(negExamples), negPartitionSize);
		
		// create partitions
		List<Partition> partitions = new ArrayList<>();
		for(int i = 0; i < nrOfPartitions; i++) {
			List<OWLIndividual> pos = (i < posPartitions.size()) ? posPartitions.get(i) : Collections.emptyList();
			List<OWLIndividual> neg = (i < negPartitions.size()) ? negPartitions.get(i) : Collections.emptyList();
			
			partitions.add(new Partition(Sets.newHashSet(pos),Sets.newHashSet(neg)));
		}
		
		return partitions;
	}

}
