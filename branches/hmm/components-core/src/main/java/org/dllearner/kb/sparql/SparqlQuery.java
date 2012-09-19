/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.kb.sparql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.jamonapi.Monitor;

/**
 * Represents one SPARQL query. It includes support for stopping the SPARQL
 * query (which may be necessary if a timeout is reached) and is designed to be
 * able to run a query in a separate thread. 
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 * 
 */
public class SparqlQuery {

	private static boolean logDeletedOnStart = false;

	private static Logger logger = Logger.getLogger(SparqlQuery.class);
	
	// additional file for logging SPARQL queries etc.
	private static String sparqlLog = "log/sparql.txt";

	// whether the query is currently running
	private boolean isRunning = false;

	// whether the query has been executed
	private boolean wasExecuted = false;

	private String sparqlQueryString;

	private QueryEngineHTTP queryExecution;

	private SparqlEndpoint sparqlEndpoint;

	private ResultSetRewindable rs;

	/**
	 * Standard constructor.
	 * 
	 * @param sparqlQueryString
	 *            A SPARQL query string
	 * @param sparqlEndpoint
	 *            An Endpoint object
	 */
	public SparqlQuery(String sparqlQueryString, SparqlEndpoint sparqlEndpoint) {
		// QUALITY there seems to be a bug in ontowiki
		this.sparqlQueryString = sparqlQueryString.replaceAll("\n", " ");
		this.sparqlEndpoint = sparqlEndpoint;
	}

	/**
	 * Sends a SPARQL query using the Jena library.
	 * 
	 */
	public ResultSetRewindable send() {
		return send(true);
	}
	
	public ResultSetRewindable send(boolean writeLog) {
		isRunning = true;

		String service = sparqlEndpoint.getURL().toString();

		if(writeLog){
			writeToSparqlLog("***********\nNew Query:");
			SparqlQuery.writeToSparqlLog("wget -S -O - '\n" + sparqlEndpoint.getHTTPRequest());
			writeToSparqlLog(sparqlQueryString);
		}

		queryExecution = new QueryEngineHTTP(service, sparqlQueryString);

		// add default and named graphs
		for (String dgu : sparqlEndpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : sparqlEndpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}

		Monitor httpTime = JamonMonitorLogger.getTimeMonitor(SparqlQuery.class, "sparql query time").start();

		try {
			logger.debug("sending query: length: " + sparqlQueryString.length() + " | ENDPOINT: "
					+ sparqlEndpoint.getURL().toString());
			
			// we execute the query and store the result in a rewindable result set
			ResultSet tmp = queryExecution.execSelect();
			rs = ResultSetFactory.makeRewindable(tmp);
		} catch (HTTPException e) {
			logger.debug("HTTPException in SparqlQuery\n" + e.toString());
			logger.debug("query was " + sparqlQueryString);
			if(writeLog){
				writeToSparqlLog("ERROR: HTTPException occured" + e.toString());
			}
			isRunning = false;
			throw e;
		// TODO: RuntimeException is very general; is it possible to catch more specific exceptions?
		} catch (RuntimeException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("RuntimeException in SparqlQuery (see /log/sparql.txt): "
						+ e.toString());
				int length = Math.min(sparqlQueryString.length(), 300);
				logger.debug("query was (max. 300 chars displayed) "
						+ sparqlQueryString.substring(0, length - 1).replaceAll("\n", " "));
			}
			if(writeLog){
				writeToSparqlLog("ERROR: HTTPException occured: " + e.toString());
			}
			isRunning = false;
			throw e;
		}

		httpTime.stop();
		isRunning = false;
		wasExecuted = true;
		return rs;
	}

	public boolean sendAsk() {
		isRunning = true;
		String service = sparqlEndpoint.getURL().toString();
		queryExecution = new QueryEngineHTTP(service, sparqlQueryString);
		boolean result = queryExecution.execAsk();
		isRunning = false;
		return result;
	}
	
	/**
	 * Stops the execution of the query.
	 */
	public void stop() {
		queryExecution.abort();
		isRunning = false;
	}

	/**
	 * Gets the String representation of the SPARQL query.
	 * 
	 * @return sparqlQueryString
	 */
	public String getSparqlQueryString() {
		return sparqlQueryString;
	}

	/**
	 * @return sparqlEndpoint object
	 */
	public SparqlEndpoint getSparqlEndpoint() {
		return sparqlEndpoint;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Return the result in JSON format.
	 * 
	 * @return A JSON string converted from the result set or null 
	 * if the query has not been executed.
	 */
	public String getJson() {
		if(wasExecuted) {
			return convertResultSetToJSON(rs);
		} else {
			return null;
		}
	}

	/**
	 * Converts the result set to an XML string.
	 * 
	 * @return An XML String
	 */
	public String getXMLString() {
		if(wasExecuted) {
			return convertResultSetToXMLString(rs);
		} else {
			return null;
		}		
	}

	/**
	 * Special log for debugging SPARQL query execution. It lives here:
	 * "log/sparql.txt" if the directory doesn't exist, there could be an error.
	 * 
	 * @param s
	 *            the String to log
	 */
	private static void writeToSparqlLog(String s) {
		new File("log").mkdirs();
		File f = new File(sparqlLog);
		if(!f.canWrite() ){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			logger.info("could not write SPARQL log to : " + f.getAbsolutePath());
//			return ;
		}	
		
		if (!logDeletedOnStart) {
				Files.createFile(f, s + "\n");
				logDeletedOnStart = true;
			} else {
				Files.appendToFile(f, s + "\n");
			}
		
	}

	/**
	 * Converts Jena result set to XML. To make a ResultSet rewindable use:
	 * ResultSetRewindable rsRewind =
	 * ResultSetFactory.makeRewindable(resultSet);
	 * 
	 * @param resultSet
	 *            The result set to transform, must be rewindable to prevent
	 *            errors.
	 * @return String xml
	 */
	public static String convertResultSetToXMLString(ResultSetRewindable resultSet) {
		String retVal = ResultSetFormatter.asXMLString(resultSet);
		resultSet.reset();
		return retVal;
	}

	/**
	 * Converts Jena result set to JSON.
	 * 
	 * @param resultSet
	 *            The result set to transform, must be rewindable to prevent
	 *            errors.
	 * @return JSON representation of the result set.
	 */
	public static String convertResultSetToJSON(ResultSet resultSet) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ResultSetFormatter.outputAsJSON(baos, resultSet);
//		resultSet.reset();
		try {
			return baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should never happen as UTF-8 is supported
			throw new Error(e);
		}
	}

	/**
	 * Converts from JSON to internal Jena format.
	 * 
	 * @param json
	 *            A JSON representation if a SPARQL query result.
	 * @return A Jena ResultSet.
	 */
	public static ResultSetRewindable convertJSONtoResultSet(String json) {
		ByteArrayInputStream bais = new ByteArrayInputStream(json
				.getBytes(Charset.forName("UTF-8")));
		// System.out.println("JSON " + json);
		return ResultSetFactory.makeRewindable(ResultSetFactory.fromJSON(bais));
	}

	/**
	 * Converts from JSON to xml format.
	 * 
	 * @param json
	 *            A JSON representation if a SPARQL query result.
	 * @return A Jena ResultSet.
	 */
	public static String convertJSONtoXML(String json) {
		return convertResultSetToXMLString(convertJSONtoResultSet(json));
	}

}
