package org.dllearner.algorithms.ccel.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class PosNegExamplePartitioningSimple extends PosNegExamplePartitioning {
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.ccel.data.PosNegExamplePartitioning#computePartitions(java.util.Set, java.util.Set, int)
	 */
	@Override
	public List<Partition> computePartitions(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples, int nrOfPartitions) {
		// handle positive examples
		int maxPartitionSize = posExamples.size() / nrOfPartitions;
		List<List<OWLIndividual>> posPartitions = Lists.partition(Lists.newArrayList(posExamples), maxPartitionSize);
		
		// handle negative examples
		maxPartitionSize = negExamples.size() / nrOfPartitions;
		List<List<OWLIndividual>> negPartitions = Lists.partition(Lists.newArrayList(negExamples), maxPartitionSize);
		
		List<Partition> partitions = new ArrayList<>();
		for(int i = 0; i <= Math.min(posPartitions.size(), negPartitions.size()); i++) {
			partitions.add(new Partition(
					Sets.newHashSet(posPartitions.get(i)), 
					Sets.newHashSet(negPartitions.get(i))));
		}
		
		return partitions;
	}

}
