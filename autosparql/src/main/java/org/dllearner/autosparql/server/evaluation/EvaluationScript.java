package org.dllearner.autosparql.server.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;


public class EvaluationScript {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		
		Class.forName("com.mysql.jdbc.Driver");
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://139.18.2.173/dbpedia_queries", "root", "WQPRisDa2");
		
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("UPDATE 'SELECT_queries' SET resultCount = ? WHERE id = ?");
		
		ResultSet rs = st.executeQuery("SELECT * FROM `SELECT_queries`");
		
		int id;
		String query;
		int frequency;
		QueryEngineHTTP qexec;
		com.hp.hpl.jena.query.ResultSet rs_jena;
		int rowCount = 0;
		while(rs.next()){
			id = rs.getInt("id");
			query = rs.getString("query");
			frequency = rs.getInt("frequency");
			
			try {
				qexec = new QueryEngineHTTP(endpoint.getURL().toString(), query);
				for (String dgu : endpoint.getDefaultGraphURIs()) {
					qexec.addDefaultGraph(dgu);
				}
				for (String ngu : endpoint.getNamedGraphURIs()) {
					qexec.addNamedGraph(ngu);
				}		
				rs_jena = qexec.execSelect();
				
				if(rs_jena.hasNext()){
					rowCount = 0;
					while(rs_jena.hasNext()){
						rs_jena.next();
						rowCount++;
					}
					
					ps.setInt(1, rowCount);
					ps.setInt(2, id);
					ps.execute();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		
	}

}
