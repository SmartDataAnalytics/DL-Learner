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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.dllearner.utilities.Helper;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * The class is used to cache information about resources to a database.
 * Provides the connection to an H2 database in a light weight, configuration free
 * manner. 
 * 
 * Note: Currently, either select ot construct has to be used (not both).
 * 
 * @author Jens Lehmann
 *
 */
public class ExtractionDBCache {

	private String databaseDirectory = "cache";
	private String databaseName = "extraction";
	private boolean autoServerMode = true;
	
	// specifies after how many seconds a cached result becomes invalid
	private long freshnessInMilliseconds = 15 * 24 * 60 * 60 * 1000; // 15 days	
	
	private int maxExecutionTimeInSeconds = 0;
	
	private Connection conn;
	
	MessageDigest md5;
	
	private Monitor mon = MonitorFactory.getTimeMonitor("Query");
	
	public ExtractionDBCache(Connection conn) throws SQLException
	{
		this.conn=conn;
		try{md5 = MessageDigest.getInstance("MD5");}
		catch(NoSuchAlgorithmException e) {throw new RuntimeException("Should never happen - MD5 not found.");}
	        // create cache table if it does not exist
		Statement stmt = conn.createStatement();
		stmt.execute("CREATE TABLE IF NOT EXISTS QUERY_CACHE(QUERYHASH BINARY PRIMARY KEY,QUERY VARCHAR(20000), TRIPLES CLOB, STORE_TIME TIMESTAMP)");		
	}

