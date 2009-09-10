package org.dllearner.tools.ore;

import java.net.MalformedURLException;
import java.net.URL;

import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class Sparql {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		URL url = new URL("http://dbpedia.org/sparql");
		SPARQLTasks task = new SPARQLTasks(SparqlEndpoint.getEndpointDBpedia());//new SparqlEndpoint(url));

		String queryString = "SELECT DISTINCT ?class WHERE {?class rdf:type owl:Class ." +
				"?class rdfs:label ?label . FILTER(regex(?label, \"City\")) }";
		System.out.println(task.queryAsSet(queryString, "class"));
	}

}
