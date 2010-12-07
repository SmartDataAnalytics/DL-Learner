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

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;


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
			
			// @Lorenz: Code ungetestet
			if(checkQuerySimple(query)) {
			
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
						//we increment the counter only if the result is a URI resource, and not a literal or blanknode
						if(rs_jena.next().get("var0").isURIResource()){
							rowCount++;
						}
					}
					//we only write to database if there are min 1 resources in the resultset
					if(rowCount > 0){
						ps.setInt(1, rowCount);
						ps.setInt(2, id);
						ps.execute();
					}
				}
			} catch (Exception e) {
				logger.error("ERROR. An error occured while working with query " + id, e);
			}
			
			}
		}
		
		
	}
	
	// checks whether query is obviously not learnable
	private static boolean checkQuerySimple(String query) {
		if(query.contains("UNION")) {
			return false;
		}	
		return true;
	}
	
	private static boolean checkQuerySyntax(String query) {
		Query q = QueryFactory.create(query);
		Op op = Algebra.compile(q);
		// ... perform checks ... can we fully decide when an algebra expression is not in the target language?
		SSE.write(op) ;
		return true;
	}
	
	private static boolean checkTargetVarIsSubject(String queryString){
		Query query = QueryFactory.create(queryString);
		Element queryPattern = query.getQueryPattern();
//		System.out.println(queryPattern);
		if(queryPattern instanceof ElementGroup){
			for(Element element : ((ElementGroup) queryPattern).getElements()){
				if(element instanceof ElementTriplesBlock){
					BasicPattern triples = ((ElementTriplesBlock) element).getPattern();
					for(Triple triple : triples){
						System.out.println(triple);
						if(triple.getObject().isVariable()){
							System.out.println(triple.getObject().getName());
							System.out.println("Has to be filtered.");
						} else {
							
							System.out.println(triple.getObject().isVariable());
						}
						
					}
				}
			}
		}
		return true;
	}

}
