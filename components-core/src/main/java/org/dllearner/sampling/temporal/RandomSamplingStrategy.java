package org.dllearner.sampling.temporal;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.dllearner.core.ComponentInitException;
import org.dllearner.reasoning.TemporalOWLReasoner;
import org.semanticweb.owlapi.model.OWLIndividual;

import com.google.common.collect.Lists;

public class RandomSamplingStrategy implements TemporalSamplingStrategy {
	private TemporalOWLReasoner reasoner = null;
	private Set<OWLIndividual> posExamples = null;
	

	public Set<OWLIndividual> getNegativeExamples(int numberOfExamples) {
		HashSet<OWLIndividual> negExamples = new HashSet<>();
		
		List<OWLIndividual> indivs = Lists.newArrayList(reasoner.getTimeIndividuals());
		int len = indivs.size();
		
		int nextIdx;
		Random rnd = new Random();
		do {
			nextIdx = rnd.nextInt(len);
			OWLIndividual i = indivs.get(nextIdx);
			
			if (!posExamples.contains(i))
				negExamples.add(i);
		} while (negExamples.size() < numberOfExamples);
		
		return negExamples;
	}

	@Override
	public Set<OWLIndividual> getNegativeExamples() {
		return getNegativeExamples(posExamples.size());
	}

	@Override
	public void init() throws ComponentInitException {
		if (reasoner == null || posExamples == null) {
			throw new ComponentInitException(
					"Temporal reasoner or positive examples not set.");
		}
	}

	@Override
	public void setReasoner(TemporalOWLReasoner reasoner) {
		this.reasoner = reasoner;
	}

	@Override
	public void setPosExamples(Set<OWLIndividual> posExamples) {
		this.posExamples = posExamples;
	}
}
