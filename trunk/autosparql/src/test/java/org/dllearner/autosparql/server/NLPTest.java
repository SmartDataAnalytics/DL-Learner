package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.exception.TimeOutException;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.sparqlquerygenerator.util.ExactMatchFilter;

import de.simba.ner.QueryProcessor;

public class NLPTest {

	/**
	 * @param args
	 * @throws SPARQLQueryException 
	 * @throws TimeOutException 
	 */
	public static void main(String[] args) throws TimeOutException, SPARQLQueryException {
		String posTaggerModelPath = "src/main/resources/de/simba/ner/models/left3words-wsj-0-18.tagger";
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
		String wordnetDictionaryPath = "src/main/resources/de/simba/ner/dictionary";
		QueryProcessor queryProcessor = new QueryProcessor(posTaggerModelPath, endpoint.getURL().toString(), wordnetDictionaryPath);
		queryProcessor.setSynonymExpansion(false);
		ExtractionDBCache selectCache = new ExtractionDBCache("select-cache");
		ExtractionDBCache constructCache = new ExtractionDBCache("construct-cache");
		//predicate filters used when sending sparql query for model creation
		List<String> predicateFilters = new ArrayList<String>();
		predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
		predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		ExampleFinder exFinder = new ExampleFinder(new SPARQLEndpointEx(endpoint, null, null, predicateFilters), selectCache, constructCache);
		
		//the query in natural language form 
		String nlpQuery = "Cities in Saxony";
		System.out.println("Query to learn: " + nlpQuery);
		
		//compute resources related to query
		System.out.println("Computing related resources...");
		long startTime = System.currentTimeMillis();
		queryProcessor.runQuery(nlpQuery);
		Set<String> relatedResources = queryProcessor.getRelatedResources().keySet();
		System.out.println("Found " + relatedResources.size() + " related resources in " + (System.currentTimeMillis()-startTime) + "ms" );
		
		//set exact match filter used in query tree creation 
		exFinder.setObjectFilter(new ExactMatchFilter(relatedResources));
		
		//create some positive examples
		List<String> posExamples = new ArrayList<String>();
		posExamples.add("http://dbpedia.org/resource/Leipzig");
		posExamples.add("http://dbpedia.org/resource/Dresden");
		
		//create some negative examples
		List<String> negExamples = new ArrayList<String>();
		
		//compute new similiar example
		System.out.println("Computing similiar example...");
		startTime = System.currentTimeMillis();
		Example example = exFinder.findSimilarExample(posExamples, negExamples);
		System.out.println("Computed similiar example \"" + example.getURI() + "\" in " + (System.currentTimeMillis()-startTime) + "ms");
		
		//print learned query up to here
		System.out.println("Learned query: \n" + exFinder.getCurrentQuery());
		
	}

}
