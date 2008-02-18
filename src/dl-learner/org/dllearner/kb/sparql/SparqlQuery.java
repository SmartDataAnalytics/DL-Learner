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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Represents one SPARQL query. It includes support for stopping the SPARQL
 * query (which may be necessary if a timeout is reached).
 * 
 * @author Jens Lehmann
 * 
 */
public class SparqlQuery {

	private static Logger logger = Logger.getLogger(SparqlKnowledgeSource.class);

	private boolean isRunning = false;
	private String queryString;
	private QueryEngineHTTP queryExecution;
	private SparqlEndpoint endpoint;
	private ResultSet rs = null;
	private SparqlQueryException sendException=null;

	/**
	 * Standard constructor.
	 * 
	 * @param queryString
	 * @param endpoint
	 */
	public SparqlQuery(String queryString, SparqlEndpoint endpoint) {
		this.queryString = queryString;
		this.endpoint = endpoint;
	}

	/**
	 * Sends a SPARQL query using the Jena library.
	 */
	public ResultSet send() {
		isRunning = true;
		logger.info(queryString);

		String service = endpoint.getURL().toString();
		logger.info(endpoint.getURL().toString());
		// Jena access to SPARQL endpoint
		queryExecution = new QueryEngineHTTP(service, queryString);
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}
		logger.info("query SPARQL server");
		try{
			//TODO after overnext Jena release
			HttpQuery.urlLimit = 3*1024 ;
			rs = queryExecution.execSelect();
			logger.info(rs.getResultVars().toString());
		} catch (Exception e){
			sendException=new SparqlQueryException(e.getMessage());
			logger.info("Exception when querying Sparql Endpoint");
		}
		isRunning = false;
		return rs;
	}

	public void stop() {
		queryExecution.abort();
		isRunning = false;
	}

	public String getQueryString() {
		return queryString;
	}
	
	public ResultSet getResultSet() {
		return rs;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public QueryEngineHTTP getExecution(){
		return queryExecution;
	}
	
	public SparqlQueryException getException(){
		return sendException;
	}

	public boolean hasCompleted() {
		return (rs != null);
	}		
	
	/**
	 * TODO define the format
	 * 
	 * @return
	 */
	@Deprecated
	@SuppressWarnings( { "unchecked" })
	public static String[][] getAsStringArray(ResultSet rs) {
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		List<String> resultVars = rs.getResultVars();
		String[][] array = new String[l.size()][resultVars.size()];
		Iterator<String> iter = resultVars.iterator();
		int i = 0, j = 0;

		for (ResultBinding resultBinding : l) {
			while (iter.hasNext()) {
				String varName = (String) iter.next();
				array[i][j] = resultBinding.get(varName).toString();
				j++;
			}
			iter = resultVars.iterator();
			i++;
			j = 0;
		}
		return array;
	}

	/**
	 * sends a query and returns XML
	 * 
	 * @return String xml
	 */
	public static String getAsXMLString(ResultSet resultSet) {
		//if (rs == null)
		//	this.send();
		return ResultSetFormatter.asXMLString(resultSet);
	}

	/**
	 * Converts Jena result set to JSON.
	 * 
	 * @param resultSet The result set to transform.
	 * @return JSON representation of the result set.
	 */
	public static String getAsJSON(ResultSet resultSet) {
		// if (rs == null)
		//	this.send();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ResultSetFormatter.outputAsJSON(baos, resultSet);
		// possible Jena bug: Jena modifies the result set during
		// JSON transformation, so we need to get it back
		//resultSet = JSONtoResultSet(baos.toString());
		try{
			return baos.toString("UTF-8");
		}catch (Exception e){
			return baos.toString();
		}
	}

	/**
	 * Converts from JSON to internal Jena format.
	 * 
	 * @param json
	 *            A JSON representation if a SPARQL query result.
	 * @return A Jena ResultSet.
	 */
	public static ResultSet JSONtoResultSet(String json) {
		ByteArrayInputStream bais = new ByteArrayInputStream(json
				.getBytes(Charset.forName("UTF-8")));
		return ResultSetFactory.fromJSON(bais);
	}

}
