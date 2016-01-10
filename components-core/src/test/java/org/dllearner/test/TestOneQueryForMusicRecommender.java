package org.dllearner.test;

import java.net.URL;
import java.util.List;

import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class TestOneQueryForMusicRecommender {



static String xml ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
"<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">"+
"<head> "+
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


	public static void main(String[] args) {

		String p1 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
			"PREFIX mo: <http://purl.org/ontology/mo/>  ";
		String sparqlQueryString = p1+ "SELECT ?artist ?name ?image ?homepage WHERE {?artist a mo:MusicArtist .?artist foaf:name \"Allison Crowe\" .?artist foaf:name ?name .?artist foaf:img ?image . ?artist foaf:homepage ?homepage .	}LIMIT 10";

		System.out.println("SparqlQuery: ");
		System.out.println(sparqlQueryString);
		System.out.println("wget -S -O test.txt "+"'http://dbtune.org:2105/sparql/?query="+sparqlQueryString+"'");


		ResultSet rs = ResultSetFactory.fromXML(xml);
		@SuppressWarnings("unchecked")
		List<QuerySolution> l = ResultSetFormatter.toList(rs);

		for (QuerySolution binding : l) {
			System.out.println(binding.toString());
		}

		System.out.println("Executing query");
		rs = null;
		//String service = "http://dbtune.org:2105/sparql/";
		//QueryEngineHTTP queryExecution = new QueryEngineHTTP(service, sparqlQueryString);

		try{
			SparqlQuery s = new SparqlQuery(sparqlQueryString, new SparqlEndpoint(new URL("http://dbtune.org:2105/sparql/")));
			s.send();
			//rs = queryExecution.execSelect();

			//ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//ResultSetFormatter.outputAsJSON(baos, rs);


			//System.out.println( baos.toString("UTF-8"));
		} catch (Exception e) {
			// should never happen as UTF-8 is supported
			e.printStackTrace();

		}








	}




}
