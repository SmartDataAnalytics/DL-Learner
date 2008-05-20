package org.dllearner.utilities.examples;

import java.util.SortedSet;

import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class AutomaticNegativeExampleFinderSPARQL {

	
	private Cache c;
	private SparqlEndpoint se;
	private SortedSet<String> posExamples;
	private SortedSet<String> negExamples;
	
	static boolean useRelated = false;
	static boolean useSuperClasses = false;
	static boolean useParallelClasses = true;
	static int poslimit = 10;
	static int neglimit = 20;
}
