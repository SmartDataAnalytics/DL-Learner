package org.dllearner.autosparql.server.evaluation;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.ExampleFinder;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class EvaluationScript {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws MalformedURLException 
	 * @throws SPARQLQueryException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, MalformedURLException, SPARQLQueryException {
		SPARQLEndpointEx endpoint = new SPARQLEndpointEx(
				new URL("http://db0.aksw.org:8999/sparql"),
				Collections.singletonList("http://dbpedia.org"),
				Collections.<String>emptyList(),
				null, null,
				Collections.<String>emptyList());
		ExtractionDBCache selectQueriesCache = new ExtractionDBCache("evaluation/select-cache");
		ExtractionDBCache constructQueriesCache = new ExtractionDBCache("evaluation/construct-cache");
		
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://139.18.2.173/dbpedia_queries", "root", "WQPRisDa2");
		
		Statement st = conn.createStatement();
		
		//fetch all queries from table 'tmp', where the number of results is lower than 2000
		ResultSet queries = st.executeQuery("SELECT * FROM queries_final WHERE resultCount<2000");
		
		int id;
		String query;
		QueryEngineHTTP qexec;
		com.hp.hpl.jena.query.ResultSet rs;
		List<String> resources;
		QuerySolution qs;
		ExampleFinder exampleFinder;
		List<String> posExamples;
		List<String> negExamples;
		//iterate over the queries
		while(queries.next()){
			id = queries.getInt("id");
			query = queries.getString("query");
			
			
			//send query to SPARQLEndpoint
			qexec = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			for (String dgu : endpoint.getDefaultGraphURIs()) {
				qexec.addDefaultGraph(dgu);
			}
			for (String ngu : endpoint.getNamedGraphURIs()) {
				qexec.addNamedGraph(ngu);
			}		
			rs = qexec.execSelect();
			
			
			//put the URIs for the resources in variable var0 into a separate list
			resources = new ArrayList<String>();
			while(rs.hasNext()){
				qs = rs.next();
				if(qs.get("var0").isURIResource()){
					resources.add(qs.get("var0").asResource().getURI());
				}
			}
			
			
			//start learning
			exampleFinder = new ExampleFinder(endpoint, selectQueriesCache, constructQueriesCache);
			posExamples = new ArrayList<String>();
			negExamples = new ArrayList<String>();
			//we choose the first resource in the list as positive example
			String posExample = resources.get(0);
			posExamples.add(posExample);
			//we ask for the next similar example
			String nextExample = exampleFinder.findSimilarExample(posExamples, negExamples).getURI();
			//if the example is contained in the resultset of the query, we add it to the positive examples,
			//otherwise to the negatives
			if(resources.contains(nextExample)){
				posExamples.add(nextExample);
			} else {
				negExamples.add(nextExample);
			}
		
		
		}

	}

}
