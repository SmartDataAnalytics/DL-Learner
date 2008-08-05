package org.dllearner.test;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;

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
		//System.out.println(se.getURL()+"?query="+query1);
		//SparqlQuery s =  new SparqlQuery(query1, se);
		//s.send();
		//System.out.println(s.getJson());
		
		
		String xml ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
"<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">"+
  "<head>"+
    "<variable name=\"artist\"/>"+
    "<variable name=\"name\"/>"+
    "<variable name=\"image\"/>"+
    "<variable name=\"homepage\"/>"+
  "</head>"+
  "<results ordered=\"false\" distinct=\"false\">"+
    "<result>"+
      "<binding name=\"artist\">"+
        "<uri>http://dbtune.org/jamendo/artist/6108</uri>"+
      "</binding>"+
      "<binding name=\"name\">"+
        "<literal datatype=\"http://www.w3.org/2001/XMLSchema#string\">Allison Crowe</literal>"+
      "</binding>"+
      "<binding name=\"image\">"+
        "<uri>http://img.jamendo.com/artists/a/allison.crowe.jpg</uri>"+
      "</binding>"+
      "<binding name=\"homepage\">"+
        "<uri>http://www.allisoncrowe.com</uri>"+
      "</binding>"+
    "</result>"+
  "</results>"+
"</sparql>";
		
		ResultSet rs = ResultSetFactory.fromXML(xml);
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		
		for (ResultBinding binding : l) {
			System.out.println(binding.toString());
		}

	}

}
