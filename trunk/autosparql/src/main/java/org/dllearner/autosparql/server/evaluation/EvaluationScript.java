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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.collections15.ListUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.server.ExampleFinder;
import org.dllearner.autosparql.server.Generalisation;
import org.dllearner.autosparql.server.NBR;
import org.dllearner.autosparql.server.PostLGG;
import org.dllearner.autosparql.server.exception.TimeOutException;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.cache.ModelCache;
import org.dllearner.sparqlquerygenerator.cache.QueryTreeCache;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGenerator;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.GreedyNBRStrategy;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.ini4j.IniFile;

import com.hp.hpl.jena.query.QuerySolution;
import com.jamonapi.MonitorFactory;

public class EvaluationScript {
	
	private static final Logger logger = Logger.getLogger(EvaluationScript.class);
	
	private static final int maxRepeatedNegativesCount = 10;
	private static final int startingPositiveExamplesCount = 3;
	private static final int maxNBRExecutionTimeInSeconds = 10;
	private static String learnedQuery;
	private static int maxQueryTimeInSeconds = 10;

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
		consoleAppender.setThreshold(Level.INFO);
		FileAppender fileAppender = new FileAppender(
				layout, "log/evaluation.log", false);
		fileAppender.setThreshold(Level.DEBUG);
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
		Logger.getLogger(NBR.class).setLevel(Level.DEBUG);
		Logger.getLogger(PostLGG.class).setLevel(Level.DEBUG);
		
		String baseURI = "http://dbpedia.org/resource/";
		Map<String,String> prefixes = new HashMap<String,String>();
		prefixes.put("dbo","http://dbpedia.org/ontology/");
		prefixes.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		prefixes.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("skos","http://www.w3.org/2004/02/skos/core#");
		prefixes.put("geo","http://www.w3.org/2003/01/geo/wgs84_pos#");
		prefixes.put("georss","http://www.georss.org/georss/");
		prefixes.put("owl","http://www.w3.org/2002/07/owl#");
		prefixes.put("yago","http://dbpedia.org/class/yago/");
		prefixes.put("cyc","http://sw.opencyc.org/concept/");
		List<String> predicateFilters = new ArrayList<String>();
		predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
//		predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		predicateFilters.add("http://dbpedia.org/property/");
		SPARQLEndpointEx endpoint = new SPARQLEndpointEx(
				new URL("http://db0.aksw.org:8999/sparql"),
				Collections.singletonList("http://dbpedia.org"),
				Collections.<String>emptyList(), 
				null, baseURI, prefixes, predicateFilters);
		
		ExtractionDBCache selectQueriesCache = new ExtractionDBCache("evaluation/select-cache");
		selectQueriesCache.setMaxExecutionTimeInSeconds(maxQueryTimeInSeconds);
		ExtractionDBCache constructQueriesCache = new ExtractionDBCache("evaluation/construct-cache");
		constructQueriesCache.setMaxExecutionTimeInSeconds(maxQueryTimeInSeconds);
		
