package org.dllearner.autosparql.server.evaluation;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
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
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorCachedImpl;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.NBRGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.GreedyNBRStrategy;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.jamonapi.MonitorFactory;

public class SingleQueryEvaluationScript {
	
	private static final Logger logger = Logger.getLogger(SingleQueryEvaluationScript.class);

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
				layout, "log/single_evaluation.log", false);
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.INFO);
		Logger.getLogger(ModelGenerator.class).setLevel(Level.OFF);
		Logger.getLogger(SPARQLQueryGeneratorCachedImpl.class).setLevel(Level.OFF);
		Logger.getLogger(LGGGeneratorImpl.class).setLevel(Level.OFF);
		Logger.getLogger(NBRGeneratorImpl.class).setLevel(Level.OFF);
		Logger.getLogger(Generalisation.class).setLevel(Level.INFO);
		Logger.getLogger(GreedyNBRStrategy.class).setLevel(Level.INFO);
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
		
		String query = ""+
//		"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
//				"SELECT * WHERE { ?var0 owl:sameAs ?var1. ?var0 rdf:type <http://dbpedia.org/class/yago/State108654360> }";
//		"SELECT ?var0 WHERE { ?var0 skos:subject <http://dbpedia.org/resource/Category:French_films> }";
//		"PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpo:  <http://dbpedia.org/ontology/> SELECT DISTINCT ?var0 WHERE {  ?var0 a dbpo:Band . ?var0 dbpedia2:genre  <http://dbpedia.org/resource/Hard_rock> . ?var0 dbpedia2:genre  <http://dbpedia.org/resource/Heavy_metal_music> . ?var0  dbpedia2:genre <http://dbpedia.org/resource/Blues-rock> . }";
		"PREFIX dbpedia: <http://dbpedia.org/resource/> PREFIX dbo: <http://dbpedia.org/ontology/> SELECT DISTINCT ?var0 ?label ?homepage ?genre WHERE {?var0 a dbo:Band .?band rdfs:label ?label .OPTIONAL { ?var0 foaf:homepage ?homepage } .?var0 dbo:genre ?genre .?genre dbo:instrument dbpedia:Electric_guitar .?genre dbo:stylisticOrigin dbpedia:Jazz .}";

		com.hp.hpl.jena.query.ResultSet rs;
		SortedSet<String> resources;
		QuerySolution qs;
		ExampleFinder exampleFinder;
		List<String> posExamples;
		List<String> negExamples;
		
			logger.info("Evaluating query:\n" + query);
			
			
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
				logger.info("Query returned " + resources.size() + " results:\n" + resources);
				
				
				//start learning
				exampleFinder = new ExampleFinder(endpoint, selectQueriesCache, constructQueriesCache);
				posExamples = new ArrayList<String>();
				negExamples = new ArrayList<String>();
				
				//we choose the first resource in the set as positive example
//				String posExample = resources.first();
				String posExample = "http://dbpedia.org/resource/Foals";
				logger.info("Selected " + posExample + " as first positive example.");
				posExamples.add(posExample);

				//we ask for the next similar example
				String nextExample;
				String learnedQuery;
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
					
				} while(!equivalentQueries);
				
				logger.info("Number of examples needed: " 
						+ (posExamples.size() + negExamples.size()) 
						+ "(+" + posExamples.size() + "/-" + negExamples.size() + ")");
				logger.info(((QueryTreeImpl<String>)exampleFinder.getCurrentQueryTree()).getTriplePatternCount());
			} catch (Exception e) {
				logger.error("Error while learning query ", e);
			}
		logger.info(MonitorFactory.getTimeMonitor("LGG"));
		logger.info(MonitorFactory.getTimeMonitor("Query"));
		logger.info("Time to compute LGG(total): " + MonitorFactory.getTimeMonitor("LGG").getTotal());
		logger.info("Time to compute LGG(avg): " + MonitorFactory.getTimeMonitor("LGG").getAvg());
		logger.info("Time to compute LGG(min): " + MonitorFactory.getTimeMonitor("LGG").getMin());
		logger.info("Time to compute LGG(max): " + MonitorFactory.getTimeMonitor("LGG").getMax());

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
