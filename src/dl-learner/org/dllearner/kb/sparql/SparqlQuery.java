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
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.dllearner.utilities.JamonMonitorLogger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Represents one SPARQL query. It includes support for stopping the SPARQL
 * query (which may be necessary if a timeout is reached).
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 * 
 */
public class SparqlQuery {

	private static boolean logDeletedOnStart = false;
	
	private static Logger logger = Logger.getLogger(SparqlQuery.class);

	private boolean isRunning = false;

	private boolean wasExecuted = false;

	private String sparqlQueryString;

	private QueryEngineHTTP queryExecution;

	private SparqlEndpoint sparqlEndpoint;

	private String json = null;

	/**
	 * Standard constructor.
	 * 
	 * @param sparqlQueryString A SPARQL query string
	 * @param sparqlEndpoint An Endpoint object
	 */
	public SparqlQuery(String sparqlQueryString, SparqlEndpoint sparqlEndpoint) {
		// QUALITY there seems to be a bug in ontowiki
		this.sparqlQueryString = sparqlQueryString.replaceAll("\n", " ");
		this.sparqlEndpoint = sparqlEndpoint;
	}

	/**
	 * Sends a SPARQL query using the Jena library. main format is JSON, use
	 * method getasjson
	 * 
	 */
	public void send(){
		wasExecuted = true;
		// isRunning = true;
		
		ResultSet rs;

		writeToSparqlLog("***********\nNew Query:");
		writeToSparqlLog(sparqlQueryString);
		writeToSparqlLog(sparqlEndpoint.getURL().toString());

		String service = sparqlEndpoint.getURL().toString();

		// Jena access to SPARQL endpoint
		queryExecution = new QueryEngineHTTP(service, sparqlQueryString);
		//System.out.println(sparqlEndpoint.getDefaultGraphURIs());
		
		for (String dgu : sparqlEndpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : sparqlEndpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}
		// TODO remove after overnext Jena release
		HttpQuery.urlLimit = 3 * 1024;
		JamonMonitorLogger.getTimeMonitor(SparqlQuery.class, "httpTime")
				.start();
		//TODO correct Bug: when there is a & in the result like in the
		//URL: http://www.discusmedia.com/catalog.php?catID=5.2.2&profile=map
		//the XML Parser throws an error, because he thinks &profile is an html entitie
		//but it doesn't end with an ;
		//the & must be masked to an &amp; but I am not sure at the moment how to do that
		try{
			
			logger.debug("sending query: length: " + sparqlQueryString.length() + " | ENDPOINT: "
					+ sparqlEndpoint.getURL().toString());
			rs = queryExecution.execSelect();
		
		

			json = SparqlQuery.convertResultSetToJSON(ResultSetFactory.makeRewindable(rs));
			//writeToSparqlLog("JSON: " + json);
		}catch (HTTPException e) {
			logger.warn("Exception in SparqlQuery\n"+ e.toString());
			logger.warn("query was "+ sparqlQueryString);
			writeToSparqlLog("ERROR: HTTPException occured"+ e.toString());
			writeToSparqlLog("ERROR: query was: "+sparqlQueryString);
			throw e;
		}catch (RuntimeException e) {
			//if (!(e instanceof HTTPException)) {
				logger.warn("RuntimeException in SparqlQuery"+ e.toString());
				writeToSparqlLog("ERROR: HTTPException occured"+ e.toString());
				writeToSparqlLog("ERROR: query was: "+sparqlQueryString);
			//}
			throw e;
		}
		
		// there is a minor issue here: Jamon now also measures ResultsetConversion
		// the code would need a second try catch block to handle it correctly
		JamonMonitorLogger.getTimeMonitor(SparqlQuery.class, "httpTime").stop();
		isRunning = false;
		
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
	 * @return the Jena QueryEngineHTTP
	 */
	public QueryEngineHTTP getExecution() {
		return queryExecution;
	}

	/**
	 * insert a result, e.g. from the cache
	 * @param json a jsonString
	 */
	public void setJson(String json) {
		this.wasExecuted = true;
		this.json = json;
	}

	/**
	 * @param running s.e.
	 */
	public void setRunning(boolean running) {
		this.isRunning = running;
	}

	/**
	 * returns the Result of the query as JSON string executes the query if it
	 * wasn't executed before.
	 * 
	 * @return a JSON string
	 */
	public String getJson() {
		if (!wasExecuted) {
			this.send();
		}
		return json;
	}

	/**
	 * makes a ResultSet from the Json String, depends on getJSON.
	 * 
	 * @return a Jena ResultSet
	 */
	public ResultSet getResultSet() {
		return (getJson() == null) ? null : convertJSONtoResultSet(json);
	}

	/**
	 * makes an XML String from the Json String, depends on getJSON.
	 * 
	 * @return An XML String
	 */
	public String getXMLString() {
		return (getJson() == null) ? null : convertJSONtoXML(json);
	}

	/**
	 * Special log for debugging SPARQL query execution.
	 * It lives here: "log/sparql.txt"
	 * if the directory doesn't exist, there could be an error.
	 * @param s the String to log
	 */
	public static void writeToSparqlLog(String s) {
		try {
			
			FileWriter fw = new FileWriter("log/sparql.txt", logDeletedOnStart);
			logDeletedOnStart = true;
			fw.write(s + "\n");
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
			// make the e object more special FileNotFound??
			//new File("log").mkdir();
			//writeToSparqlLog(s);
		}
	}


	/**
	 * Converts Jena result set to XML.
	 * To make a ResultSet rewindable use:
	 * ResultSetRewindable rsRewind = ResultSetFactory.makeRewindable(resultSet);
	 * @param resultSet  The result set to transform, must be rewindable to prevent errors.
	 * @return String xml
	 */
	public static String convertResultSetToXMLString(ResultSetRewindable resultSet) {
		// if (rs == null)
		// this.send();
		String retVal = ResultSetFormatter.asXMLString(resultSet);
		resultSet.reset();
		return retVal;
	}

	/**
	 * Converts Jena result set to JSON.
	 * To make a ResultSet rewindable use:
	 * ResultSetRewindable rsRewind = ResultSetFactory.makeRewindable(resultSet);
	 * 
	 * @param resultSet
	 *            The result set to transform, must be rewindable to prevent errors.
	 * @return JSON representation of the result set.
	 */
	public static String convertResultSetToJSON(ResultSetRewindable resultSet) {
		// if (rs == null)
		// this.send();
		//ResultSetRewindable rsRewind = ResultSetFactory.makeRewindable(resultSet);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ResultSetFormatter.outputAsJSON(baos, resultSet);
		// possible Jena bug: Jena modifies the result set during
		// JSON transformation, so we need to get it back
		//rsRewind.
		//resultSet = convertJSONtoResultSet(baos.toString());
		resultSet.reset();
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
