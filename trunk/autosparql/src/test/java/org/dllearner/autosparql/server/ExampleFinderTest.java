package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.server.exception.TimeOutException;
import org.dllearner.autosparql.server.util.Endpoints;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.junit.Test;

import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class ExampleFinderTest {
	
	private static final SPARQLEndpointEx endpoint = Endpoints.getDBPediaAKSWEndpoint();
	
	@Test
	public void test1(){
		try {
			ExampleFinder f = new ExampleFinder(endpoint, new ExtractionDBCache("select-cache"), new ExtractionDBCache("construct-cache"));
			
			List<String> posExamples = new ArrayList<String>();
			posExamples.add("http://dbpedia.org/resource/Maroochydore%2C_Queensland");
			posExamples.add("http://dbpedia.org/resource/Sparwood_Secondary_School");
			
			List<String> negExamples = new ArrayList<String>();
			negExamples.add("http://dbpedia.org/resource/CAT:Sport_in_Hamburg");
//			negExamples.add("http://dbpedia.org/resource/Arizona_State_University");
			
			
			System.out.println(f.findSimilarExample(posExamples, negExamples));
		} catch (SPARQLQueryException e) {
			e.printStackTrace();
		} catch (TimeOutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
