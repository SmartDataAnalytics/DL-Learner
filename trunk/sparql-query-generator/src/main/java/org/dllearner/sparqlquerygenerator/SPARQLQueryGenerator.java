package org.dllearner.sparqlquerygenerator;

import java.util.List;
import java.util.Set;

public interface SPARQLQueryGenerator {
	
	List<String> getSPARQLQueries(Set<String> posExamples);
	
	List<String> getSPARQLQueries(Set<String> posExamples, Set<String> negExamples);

}
