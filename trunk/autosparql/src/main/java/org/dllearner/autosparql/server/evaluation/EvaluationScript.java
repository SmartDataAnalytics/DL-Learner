package org.dllearner.autosparql.server.evaluation;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class EvaluationScript {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, MalformedURLException {
		SparqlEndpoint endpoint = new SparqlEndpoint(
				new URL("http://db0.aksw.org:8999/sparql"),
				Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
		
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://139.18.2.173/dbpedia_queries", "root", "WQPRisDa2");
		
		Statement st = conn.createStatement();
		
		//fetch all queries from table 'tmp', where the number of results is lower than 2000
		ResultSet rs = st.executeQuery("SELECT * FROM tmp WHERE resultCount<2000");
		
		int id;
		String query;
		QueryEngineHTTP qexec;
		com.hp.hpl.jena.query.ResultSet rs_jena;
		//iterate over the queries
		while(rs.next()){
			id = rs.getInt("id");
			query = rs.getString("query");
			
			//send query to SPARQLEndpoint
			qexec = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			for (String dgu : endpoint.getDefaultGraphURIs()) {
				qexec.addDefaultGraph(dgu);
			}
			for (String ngu : endpoint.getNamedGraphURIs()) {
				qexec.addNamedGraph(ngu);
			}		
			rs_jena = qexec.execSelect();
		
		
		}

	}

}
