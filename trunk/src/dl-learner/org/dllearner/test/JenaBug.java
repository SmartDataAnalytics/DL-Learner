package org.dllearner.test;

import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class JenaBug {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String query = "SELECT * WHERE {?s ?p ?o} LIMIT 10";
		String endpoint = "http://dbtune.org/bbc/peel/sparql";
		
		try {
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint, query);
			queryExecution.execSelect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
