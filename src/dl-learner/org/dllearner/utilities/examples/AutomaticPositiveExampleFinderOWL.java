package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;

public class AutomaticPositiveExampleFinderOWL {
	
	// LOGGER: ComponentManager
	private static Logger logger = Logger
		.getLogger(AutomaticPositiveExampleFinderOWL.class);

	
	private ReasoningService reasoningService;
	
	private SortedSet<Individual> posExamples;
	
	public AutomaticPositiveExampleFinderOWL(ReasoningService reasoningService) {
	
		this.posExamples = new TreeSet<Individual>();
		this.reasoningService = reasoningService;
	}
	
	//QUALITY resultsize is not accounted for
	public void makePositiveExamplesFromConcept(Description concept){
		logger.debug("making Positive Examples from Concept: "+concept);
		this.posExamples = reasoningService.retrieval(concept);
		//this.posExamples = sparqltasks.retrieveInstancesForClassDescription(conceptKBSyntax, 0);
		logger.debug("pos Example size: "+posExamples.size());
	}
	
	
	public SortedSet<Individual> getPosExamples() {
		return posExamples;
	}




	
}
