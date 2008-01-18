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

import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.utilities.StringTuple;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;

/**
 * Represents a SPARQL query. It includes support for stopping the SPARQL query
 * (which may be necessary if a timeout is reached).
 * 
 * @author Jens Lehmann
 * 
 */
public class SparqlQuery {

	private boolean print_flag = false;
	private boolean isRunning = false;
	private String queryString;
	private QueryExecution queryExecution;
	SparqlEndpoint endpoint;

	public SparqlQuery(String queryString, URL u) {
		this.queryString = queryString;
		this.endpoint = new SparqlEndpoint(u);
	}

	public SparqlQuery(String queryString, SparqlEndpoint se) {
		this.queryString = queryString;
		this.endpoint = se;
	}

	public ResultSet send() {
		isRunning = true;

		p(queryString);
		// create a query and parse it into Jena
		Query query = QueryFactory.create(queryString);
		// query.validate();

		String service = endpoint.getURL().toString();
		// Jena access to SPARQL endpoint
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(
				service, query, endpoint.getDefaultGraphURIs(), endpoint
						.getNamedGraphURIs());

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

	public String getAsXMLString() {
		ResultSet rs = send();
		return ResultSetFormatter.asXMLString(rs);
	}

	public List<ResultBinding> getAsList() {
		ResultSet rs = send();
		return ResultSetFormatter.toList(rs);
	}

	public Vector<String> getAsVector(String varName) {
		ResultSet rs = send();
		Vector<String> vret = new Vector<String>();
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		for (ResultBinding resultBinding : l) {
			vret.add(resultBinding.get(varName).toString());
		}
		return vret;
	}

	public Vector<StringTuple> getAsVectorOfTupels(String varName1,
			String varName2) {
		ResultSet rs = send();
		Vector<StringTuple> vret = new Vector<StringTuple>();
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		for (ResultBinding resultBinding : l) {
			vret.add(new StringTuple(resultBinding.get(varName1).toString(),
					resultBinding.get(varName2).toString()));
		}
		return vret;
	}

	@Deprecated
	public Vector<Vector<String>> getAsVectorOfVectors(Vector<String> varNames) {
		// ResultSet rs = send();
		Vector<Vector<String>> vret = new Vector<Vector<String>>();
		/*
		 * Does not work yet List<ResultBinding> l =
		 * ResultSetFormatter.toList(rs); for (ResultBinding resultBinding : l) {
		 * vret.add(new StringTuple(resultBinding.get(varName1).toString(),
		 * resultBinding.get(varName2).toString())); }
		 */
		return vret;
	}

	// probably not needed
	/*
	 * public Model asJenaModel(){ ResultSet rs=send(); return
	 * ResultSetFormatter.toModel(rs); }
	 */

	public void p(String str) {
		if (print_flag) {
			System.out.println(str);
		}
	}

}
