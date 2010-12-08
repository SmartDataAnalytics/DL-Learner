package org.dllearner.autosparql.server.evaluation;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.server.ExampleFinder;
import org.dllearner.autosparql.server.Generalisation;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGeneratorImpl;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class EvaluationScript {
	
	private static final Logger logger = Logger.getLogger(EvaluationScript.class);

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws SPARQLQueryException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, SPARQLQueryException, IOException {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		FileAppender fileAppender = new FileAppender(
				layout, "log/evaluation.log", false);
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.INFO);
		Logger.getLogger(ModelGenerator.class).setLevel(Level.OFF);
		Logger.getLogger(SPARQLQueryGeneratorImpl.class).setLevel(Level.OFF);
		Logger.getLogger(LGGGeneratorImpl.class).setLevel(Level.OFF);
		Logger.getLogger(NBRGeneratorImpl.class).setLevel(Level.OFF);
		Logger.getLogger(Generalisation.class).setLevel(Level.OFF);
		
		
		SPARQLEndpointEx endpoint = new SPARQLEndpointEx(
				new URL("http://db0.aksw.org:8999/sparql"),
				Collections.singletonList("http://dbpedia.org"),
				Collections.<String>emptyList(),
				null, null,
				Collections.<String>emptyList());
		ExtractionDBCache selectQueriesCache = new ExtractionDBCache("evaluation/select-cache");
		ExtractionDBCache constructQueriesCache = new ExtractionDBCache("evaluation/construct-cache");
		
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://139.18.2.173/dbpedia_queries", "root", "WQPRisDa2");
		
		
		//fetch all queries from table 'tmp', where the number of results is lower than 2000
		Statement st = conn.createStatement();
		ResultSet queries = st.executeQuery("SELECT * FROM queries_final WHERE resultCount<2000 ORDER BY resultCount DESC");
		queries.last();
		logger.info("Evaluating " + queries.getRow() + " queries.");
		queries.beforeFirst();
		
		
		int id;
		String query;
		QueryEngineHTTP qexec;
		com.hp.hpl.jena.query.ResultSet rs;
		SortedSet<String> resources;
		QuerySolution qs;
		ExampleFinder exampleFinder;
		List<String> posExamples;
		List<String> negExamples;
		//iterate over the queries
		while(queries.next()){
			id = queries.getInt("id");
			query = queries.getString("query");
			logger.info("Evaluating query:\n" + query);
			
			
			//send query to SPARQLEndpoint
			qexec = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			for (String dgu : endpoint.getDefaultGraphURIs()) {
				qexec.addDefaultGraph(dgu);
			}
			for (String ngu : endpoint.getNamedGraphURIs()) {
				qexec.addNamedGraph(ngu);
			}		
			rs = qexec.execSelect();
			
			
			//put the URIs for the resources in variable var0 into a separate list
			resources = new TreeSet<String>();
			while(rs.hasNext()){
				qs = rs.next();
				if(qs.get("var0").isURIResource()){
					resources.add(qs.get("var0").asResource().getURI());
				}
			}
			logger.info("Query returned " + resources.size() + " results:\n" + resources);
			
			
			//start learning
			exampleFinder = new ExampleFinder(endpoint, selectQueriesCache, constructQueriesCache);
			posExamples = new ArrayList<String>();
			negExamples = new ArrayList<String>();
			//we choose the first resource in the set as positive example
			String posExample = resources.first();
			logger.info("Selected " + posExample + " as first positive example.");
			posExamples.add(posExample);
			//we ask for the next similar example
//			String nextExample = exampleFinder.findSimilarExample(posExamples, negExamples).getURI();
//			logger.info("Next suggested example is " + nextExample);
//			//if the example is contained in the resultset of the query, we add it to the positive examples,
//			//otherwise to the negatives
//			if(resources.contains(nextExample)){
//				posExamples.add(nextExample);
//				logger.info("Suggested example is considered as positive example.");
//			} else {
//				negExamples.add(nextExample);
//				logger.info("Suggested example is considered as negative example.");
//			}
//			nextExample = exampleFinder.findSimilarExample(posExamples, negExamples).getURI();
			
			String nextExample;
			String learnedQuery;
			boolean equivalentQueries = false;
			do{
				nextExample = exampleFinder.findSimilarExample(posExamples, negExamples).getURI();
				logger.info("Next suggested example is " + nextExample);
				//if the example is contained in the resultset of the query, we add it to the positive examples,
				//otherwise to the negatives
				if(resources.contains(nextExample)){
					posExamples.add(nextExample);
					logger.info("Suggested example is considered as positive example.");
				} else {
					negExamples.add(nextExample);
					logger.info("Suggested example is considered as negative example.");
				}
				learnedQuery = exampleFinder.getCurrentQuery();
				logger.info("Learned query:\n" + learnedQuery);
				equivalentQueries = isEquivalentQuery(resources, learnedQuery, endpoint);
				logger.info("Original query and learned query are equivalent: " + equivalentQueries);
			} while(!equivalentQueries);
			
			logger.info("Number of examples needed: " + (posExamples.size() + negExamples.size()));
			
			break;
		}

	}
	
	/**
	 * Check if resultset of the learned query is equivalent to the resultset of the original query
	 * @param originalResources
	 * @param query
	 * @param endpoint
	 * @return
	 */
	private static boolean isEquivalentQuery(SortedSet<String> originalResources, String query, SparqlEndpoint endpoint){
		QueryEngineHTTP qexec = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			qexec.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			qexec.addNamedGraph(ngu);
		}		
		com.hp.hpl.jena.query.ResultSet rs = qexec.execSelect();
		
		SortedSet<String> learnedResources = new TreeSet<String>();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("x0").isURIResource()){
				learnedResources.add(qs.get("x0").asResource().getURI());
			}
		}
		logger.info("Number of resources in original query: " + originalResources.size());
		logger.info("Number of resources in learned query: " + learnedResources.size());
		return originalResources.equals(learnedResources);
	}

}
