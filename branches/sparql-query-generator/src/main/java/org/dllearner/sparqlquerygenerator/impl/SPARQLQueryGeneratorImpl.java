package org.dllearner.sparqlquerygenerator.impl;

import java.util.List;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.SPARQLQueryGenerator;

public class SPARQLQueryGeneratorImpl implements SPARQLQueryGenerator{
	
	private String endpointURL;
	
	private Set<String> posExamples;
	private Set<String> negExamples;
	
	public SPARQLQueryGeneratorImpl(){
		
	}
	
	public SPARQLQueryGeneratorImpl(String endpointURL){
		this.endpointURL = endpointURL;
	}

	@Override
	public List<String> getSPARQLQueries(Set<String> posExamples) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getSPARQLQueries(Set<String> posExamples,
			Set<String> negExamples) {
		// TODO Auto-generated method stub
		return null;
	}

}
