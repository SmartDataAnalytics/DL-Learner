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
package org.dllearner.kb.sparql.query;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

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
 * @author Jens Lehmann
 *
 */
public class SparqlQuery extends SparqlQueryAbstract{
		
	private boolean isRunning = false;
	private String queryString;
	private QueryExecution queryExecution;
	
	public SparqlQuery(SparqlEndpoint endpoint, String queryString) {
		super(endpoint);
		this.queryString = queryString;
	}
	
	public ResultSet send() {
		isRunning = true;
				
		p(queryString);
		// create a query and parse it into Jena
		Query query = QueryFactory.create(queryString);
		// query.validate();
		// Jena access to DBpedia SPARQL endpoint
		String service=specificSparqlEndpoint.getURL().toString();
		
		// TODO: the graph uri should be a parameter of SparqlQuery
		ArrayList<String> al=new ArrayList<String>();
		al.add("http://dbpedia.org");
		QueryExecution queryExecution = 
			QueryExecutionFactory.sparqlService(service, query, al, new ArrayList<String>());
		p("query SPARQL server");		
		ResultSet rs = queryExecution.execSelect();		
		isRunning = false;
		return rs;
	}
	
	public void stop() {
		queryExecution.abort();
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}

	
	
	// CODE BY SEBASTIAN H. BELOW //
	
	public SparqlQuery(SparqlEndpoint endpoint) {
		super(endpoint);
	}	
	
	private ResultSet sendAndReceive(String queryString){
		
		p(queryString);
		// create a query and parse it into Jena
		Query query = QueryFactory.create(queryString);
		query.validate();
		// Jena access to DBpedia SPARQL endpoint
		String service=specificSparqlEndpoint.getURL().toString();
		ArrayList al=new ArrayList();
		al.add("http://dbpedia.org");
		//QueryExecution queryExecution = 
			//QueryExecutionFactory.sparqlService(specificSparqlEndpoint.getURL().toString(), query);
		QueryExecution queryExecution = 
			QueryExecutionFactory.sparqlService(service, query, al, new ArrayList());
		p("query SPARQL server");		
		ResultSet rs = queryExecution.execSelect();
		
		//ResultSetFormatter.out(System.out, rs, query) ;
		
		return rs;
	}
	
	public String getAsXMLString(String queryString){
		ResultSet rs=sendAndReceive(queryString);
		return ResultSetFormatter.asXMLString(rs);
	}
	
	public List asList(String queryString){
		ResultSet rs=sendAndReceive(queryString);
		return ResultSetFormatter.toList(rs);
		
	}
	
}
