package org.dllearner.core;

import java.util.List;

/**
 * Basic interface for algorithms learning SPARQL queries.
 * 
 * TODO: Check whether it is necessary to have a "real" SPARQL query class instead of 
 * only a string.
 * 
 * @author Jens Lehmann
 *
 */
public interface SparqlQueryLearningAlgorithm extends LearningAlgorithm {

	/**
	 * @param nrOfSPARQLQueries Limit for the number or returned SPARQL queries.
	 * @return The best SPARQL queries found by the learning algorithm so far.
	 */
	List<String> getCurrentlyBestSPARQLQueries(int nrOfSPARQLQueries);
	
	String getBestSPARQLQuery();
	
	
	
}
