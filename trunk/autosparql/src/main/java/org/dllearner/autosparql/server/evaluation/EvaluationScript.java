package org.dllearner.autosparql.server.evaluation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.autosparql.server.cache.DBModelCacheExtended;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;


public class EvaluationScript {
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		FileAppender fileAppender = new FileAppender(
				layout, "log/filterQueriesScriptExecution.log", false);
		// FileAppender fileAppender = new FileAppender( layout,
		// "log/fillCache_" + databaseType + ".log", false );
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.INFO);
		Logger.getLogger(DBModelCacheExtended.class).setLevel(Level.INFO);
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		
		Class.forName("com.mysql.jdbc.Driver");
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://139.18.2.173/dbpedia_queries", "root", "WQPRisDa2");
		
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("UPDATE 'SELECT_queries' SET resultCount = ? WHERE id = ?");
		
		ResultSet rs = st.executeQuery("SELECT * FROM `SELECT_queries`");
		
		int id;
		String query;
		QueryEngineHTTP qexec;
		com.hp.hpl.jena.query.ResultSet rs_jena;
		int rowCount = 0;
		while(rs.next()){
			id = rs.getInt("id");
			query = rs.getString("query");
			
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
				logger.error("ERROR. An error occured while working with query " + id, e);
			}
			
		}
		
		
	}

}
