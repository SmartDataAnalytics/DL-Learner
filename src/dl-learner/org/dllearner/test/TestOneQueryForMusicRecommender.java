package org.dllearner.test;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

public class TestOneQueryForMusicRecommender {

	private static Logger logger = Logger.getRootLogger();
	
	public static void main(String[] args) {
		Logger.getLogger(SparqlQuery.class).setLevel(Level.TRACE);
		logger.info("Start");
		String p1 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";
		p1 += "PREFIX mo: <http://purl.org/ontology/mo/>  ";
		String query1 = "SELECT ?artist ?name ?image ?homepage WHERE {?artist a mo:MusicArtist .?artist foaf:name \"Allison Crowe\" .?artist foaf:name ?name .?artist foaf:img ?image . ?artist foaf:homepage ?homepage .	}LIMIT 10";
		//String query2 = "SELECT ?artist ?name ?image ?homepage WHERE {?artist a mo:MusicArtist .?artist foaf:name 'Allison Crowe' .?artist foaf:name ?name .?artist foaf:img ?image . ?artist foaf:homepage ?homepage .	}LIMIT 10";
		
		
		SparqlEndpoint se = SparqlEndpoint.getEndpointJamendo();
		System.out.println(se.getURL());
		System.out.println("wget -S -O test.txt '"+se.getURL()+"?query="+query1+"'");
	//System.out.println("wget -S -O test.txt '"+se.getURL()+"?query="+query2+"'");
		System.out.println("wget -S -O test.txt '"+se.getURL()+"?query="+ p1+query1+"'");
		//System.out.println("wget -S -O test.txt '"+se.getURL()+"?query="+ p1+query2+"'");
		System.out.println(se.getURL()+"?query="+query1);
		SparqlQuery s =  new SparqlQuery(p1+query1, se);
		s.send();
		System.out.println(s.getJson());
		

	}

}
