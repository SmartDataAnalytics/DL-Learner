package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class AutomaticPositiveExampleFinderSPARQL {
	
	//CHECK
	private static Logger logger = Logger
	.getLogger(ComponentManager.class);

	
	private SPARQLTasks sparqltasks;
	
	private SortedSet<String> posExamples;
	
	public AutomaticPositiveExampleFinderSPARQL(SPARQLTasks st) {
		super();
		
		this.posExamples = new TreeSet<String>();
		this.sparqltasks = st;
	}
	
	//QUALITY resultsize is not accounted for
	public void makePositiveExamplesFromConcept(String conceptKBSyntax){
		logger.debug("making Positive Examples from Concept: "+conceptKBSyntax);	
		this.posExamples = sparqltasks.retrieveInstancesForConcept(conceptKBSyntax, 0);
		logger.debug("   pos Example size: "+posExamples.size());
	}
	
	
	//QUALITY resultsize is not accounted for
	public void makePositiveExamplesFromRoleAndObject(String role, String object){
		logger.debug("making Positive Examples from role: "+role+" and object: "+object);	
		this.posExamples = sparqltasks.retrieveDISTINCTSubjectsForRoleAndObject(role, object, 0);
		logger.debug("   pos Example size: "+posExamples.size());
	}
	
	//QUALITY resultsize is not accounted for
	public void makePositiveExamplesFromSKOSConcept(String SKOSConcept){
		logger.debug("making Positive Examples from SKOSConcept: "+SKOSConcept);	
		this.posExamples = sparqltasks.retrieveInstancesForSKOSConcept(SKOSConcept, 0);
		logger.debug("pos Example size: "+posExamples.size());
	}

	public SortedSet<String> getPosExamples() {
		return posExamples;
	}




	
}
