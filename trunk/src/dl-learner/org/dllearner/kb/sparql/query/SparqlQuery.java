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
import java.util.Iterator;
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
 * Represents one SPARQL query. It includes support for stopping the SPARQL query
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

	public void setQueryExecutionRunning(boolean isRunning){
		this.isRunning=isRunning;
	}
	
	/**
	 * simplest contructor, works only with some endpoints, 
	 * not with DBpedia
	 * @param queryString
	 * @param url
	 */
	public SparqlQuery(String queryString, URL url) {
		this.queryString = queryString;
		this.endpoint = new SparqlEndpoint(url);
	}

	/**
	 * standard constructor
	 * @param queryString
	 * @param endpoint
	 */
	public SparqlQuery(String queryString, SparqlEndpoint endpoint) {
		this.queryString = queryString;
		this.endpoint = endpoint;
	}

	
	/**
	 * method used for sending over Jena
	 * @return jena ResultSet
	 */
	protected ResultSet send() {
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
		return rs;
	}

	public void stop() {
		queryExecution.abort();
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}
	
	public String[][] getAsStringArray(){
		System.out.println("Starting Query");
		ResultSet rs=send();
		System.out.println("getResults");
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		List resultVars=rs.getResultVars();
		String[][] array=new String[l.size()][resultVars.size()];
		Iterator iter=resultVars.iterator();
		int i=0,j=0;
		
		for (ResultBinding resultBinding : l) {
			while (iter.hasNext()){
				String varName=(String)iter.next();
				array[i][j]=resultBinding.get(varName).toString();
				j++;
			}
			iter=resultVars.iterator();
			i++;
			j=0;
		}
		System.out.println("Query complete");
		return array;
	}
	
	/**
	 * sends a query and returns XML
	 * 
	 * @return String xml
	 */
	public String getAsXMLString() {
		ResultSet rs = send();
		return ResultSetFormatter.asXMLString(rs);
	}

	/**
	 * sends a query and returns complicated Jena List with ResultBindings
	 * 
	 * 
	 * @return jena List<ResultBinding>
	 */
	@SuppressWarnings({"unchecked"})
	public List<ResultBinding> getAsList() {
		ResultSet rs = send();
		return ResultSetFormatter.toList(rs);
	}

	
	/**
	 * sends a query and returns the results for variable
	 * TODO untested and not used, feel free to change
	 * varName as Vector<String>
	 * @param varName
	 * @return Vector<String>
	 */
	@SuppressWarnings({"unchecked"})
	public Vector<String> getAsVector(String varName) {
		ResultSet rs = send();
		Vector<String> vret = new Vector<String>();
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		for (ResultBinding resultBinding : l) {
			vret.add(resultBinding.get(varName).toString());
		}
		return vret;
	}

	/**
	 * sends a query and returns the results for two variables
	 * ex: getAsVectorOfTupels("predicate", "object")
	 * TODO untested and not used, feel free to change
	 * 
	 * @param varName1
	 * @param varName2
	 * @return Vector<StringTuple>
	 */
	@SuppressWarnings({"unchecked"})
	public Vector<StringTuple> getAsVectorOfTupels(String varName1,
			String varName2) {
		ResultSet rs = send();
		
		Vector<StringTuple> vret = new Vector<StringTuple>();
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		//System.out.println(l);
		//System.out.println(ResultSetFormatter.asXMLString(rs));
		for (ResultBinding resultBinding : l) {
					
			vret.add(new StringTuple(resultBinding.get(varName1).toString(),
					resultBinding.get(varName2).toString()));
		}
		return vret;
	}

	
	/**
	 * sends a query and returns the results for n variables
	 * TODO not working, finish
	 * @param varNames
	 * @return Vector<Vector<String>>
	 */
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
	
	/**
	 * creates a query for subjects with the specified label
	 * @param label a phrase that is part of the label of a subject
	 * @param limit this limits the amount of results
	 * @param endpoint a SparqlEndpoint
	 * @return SparqlQuery
	 */
	public static SparqlQuery makeLabelQuery(String label,int limit,SparqlEndpoint endpoint){
		//TODO maybe use http://xmlns:com/foaf/0.1/page
		String queryString= 
		"SELECT DISTINCT ?subject\n"+
		"WHERE { ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?object. ?object bif:contains '\""+label+"\"'@en}\n"+
		"LIMIT "+limit;
		return new SparqlQuery( queryString,endpoint);
	}
	
	/**
	 * creates a query for all subjects that are of the type concept
	 * @param concept the type that subjects are searched for
	 * @param endpoint a SparqlEndpoint
	 * @return SparqlQuery
	 */
	public static SparqlQuery makeConceptQuery(String concept, SparqlEndpoint endpoint){
		String queryString = 
			"SELECT DISTINCT ?subject\n"+
			"WHERE { ?subject a <"+concept+">}\n";
		return new SparqlQuery( queryString,endpoint);
	}
	
	/**
	 * @param subject
	 * @param endpoint a SparqlEndpoint
	 * @return SparqlQuery
	 */
	public static SparqlQuery makeArticleQuery(String subject,SparqlEndpoint endpoint){
		String queryString = 
		"SELECT ?predicate ?object\n"+
		"WHERE { <"+subject+"> ?predicate ?object}\n";
		return new SparqlQuery( queryString,endpoint);
	}	
	

	public void p(String str) {
		if (print_flag) {
			System.out.println(str);
		}
	}

}
