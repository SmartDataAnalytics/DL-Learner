package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

public class AutomaticPositiveExampleFinderOWL {
	
	// LOGGER: ComponentManager
	private static Logger logger = Logger
		.getLogger(AutomaticPositiveExampleFinderOWL.class);

	
	private AbstractReasonerComponent reasoningService;
	
	private SortedSet<OWLIndividual> posExamples;
	
	public AutomaticPositiveExampleFinderOWL(AbstractReasonerComponent reasoningService) {
	
		this.posExamples = new TreeSet<>();
		this.reasoningService = reasoningService;
	}
	
	//QUALITY resultsize is not accounted for
	public void makePositiveExamplesFromConcept(OWLClassExpression concept){
		logger.debug("making Positive Examples from Concept: "+concept);
		this.posExamples.clear();
		this.posExamples.addAll(reasoningService.getIndividuals(concept));
		//this.posExamples = sparqltasks.retrieveInstancesForClassDescription(conceptKBSyntax, 0);
		logger.debug("pos Example size: "+posExamples.size());
	}
	
	
	public SortedSet<OWLIndividual> getPosExamples() {
		return posExamples;
	}




	
}
