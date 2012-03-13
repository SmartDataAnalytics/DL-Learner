package org.dllearner.scripts;

import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class OxtractorHelper {

	private String endpoint = "http://live.dbpedia.org/sparql";
	private String defaultGraph = "http://dbpedia.org";
	
	public OxtractorHelper(String endpoint) {
		this(endpoint,null);
	}
	
	public OxtractorHelper(String endpoint, String defaultGraph) {
		this.endpoint = endpoint;
		this.defaultGraph = defaultGraph;
	}	
	
	public Set<String> getCategories(String keyword) {
		String sparqlQuery = "SELECT DISTINCT ?cat { ?cat <http://purl.org/dc/terms/subject> ?subject . ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label . FILTER( bif:contains(?label, \""+keyword+"\" ) ) } LIMIT 100";
		ResultSet rs = executeSelectQuery(sparqlQuery);
		QuerySolution qs;
		Set<String> categories = new TreeSet<String>();
		while(rs.hasNext()){
			qs = rs.next();
			categories.add(qs.get("cat").toString());
		}
		return categories;
	}
	
	public Set<String> getInstances(String category) {
		String sparqlQuery = "SELECT ?instance { ?instance <http://purl.org/dc/terms/subject> <"+category+"> }";
		ResultSet rs = executeSelectQuery(sparqlQuery);
		QuerySolution qs;
		Set<String> instances = new TreeSet<String>();
		while(rs.hasNext()){
			qs = rs.next();
			instances.add(qs.get("instance").toString());
		}
		return instances;
	}
	
	private ResultSet executeSelectQuery(String query) {
		System.out.println("Sending query: " + query);
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint, query);
		queryExecution.addDefaultGraph(defaultGraph);
		return queryExecution.execSelect();
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OxtractorHelper oh = new OxtractorHelper("http://live.dbpedia.org/sparql","http://dbpedia.org");
//		System.out.println(oh.getInstances("http://dbpedia.org/resource/Category:Cities_in_Saxony"));
		System.out.println(oh.getCategories("Kitchen"));
	}

}
