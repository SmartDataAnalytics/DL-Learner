package org.dllearner.autosparql.server.evaluation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;


public class EvaluationScript {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		
		Class.forName("com.mysql.jdbc.Driver");
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://139.18.2.173/dbpedia_queries", "root", "WQPRisDa2");
		
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO 'SELECT_queries_with_resultset' VALUES(?,?,?,?)");
		
		ResultSet rs = st.executeQuery("SELECT * FROM `SELECT_queries`");
		
		int id;
		String query;
		int frequency;
		QueryExecution qexec;
		com.hp.hpl.jena.query.ResultSet rs_jena;
		int rowCount = 0;
		while(rs.next()){
			id = rs.getInt("id");
			query = rs.getString("query");
			frequency = rs.getInt("frequency");
			
			qexec = QueryExecutionFactory.sparqlService(
					endpoint.getURL().toString(),
					query,
					endpoint.getDefaultGraphURIs(),
					endpoint.getNamedGraphURIs());
			rs_jena = qexec.execSelect();
			
			if(rs_jena.hasNext()){
				rowCount = 0;
				while(rs_jena.hasNext()){
					rs_jena.next();
					rowCount++;
				}
				
				ps.setInt(1, id);
				ps.setString(2, query);
				ps.setInt(3, frequency);
				ps.setInt(4, rowCount);
			}
			
		}
		
		
	}

}
