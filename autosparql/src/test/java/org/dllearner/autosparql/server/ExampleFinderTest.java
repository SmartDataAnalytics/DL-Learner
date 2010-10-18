package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.junit.Test;

public class ExampleFinderTest {
	
	private static final SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaAKSW();
	
	@Test
	public void test1(){
		try {
			ExampleFinder f = new ExampleFinder(endpoint, new ExtractionDBCache("select-cache"), new ExtractionDBCache("construct-cache"));
			
			List<String> posExamples = new ArrayList<String>();
			posExamples.add("http://dbpedia.org/resource/University_of_Leipzig");
			
			List<String> negExamples = new ArrayList<String>();
			negExamples.add("http://dbpedia.org/resource/Ackermann%E2%80%93Teubner_Memorial_Award");
			
			System.out.println(f.findSimilarExample(posExamples, negExamples));
		} catch (SPARQLQueryException e) {
			e.printStackTrace();
		}
	}

}
