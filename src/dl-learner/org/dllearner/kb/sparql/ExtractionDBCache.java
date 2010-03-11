/**
 * Copyright (C) 2007-2010, Jens Lehmann
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * The class is used to cache information about resources to a database.
 * Provides the connection to an H2 database in a light weight, configuration free
 * manner. 
 * 
 * @author Jens Lehmann
 *
 */
public class ExtractionDBCache {

	private String databaseDirectory = "cache";
	private String databaseName = "extraction";
	
	// specifies after how many seconds a cached result becomes invalid
	private long freshnessSeconds = 15 * 24 * 60 * 60; // 15 days	
	
	private Connection conn;
	
	MessageDigest md5;
	
	public ExtractionDBCache() throws ClassNotFoundException, SQLException, NoSuchAlgorithmException {
		md5 = MessageDigest.getInstance("MD5");
		
		// load driver
		Class.forName("org.h2.Driver");
		
		// connect to database (created automatically if not existing)
        conn = DriverManager.getConnection("jdbc:h2:"+databaseDirectory+"/"+databaseName, "sa", "");

        // create cache table if it does not exist
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS QUERY_CACHE(QUERYHASH BINARY PRIMARY KEY,QUERY VARCHAR(255), TRIPLES CLOB, STORE_TIME TIMESTAMP)");
	}
	
	public Model executeConstructQuery(String query) throws SQLException, UnsupportedEncodingException {
		byte[] md5 = md5(query);
//		Timestamp currTS = new Timestamp(new java.util.Date().getTime());
		PreparedStatement ps=conn.prepareStatement("SELECT * FROM QUERY_CACHE WHERE QUERYHASH=? LIMIT 1");
		ps.setBytes(1, md5);
		ResultSet rs = ps.executeQuery();
		
		boolean readFromCache = rs.next() && (rs.getTimestamp("STORE_TIME").getTime() - System.currentTimeMillis() < freshnessSeconds);
		
		if(readFromCache) {
//			System.out.println("Reading from cache");
//			String posedQuery = rs.getString("QUERY");
//			System.out.println(posedQuery);
			Clob clob = rs.getClob("TRIPLES");
			Model readModel = ModelFactory.createDefaultModel();
			readModel.read(clob.getAsciiStream(), null, "N-TRIPLE");
			return readModel;
		} else {
//			System.out.println("Posing new query");
			
			String endpoint = "http://139.18.2.37:8890/sparql";
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint, query);
			queryExecution.addDefaultGraph("http://dbpedia.org");
			Model m2 = queryExecution.execConstruct();	
			
			// convert model to N-Triples
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			m2.write(baos, "N-TRIPLE");
			String modelStr = baos.toString("UTF-8");
			
			// use a prepared statement, so that Java handles all the escaping stuff correctly automatically
			PreparedStatement ps2=conn.prepareStatement("INSERT INTO QUERY_CACHE VALUES(?,?,?,?)");
			ps2.setBytes(1, md5);
			ps2.setString(2, query);
			ps2.setClob(3, new StringReader(modelStr));
			ps2.setTimestamp(4, new java.sql.Timestamp(new java.util.Date().getTime()));
			ps2.executeUpdate(); 
			
			return m2;
		}
	}
	
	public void closeConnection() throws SQLException {
		conn.close();
	}
	
	private byte[] md5(String string) {
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
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {
		String resource = "http://dbpedia.org/resource/Leipzig";
		String query = "CONSTRUCT { <"+resource+"> ?p ?o } WHERE { <"+resource+"> ?p ?o }"; 
		System.out.println("query: " + query);
		
		ExtractionDBCache h2 = new ExtractionDBCache(); 
		long startTime = System.nanoTime();
		Model m = h2.executeConstructQuery(query);
		long runTime = System.nanoTime() - startTime;
		System.out.println("Answer obtained in " + Helper.prettyPrintNanoSeconds(runTime));
		System.out.println(ExtractionDBCache.toNTriple(m));
	}	

}
