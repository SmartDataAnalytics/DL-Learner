package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.junit.Test;

import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class ExampleFinderTest {
	
	private static final SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaAKSW();
	
	@Test
	public void test1(){
		try {
			ExampleFinder f = new ExampleFinder(endpoint, new ExtractionDBCache("select-cache"), new ExtractionDBCache("construct-cache"));
			
			List<String> posExamples = new ArrayList<String>();
			posExamples.add("http://dbpedia.org/resource/Peru");
			posExamples.add("http://dbpedia.org/resource/Costa_Rica");
			
			List<String> negExamples = new ArrayList<String>();
			
			
			System.out.println(f.findSimilarExample(posExamples, negExamples));
		} catch (SPARQLQueryException e) {
			e.printStackTrace();
		}
	}

}