		LGGGenerator<String> lggGen = new LGGGeneratorImpl<String>();
		NBR<String> nbrGen = new NBR<String>(endpoint, selectQueriesCache, constructQueriesCache);
		Generalisation<String> posGen = new Generalisation<String>();
		nbrGen.setMaxExecutionTimeInSeconds(maxNBRExecutionTimeInSeconds);
		ModelGenerator modelGen = new ModelGenerator(endpoint, new HashSet<String>(endpoint.getPredicateFilters()), constructQueriesCache);
		ModelCache modelCache = new ModelCache(modelGen);
		QueryTreeCache treeCache = new QueryTreeCache();
		
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
		SortedSet<String> targetResources;
		ExampleFinder exampleFinder;
		List<String> posExamples;
		List<String> negExamples;
		List<QueryTree<String>> posExampleTrees;
		List<QueryTree<String>> negExampleTrees;
		//iterate over the queries
		int testedCnt = 0;
		int learnedCnt = 0;
		int mostGeneralQueryCount = 0;
		String mostGeneralQuery = "SELECT ?x0 WHERE {?x0 ?y ?z.}";
		boolean failed = false;
		String lastQuery = "";
		int equalsLastQueryCount = 0;
		int repeatedNegativeCount;
		int timeOutErrorCount;
		int repeatedNegativeErrorCount;
		QueryTree<String> lgg = null;
		while(queries.next()){
			lgg = null;
			id = queries.getInt("id");
			query = queries.getString("query");
			logger.info("Evaluating query:\n" + query);
			testedCnt++;
			MonitorFactory.getTimeMonitor("Query").reset();
			MonitorFactory.getTimeMonitor("LGG").reset();
			MonitorFactory.getTimeMonitor("NBR").reset();
			mostGeneralQueryCount = 0;
			equalsLastQueryCount = 0;
			repeatedNegativeCount = 0;
			timeOutErrorCount = 0;
			repeatedNegativeErrorCount = 0;
			lastQuery = "";
			failed = false;
			
			try {
				targetResources = getResources(query, endpoint, selectQueriesCache);
				logger.info("Target query returned " + targetResources.size() + " results");
				
				//start learning
				exampleFinder = new ExampleFinder(endpoint, selectQueriesCache, constructQueriesCache);
				posExamples = new ArrayList<String>();
				negExamples = new ArrayList<String>();
				posExampleTrees = new ArrayList<QueryTree<String>>();
				negExampleTrees = new ArrayList<QueryTree<String>>();
				//we choose the first n resources in the set as positive example
				Iterator<String> iter = targetResources.iterator();
				String posExample;
				for(int i = 0; i < startingPositiveExamplesCount; i++){
					posExample = iter.next();
					posExamples.add(posExample);
					posExampleTrees.add(treeCache.getQueryTree(posExample, modelCache.getModel(posExample)));
				}
				logger.info("Selected " + posExamples + " as first " + startingPositiveExamplesCount + " positive examples.");
				
				//we ask for the next similar example
				String nextExample;
				String learnedQuery = "";
				boolean equivalentQueries = false;
				do{
					try {
//						nextExample = exampleFinder.findSimilarExample(posExamples, negExamples).getURI();
						lgg = lggGen.getLGG(posExampleTrees);
						if(negExamples.isEmpty()){
							nextExample = getExampleByPositiveGeneralisation(lgg, ListUtils.union(posExamples, negExamples), endpoint, selectQueriesCache);
							learnedQuery = EvaluationScript.learnedQuery;
						} else {
							nextExample = nbrGen.getQuestion(lgg, negExampleTrees, ListUtils.union(posExamples, negExamples)).getURI();
							learnedQuery = nbrGen.getQuery();
						}
//						nextExample = nbrGen.getQuestion(lgg, negExampleTrees, ListUtils.union(posExamples, negExamples)).getURI();
//						learnedQuery = nbrGen.getQuery();
					} catch (TimeOutException e) {
						timeOutErrorCount++;
//						QueryTree<String> lgg = exampleFinder.getLGG();
						String newPosExample = getNewPosExampleNotCoveredByLGG(lgg, targetResources, posExamples, endpoint, selectQueriesCache);
						if(newPosExample != null){
							posExamples.add(newPosExample);
							continue;
						} else {
							failed = true;
							break;
						}
					}
//					learnedQuery = exampleFinder.getCurrentQuery();
					
					logger.info("Learned query:\n" + learnedQuery);
					equivalentQueries = isEquivalentQuery(targetResources, learnedQuery, endpoint, selectQueriesCache);
					logger.info("Original query and learned query are equivalent: " + equivalentQueries);
					if(equivalentQueries){
						break;
					}
					
					
					logger.info("Next suggested example is " + nextExample);
					//if the example is contained in the resultset of the query, we add it to the positive examples,
					//otherwise to the negatives
					if(targetResources.contains(nextExample)){
						posExamples.add(nextExample);
						posExampleTrees.add(treeCache.getQueryTree(nextExample, modelCache.getModel(nextExample)));
						logger.info("Suggested example is considered as positive example.");
						repeatedNegativeCount = 0;
					} else {
						negExamples.add(nextExample);
						negExampleTrees.add(treeCache.getQueryTree(nextExample, modelCache.getModel(nextExample)));
						logger.info("Suggested example is considered as negative example.");
						repeatedNegativeCount++;
					}
					
					if(repeatedNegativeCount > maxRepeatedNegativesCount){
						repeatedNegativeErrorCount++;
//						QueryTree<String> lgg = exampleFinder.getLGG();
						String newPosExample = getNewPosExampleNotCoveredByLGG(lgg, targetResources, posExamples, endpoint, selectQueriesCache);
						if(newPosExample != null){
							posExamples.add(newPosExample);
							continue;
						} else {
							failed = true;
							break;
						}
					}
					
					if(learnedQuery.equals(mostGeneralQuery)){
						mostGeneralQueryCount++;
					} else {
						mostGeneralQueryCount = 0;
					}
					if(lastQuery.equals(learnedQuery)){
						equalsLastQueryCount++;
					} else {
						equalsLastQueryCount = 0;
						lastQuery = learnedQuery;
					}
					if(mostGeneralQueryCount == 10 || equalsLastQueryCount == 20){
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
					
//					write2DB(ps, id, query, learnedQuery, triplePatternCount,
//							examplesCount, posExamplesCount, negExamplesCount,
//							totalTime, queryTime, queryTimeAvg, lggTime, lggTimeAvg, nbrTime, nbrTimeAvg);
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
	private static boolean isEquivalentQuery(SortedSet<String> originalResources, String query, SparqlEndpoint endpoint, ExtractionDBCache cache){
		if(query.equals("SELECT ?x0 WHERE {?x0 ?y ?z.}")){
			return false;
		}
		com.hp.hpl.jena.query.ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		
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
	
	private static SortedSet<String> getResources(String query, SPARQLEndpointEx endpoint, ExtractionDBCache cache){
		com.hp.hpl.jena.query.ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		
		SortedSet<String> resources = new TreeSet<String>();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("var0").isURIResource()){
				resources.add(qs.get("var0").asResource().getURI());
			}
		}
		return resources;
	}
	
	private static SortedSet<String> getResources2(String query, SPARQLEndpointEx endpoint, ExtractionDBCache cache){
		com.hp.hpl.jena.query.ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		
		SortedSet<String> resources = new TreeSet<String>();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("x0").isURIResource()){
				resources.add(qs.get("x0").asResource().getURI());
			}
		}
		return resources;
	}
	
	private static String getNewPosExampleNotCoveredByLGG(QueryTree<String> lgg, SortedSet<String> targetResources, List<String> selectedPosExamples, 
			SPARQLEndpointEx endpoint, ExtractionDBCache cache){
		SortedSet<String> lggResources = getResources(lgg.toSPARQLQueryString(), endpoint, cache);
		SortedSet<String> targetCopy = new TreeSet<String>(targetResources);
		targetCopy.removeAll(lggResources);
		targetCopy.removeAll(selectedPosExamples);
		return targetCopy.first();
	}
	
	private static String getExampleByPositiveGeneralisation(QueryTree<String> lgg, List<String> knownResources, SPARQLEndpointEx endpoint, ExtractionDBCache cache){
		Generalisation<String> posGen = new Generalisation<String>();
		QueryTree<String> genTree = posGen.generalise(lgg);
		learnedQuery = getDistinctQuery(genTree.toSPARQLQueryString());
		SortedSet<String> resources = getResources2(getLimitedQuery(learnedQuery, knownResources.size()+1), endpoint, cache);
		resources.removeAll(knownResources);
		if(resources.isEmpty()){
			return getExampleByPositiveGeneralisation(genTree, knownResources, endpoint, cache);
		} else {
			return resources.first();
		}
	}
	
	private static String getDistinctQuery(String query){
		return "SELECT DISTINCT " + query.substring(7);
	}
	
	private static String getLimitedQuery(String query, int limit){
		return query + " LIMIT " + limit;
	}

}
