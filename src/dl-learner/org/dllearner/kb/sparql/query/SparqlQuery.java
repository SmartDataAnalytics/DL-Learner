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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.utilities.StringTuple;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

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
		
		String service = endpoint.getURL().toString();
		p(endpoint.getURL().toString());
		// Jena access to SPARQL endpoint
		QueryEngineHTTP queryExecution=new QueryEngineHTTP(service,queryString);
		for (String dgu : endpoint.getDefaultGraphURIs()){
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()){
			queryExecution.addNamedGraph(ngu);
		}
		p("query SPARQL server");
		
		
		ResultSet rs = queryExecution.execSelect();
		p(rs.getResultVars().toString());
		//p(ResultSetFormatter.asXMLString(rs));
		return rs;
	}

	public void stop() {
		queryExecution.abort();
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}
	
	/**
	 * TODO define the format
	 * @return
	 */
	@SuppressWarnings({"unchecked"})
	public String[][] getAsStringArray(){
		System.out.println("Starting Query");
		ResultSet rs=send();
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		List<String> resultVars=rs.getResultVars();
		String[][] array=new String[l.size()][resultVars.size()];
		Iterator<String> iter=resultVars.iterator();
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
	@Deprecated
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
	@Deprecated
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
	/**
	 * sends a query and returns JSON
	 * @return a String representation of the Resultset as JSON
	 */
	public String getAsJSON(){
		ResultSet rs=send(); 
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ResultSetFormatter.outputAsJSON(baos, rs);
		return baos.toString();
	}
	
	
	/**
	 * @param json a string representation string object
	 * @return jena ResultSet
	 */
	public static ResultSet JSONtoResultSet(String json){
		ResultSet rs=null;
		try{
			ByteArrayInputStream bais=new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8")));
			rs=ResultSetFactory.fromJSON(bais);
		}catch (Exception e) {e.printStackTrace();}
		return rs;
		
	}
	
	public String getQueryString() {
		return queryString;
	}

	public void p(String str) {
		if (print_flag) {
			System.out.println(str);
		}
	}

}
