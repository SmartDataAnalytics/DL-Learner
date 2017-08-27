package org.dllearner.reasoning;

import java.util.Set;

import org.dllearner.core.Reasoner;
import org.semanticweb.owlapi.model.OWLIndividual;

public interface TemporalOWLReasoner extends Reasoner {

	public Set<OWLIndividual> getTimeIndividuals();

	public OWLAPIReasoner getReasonerComponent();
}