	public ExtractionDBCache(String cacheDir) {
		databaseDirectory = cacheDir;
		try {
		md5 = MessageDigest.getInstance("MD5");
		
		// load driver
		Class.forName("org.h2.Driver");
		
		String jdbcString = "";
		if(autoServerMode) {
			jdbcString = ";AUTO_SERVER=TRUE";
		}
		
		// connect to database (created automatically if not existing)
        conn = DriverManager.getConnection("jdbc:h2:"+databaseDirectory+"/"+databaseName+jdbcString, "sa", "");

        // create cache table if it does not exist
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS QUERY_CACHE(QUERYHASH BINARY PRIMARY KEY,QUERY VARCHAR(20000), TRIPLES CLOB, STORE_TIME TIMESTAMP)");
		} catch(NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setFreshnessInMilliseconds(long freshnessInMilliseconds) {
		this.freshnessInMilliseconds = freshnessInMilliseconds;
	}
	
	public Model executeConstructQuery(SparqlEndpoint endpoint, String query) throws SQLException, UnsupportedEncodingException {
		return executeConstructQuery(endpoint, query, maxExecutionTimeInSeconds);
	}
	
	public Model executeConstructQuery(SparqlEndpoint endpoint, String query, int maxExecutionTimeInSeconds) throws SQLException, UnsupportedEncodingException {
		byte[] md5 = md5(query);		
//		Timestamp currTS = new Timestamp(new java.util.Date().getTime());
		PreparedStatement ps=conn.prepareStatement("SELECT * FROM QUERY_CACHE WHERE QUERYHASH=? LIMIT 1");
		ps.setBytes(1, md5);
		ResultSet rs = ps.executeQuery();
		
//		long startTime = System.nanoTime();
		boolean entryExists = rs.next();
		boolean readFromCache = entryExists && (System.currentTimeMillis() - rs.getTimestamp("STORE_TIME").getTime() < freshnessInMilliseconds);
//		long runTime = System.nanoTime() - startTime;
//		System.out.println(Helper.prettyPrintNanoSeconds(runTime, true, true));
		
		if(readFromCache) {
//			System.out.println("Reading from cache");
//			String posedQuery = rs.getString("QUERY");
//			System.out.println(posedQuery);
				
			Clob clob = rs.getClob("TRIPLES");
//			long startTime = System.nanoTime();
			Model readModel = ModelFactory.createDefaultModel();
			readModel.read(clob.getAsciiStream(), null, "N-TRIPLE");
//			long runTime = System.nanoTime() - startTime;
//			System.out.println(Helper.prettyPrintNanoSeconds(runTime, true, true));			
			return readModel;
		} else {
			mon.start();
//			System.out.println("Posing new query");
			
//			String endpoint = "http://139.18.2.37:8890/sparql";
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			queryExecution.setTimeout(maxExecutionTimeInSeconds * 1000);
			for (String dgu : endpoint.getDefaultGraphURIs()) {
				queryExecution.addDefaultGraph(dgu);
			}
			for (String ngu : endpoint.getNamedGraphURIs()) {
				queryExecution.addNamedGraph(ngu);
			}			
			Model m2 = queryExecution.execConstruct();	
			
			// convert model to N-Triples
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			m2.write(baos, "N-TRIPLE");
			String modelStr = baos.toString("UTF-8");
			
			// use a prepared statement, so that Java handles all the escaping stuff correctly automatically
			PreparedStatement ps2;
			if(entryExists){
				ps2 = conn.prepareStatement("UPDATE QUERY_CACHE SET TRIPLES=?, STORE_TIME=? WHERE QUERYHASH=?");
				ps2.setClob(1, new StringReader(modelStr));
				ps2.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
				ps2.setBytes(3, md5);
			} else {
				ps2 = conn.prepareStatement("INSERT INTO QUERY_CACHE VALUES(?,?,?,?)");
				ps2.setBytes(1, md5);
				ps2.setString(2, query);
				ps2.setClob(3, new StringReader(modelStr));
				ps2.setTimestamp(4, new java.sql.Timestamp(new java.util.Date().getTime()));
			}
			mon.stop();
			ps2.executeUpdate(); 
			
			return m2;
		}
	}
	
	public String executeSelectQuery(SparqlEndpoint endpoint, String query) {
		return executeSelectQuery(endpoint, query, maxExecutionTimeInSeconds);
	}
	
	public String executeSelectQuery(SparqlEndpoint endpoint, String query, int maxExecutionTimeInSeconds) {
		
		try {
			
			
		byte[] md5 = md5(query);		
		PreparedStatement ps=conn.prepareStatement("SELECT * FROM QUERY_CACHE WHERE QUERYHASH=? LIMIT 1");
		ps.setBytes(1, md5);
		ResultSet rs = ps.executeQuery();
		
		boolean entryExists = rs.next();
		boolean readFromCache = entryExists && (System.currentTimeMillis() - rs.getTimestamp("STORE_TIME").getTime() < freshnessInMilliseconds);
		
		if(readFromCache) {
//			System.out.println("cache");
			Clob clob = rs.getClob("TRIPLES");
			return clob.getSubString(1, (int) clob.length());
		} else {
			mon.start();
//			System.out.println("no-cache");
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			queryExecution.setTimeout(maxExecutionTimeInSeconds * 1000);
			for (String dgu : endpoint.getDefaultGraphURIs()) {
				queryExecution.addDefaultGraph(dgu);
			}
			for (String ngu : endpoint.getNamedGraphURIs()) {
				queryExecution.addNamedGraph(ngu);
			}
			com.hp.hpl.jena.query.ResultSet tmp = queryExecution.execSelect();
			ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(tmp);
			String json = SparqlQuery.convertResultSetToJSON(rs2);
			
			// use a prepared statement, so that Java handles all the escaping stuff correctly automatically
			PreparedStatement ps2;
			if(entryExists){
				ps2 = conn.prepareStatement("UPDATE QUERY_CACHE SET TRIPLES=?, STORE_TIME=? WHERE QUERYHASH=?");
				ps2.setClob(1, new StringReader(json));
				ps2.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
				ps2.setBytes(3, md5);
			} else {
				ps2 = conn.prepareStatement("INSERT INTO QUERY_CACHE VALUES(?,?,?,?)");
				ps2.setBytes(1, md5);
				ps2.setString(2, query);
				ps2.setClob(3, new StringReader(json));
				ps2.setTimestamp(4, new java.sql.Timestamp(new java.util.Date().getTime()));
			}
			mon.stop();
			ps2.executeUpdate(); 
			return json;
		}
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		} finally{
			mon.stop();
		}
	}	
	
	public void closeConnection() throws SQLException {
		conn.close();
	}
	
	private synchronized byte[] md5(String string) {
		md5.reset();
		md5.update(string.getBytes());
		return md5.digest();
	}
	
	public static String toNTriple(Model m) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		m.write(baos, "N-TRIPLE");
		try {
			return baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds){
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
		String resource = "http://dbpedia.org/resource/Leipzig";
		String query = "CONSTRUCT { <"+resource+"> ?p ?o } WHERE { <"+resource+"> ?p ?o }"; 
		System.out.println("query: " + query);
		
		ExtractionDBCache h2 = new ExtractionDBCache("cache"); 
		long startTime = System.nanoTime();
		Model m = h2.executeConstructQuery(endpoint, query);
//		for(int i=0; i<1000; i++) {
//			h2.executeConstructQuery(endpoint, query);
//		}
		long runTime = System.nanoTime() - startTime;
		System.out.println("Answer obtained in " + Helper.prettyPrintNanoSeconds(runTime));
		System.out.println(ExtractionDBCache.toNTriple(m));
	}	

}
