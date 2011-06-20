package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.QTL;
import org.dllearner.algorithm.qtl.operations.NBR;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.autosparql.server.util.Endpoints;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.junit.Test;

public class ExampleFinderTest {
	
	private static final SPARQLEndpointEx endpoint = Endpoints.getDBPediaAKSWEndpoint();
	private static final String SOLR_SERVER_URL = "http://139.18.2.173:8080/apache-solr-3.1.0/";
	
	@Test
	public void test1(){
		try {
			Logger.getLogger(QTL.class).setLevel(Level.DEBUG);
			Logger.getLogger(NBR.class).setLevel(Level.DEBUG);
			ExampleFinder f = new ExampleFinder(endpoint, new ExtractionDBCache("select-cache"), new ExtractionDBCache("construct-cache"), SOLR_SERVER_URL);
			
//			f.setQuestion("soccer clubs in Premier League");
//			
//			List<String> posExamples = new ArrayList<String>();
//			posExamples.add("http://dbpedia.org/resource/Manchester_United_F.C.");
//			posExamples.add("http://dbpedia.org/resource/Chelsea_F.C.");
////			posExamples.add("http://dbpedia.org/resource/Burnley_F.C.");
//			
//			List<String> negExamples = new ArrayList<String>();
//			negExamples.add("http://dbpedia.org/resource/Southern_Premier_Soccer_League");
////			negExamples.add("http://dbpedia.org/resource/Barnsley_F.C.");
////			negExamples.add("http://dbpedia.org/resource/Harchester_United_F.C.");
			
			f.setQuestion("films with starring Brad Pitt");
			
			List<String> posExamples = new ArrayList<String>();
			posExamples.add("http://dbpedia.org/resource/Interview_with_the_Vampire:_The_Vampire_Chronicles");
			posExamples.add("http://dbpedia.org/resource/Megamind");
			
			List<String> negExamples = new ArrayList<String>();
			negExamples.add("http://dbpedia.org/resource/Die_Hard");
			
			System.out.println(f.findSimilarExample(posExamples, negExamples));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
