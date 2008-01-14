/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.kb.sparql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 * Represents a SPARQL query. It includes support for stopping the SPARQL
 * query (which may be necessary if a timeout is reached).
 * 
 * TODO: It is probably good to change all SPARQL query calls to use only
 * this class.
 * 
 * TODO: Could we use Jena as a solid foundation here? (com.hp.jena.query)
 * 
 * @author Jens Lehmann
 *
 */
public class SparqlQuery {
	
	private boolean isRunning = false;
	
	public SparqlQuery(SpecificSparqlEndpoint endpoint, String query) {
		
	}
	
	public void send() {
		isRunning = true;
		
		// ... send query 
		// ... check periodically whether isRunning is still true, if not
		// abort the query
	}
	
	public void stop() {
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}
	
	// this is a working Jena script
	// TODO: query runtime seems to be much too high (compared to running it in http://dbpedia.org/sparql)
	// verify whether our SPARQL query implementation is faster and why;
	// TODO: check whether Jena works with the other endpoints in PredefinedEndpoint; if not
	// check whether it can be configured to run with these
	public static void main(String[] args) {
		
		String queryString = "PREFIX dbpedia2: <http://dbpedia.org/property/> " +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
				"SELECT ?episode ?chalkboard_gag WHERE {   ?episode skos:subject" +
				"    <http://dbpedia.org/resource/Category:The_Simpsons_episodes%2C_season_12>." +
				"  ?episode dbpedia2:blackboard ?chalkboard_gag }";
		
		System.out.println(queryString);
		// create a query and parse it into Jena
		Query query = QueryFactory.create(queryString);
		query.validate();
		// Jena access to DBpedia SPARQL endpoint
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		System.out.println("query SPARQL server");		
		ResultSet rs = queryExecution.execSelect();
		ResultSetFormatter.out(System.out, rs, query) ;
	}
	
}
