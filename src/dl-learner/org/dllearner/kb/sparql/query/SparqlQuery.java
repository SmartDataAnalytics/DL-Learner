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

import org.dllearner.kb.sparql.configuration.SpecificSparqlEndpoint;

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
public class SparqlQuery extends SparqlQueryAbstract{
		
	public SparqlQuery(SpecificSparqlEndpoint endpoint) {
		super(endpoint);
		// TODO Auto-generated constructor stub
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
