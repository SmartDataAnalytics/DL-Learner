/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.test;

import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

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
			SparqlQuery s = new SparqlQuery(sparqlQueryString, SparqlEndpoint.getEndpointJamendo());
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
