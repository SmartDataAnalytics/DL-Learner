package org.dllearner.sampling.temporal;

import java.util.Set;

import org.dllearner.core.Component;
import org.dllearner.reasoning.TemporalOWLReasoner;
import org.semanticweb.owlapi.model.OWLIndividual;

public interface TemporalSamplingStrategy extends Component{
	
	public void setReasoner(TemporalOWLReasoner reasoner);
	
	public void setPosExamples(Set<OWLIndividual> posExamples);

	public Set<OWLIndividual> getNegativeExamples(int numberOfExamples);
	
	public Set<OWLIndividual> getNegativeExamples();
}
