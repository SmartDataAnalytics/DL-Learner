package org.dllearner.autosparql.server.evaluation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.GreedyNBRStrategy;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.ini4j.IniFile;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.jamonapi.MonitorFactory;

public class EvaluationScript {
	
	private static final Logger logger = Logger.getLogger(EvaluationScript.class);

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws SPARQLQueryException 
	 * @throws IOException 
	 * @throws BackingStoreException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, SPARQLQueryException, IOException, BackingStoreException {
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
		Logger.getLogger(GreedyNBRStrategy.class).setLevel(Level.OFF);
		Logger.getLogger(Generalisation.class).setLevel(Level.OFF);
		Logger.getLogger(ExampleFinder.class).setLevel(Level.INFO);
		
		
		SPARQLEndpointEx endpoint = new SPARQLEndpointEx(
//				new URL("http://dbpedia.org/sparql"),
				new URL("http://db0.aksw.org:8999/sparql"),
				Collections.singletonList("http://dbpedia.org"),
				Collections.<String>emptyList(),
				null, null,
				Collections.<String>emptyList());
		ExtractionDBCache selectQueriesCache = new ExtractionDBCache("evaluation/select-cache");
		ExtractionDBCache constructQueriesCache = new ExtractionDBCache("evaluation/construct-cache");
		
		String iniFile = "settings.ini";
		Preferences prefs = new IniFile(new File(iniFile));
		String dbServer = prefs.node("database").get("server", null);
		String dbName = prefs.node("database").get("name", null);
		String dbUser = prefs.node("database").get("user", null);
		String dbPass = prefs.node("database").get("pass", null);
		
		Class.forName("com.mysql.jdbc.Driver");
		String url =
            "jdbc:mysql://"+dbServer+"/"+dbName;
		Connection conn = DriverManager.getConnection(url, dbUser, dbPass);
		PreparedStatement ps = conn.prepareStatement("INSERT INTO evaluation (" +
				"id, original_query, learned_query, triple_pattern_count," +
				"examples_needed, pos_examples_needed, neg_examples_needed," +
				"total_time_in_ms, query_time_in_ms, query_time_avg, lgg_time_in_ms, lgg_time_avg, nbr_time_in_ms, nbr_time_avg) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		
		//fetch all queries from table 'tmp', where the number of results is lower than 2000
		Statement st = conn.createStatement();
		ResultSet queries = st.executeQuery("SELECT * FROM queries_final WHERE resultCount<2000 AND query not like '%filter%' ORDER BY resultCount DESC");
		queries.last();
		logger.info("Evaluating " + queries.getRow() + " queries.");
		queries.beforeFirst();
		
		
		int id;
		String query;
		com.hp.hpl.jena.query.ResultSet rs;
		SortedSet<String> resources;
		QuerySolution qs;
		ExampleFinder exampleFinder;
		List<String> posExamples;
		List<String> negExamples;
		//iterate over the queries
		int testedCnt = 0;
		int learnedCnt = 0;
		int mostGeneralQueryCount = 0;
		String mostGeneralQuery = "SELECT ?x0 WHERE {?x0 ?y ?z.}";
		boolean failed = false;
		while(queries.next()){
			id = queries.getInt("id");
			query = queries.getString("query");
			logger.info("Evaluating query:\n" + query);
			testedCnt++;
			MonitorFactory.getTimeMonitor("Query").reset();
			MonitorFactory.getTimeMonitor("LGG").reset();
			MonitorFactory.getTimeMonitor("NBR").reset();
			mostGeneralQueryCount = 0;
			failed = false;
			try {
				//send query to SPARQLEndpoint
				rs = SparqlQuery.convertJSONtoResultSet(selectQueriesCache.executeSelectQuery(endpoint, query));
				
				
				//put the URIs for the resources in variable var0 into a separate list
				resources = new TreeSet<String>();
				while(rs.hasNext()){
					qs = rs.next();
					if(qs.get("var0").isURIResource()){
						resources.add(qs.get("var0").asResource().getURI());
					}
				}
				logger.info("Query returned " + resources.size() + " results");
//				logger.info(resources);
				
				
				//start learning
				exampleFinder = new ExampleFinder(endpoint, selectQueriesCache, constructQueriesCache);
				posExamples = new ArrayList<String>();
				negExamples = new ArrayList<String>();
				//we choose the first resource in the set as positive example
				String posExample = resources.first();
				logger.info("Selected " + posExample + " as first positive example.");
				posExamples.add(posExample);
				//we ask for the next similar example
				String nextExample;
				String learnedQuery = "";
				boolean equivalentQueries = false;
				do{
					nextExample = exampleFinder.findSimilarExample(posExamples, negExamples).getURI();
					learnedQuery = exampleFinder.getCurrentQuery();
					logger.info("Learned query:\n" + learnedQuery);
					equivalentQueries = isEquivalentQuery(resources, learnedQuery, endpoint);
					logger.info("Original query and learned query are equivalent: " + equivalentQueries);
					if(equivalentQueries){
						break;
					}
					
					
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
					
					if(learnedQuery.equals(mostGeneralQuery)){
						mostGeneralQueryCount++;
					} else {
						mostGeneralQueryCount = 0;
					}
					if(mostGeneralQueryCount == 10){
						logger.info("Breaking because seems to be that we run into an infinite loop");
						failed = true;
						break;
					}
					
					
				} while(!equivalentQueries);
				
				if(!failed){
					int triplePatternCount = ((QueryTreeImpl<String>)exampleFinder.getCurrentQueryTree()).getTriplePatternCount();
					int posExamplesCount = posExamples.size();
					int negExamplesCount = negExamples.size();
					int examplesCount = posExamplesCount + negExamplesCount;
					double queryTime = MonitorFactory.getTimeMonitor("Query").getTotal();
					double lggTime = MonitorFactory.getTimeMonitor("LGG").getTotal();
					double nbrTime = MonitorFactory.getTimeMonitor("NBR").getTotal();
					double queryTimeAvg = MonitorFactory.getTimeMonitor("Query").getAvg();
					double lggTimeAvg = MonitorFactory.getTimeMonitor("LGG").getAvg();
					double nbrTimeAvg = MonitorFactory.getTimeMonitor("NBR").getAvg();
					double totalTime = queryTime + nbrTime + lggTime;
					
					write2DB(ps, id, query, learnedQuery, triplePatternCount,
							examplesCount, posExamplesCount, negExamplesCount,
							totalTime, queryTime, queryTimeAvg, lggTime, lggTimeAvg, nbrTime, nbrTimeAvg);
					logger.info("Number of examples needed: " 
							+ (posExamples.size() + negExamples.size()) 
							+ "(+" + posExamples.size() + "/-" + negExamples.size() + ")");
					learnedCnt++;
				}
				
//			if(testedCnt == 50){
//				break;
//			}
			} catch (Exception e) {
				logger.error("Error while learning query " + id, e);
			}
		}
		logger.info("Learned " + learnedCnt + " of " + testedCnt + " queries");
		logger.info(MonitorFactory.getTimeMonitor("Query"));
		logger.info(MonitorFactory.getTimeMonitor("LGG"));
		logger.info(MonitorFactory.getTimeMonitor("NBR"));

	}
	
	private static void write2DB(PreparedStatement ps, 
			int id, String originalQuery, String learnedQuery, int triplePatternCount,
			int examplesCount, int posExamplesCount, int negExamplesCount,
			double totalTime, double queryTime, double queryTimeAvg, 
			double lggTime, double lggTimeAvg, double nbrTime, double nbrTimeAvg){
		try {
			ps.setInt(1, id);
			ps.setString(2, originalQuery);
			ps.setString(3, learnedQuery);
			ps.setInt(4, triplePatternCount);
			ps.setInt(5, examplesCount);
			ps.setInt(6, posExamplesCount);
			ps.setInt(7, negExamplesCount);
			ps.setDouble(8, totalTime);
			ps.setDouble(9, queryTime);
			ps.setDouble(10, queryTimeAvg);
			ps.setDouble(11, lggTime);
			ps.setDouble(12, lggTimeAvg);
			ps.setDouble(13, nbrTime);
			ps.setDouble(14, nbrTimeAvg);
			
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error while writing to DB.",e);
			e.printStackTrace();
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
		if(query.equals("SELECT ?x0 WHERE {?x0 ?y ?z.}")){
			return false;
		}
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
