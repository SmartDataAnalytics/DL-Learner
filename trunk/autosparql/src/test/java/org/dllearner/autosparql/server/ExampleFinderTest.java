package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.autosparql.server.util.Endpoints;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.junit.Test;

public class ExampleFinderTest {
	
	private static final SPARQLEndpointEx endpoint = Endpoints.getDB0Endpoint();
	
	@Test
	public void test1(){
		try {
			ExampleFinder f = new ExampleFinder(endpoint, new ExtractionDBCache("select-cache"), new ExtractionDBCache("construct-cache"));
			
			f.setQuestion("soccer clubs in Premier League");
			
			List<String> posExamples = new ArrayList<String>();
			posExamples.add("http://dbpedia.org/resource/Manchester_United_F.C.");
			posExamples.add("http://dbpedia.org/resource/Liverpool_F.C.");
			
			List<String> negExamples = new ArrayList<String>();
			negExamples.add("http://dbpedia.org/resource/1._FC_Kaiserslautern");
			
			System.out.println(f.findSimilarExample(posExamples, negExamples));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
