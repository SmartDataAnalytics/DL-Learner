package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.SPARQLQueryGeneratorCached;
import org.dllearner.sparqlquerygenerator.cache.ModelCache;
import org.dllearner.sparqlquerygenerator.cache.QueryTreeCache;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorCachedImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.BruteForceNBRStrategy;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.GreedyNBRStrategy;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ExampleFinder {
	
	private SPARQLEndpointEx endpoint;
	private ExtractionDBCache selectCache;
	private ExtractionDBCache constructCache;
	private ModelGenerator modelGen;
	private ModelCache modelCache;
	private QueryTreeCache queryTreeCache;
	
	private List<String> posExamples;
	private List<String> negExamples;
	
	private static final Logger logger = Logger.getLogger(ExampleFinder.class);
	
	private String currentQuery;
	
	private QueryTree<String> currentQueryTree;
	
	private Set<String> testedQueries;
	
	private SPARQLQueryGeneratorCached queryGen;
	
	public ExampleFinder(SPARQLEndpointEx endpoint, ExtractionDBCache selectCache, ExtractionDBCache constructCache){
		this.endpoint = endpoint;
		this.selectCache = selectCache;
		this.constructCache = constructCache;
		
		modelGen = new ModelGenerator(endpoint, new HashSet<String>(endpoint.getPredicateFilters()), constructCache);
		modelCache = new ModelCache(modelGen);
		queryTreeCache = new QueryTreeCache();
		testedQueries = new HashSet<String>();
		
		queryGen = new SPARQLQueryGeneratorCachedImpl(new GreedyNBRStrategy());
//		queryGen = new SPARQLQueryGeneratorCachedImpl(new BruteForceNBRStrategy());
	}
	
	public Example findSimilarExample(List<String> posExamples,
			List<String> negExamples) throws SPARQLQueryException{
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		List<QueryTree<String>> posExampleTrees = new ArrayList<QueryTree<String>>();
		List<QueryTree<String>> negExampleTrees = new ArrayList<QueryTree<String>>();
		
		Model model;
		QueryTree<String> queryTree;
		for(String resource : posExamples){
			logger.info("Fetching model for resource: " + resource);
			model = modelCache.getModel(resource);
			queryTree = queryTreeCache.getQueryTree(resource, model);
			posExampleTrees.add(queryTree);
		}
		for(String resource : negExamples){
			logger.info("Fetching model for resource: " + resource);
			model = modelCache.getModel(resource);
			queryTree = queryTreeCache.getQueryTree(resource, model);
			negExampleTrees.add(queryTree);
		}
		
		if(posExamples.size() == 1 && negExamples.isEmpty()){
			return findExampleByGeneralisation(posExampleTrees.get(0));
		} else {
			return findExampleByLGG(posExampleTrees, negExampleTrees);
		}
		
	}
	
//	private Example findExampleByGeneralisation(List<String> posExamples,
//			List<String> negExamples) throws SPARQLQueryException{
//		logger.info("USING GENERALISATION");
//		
//		QueryTreeGenerator treeGen = new QueryTreeGenerator(constructCache, endpoint, 5000);
//		QueryTree<String> tree = treeGen.getQueryTree(posExamples.get(0));
//		logger.info("QUERY BEFORE GENERALISATION: \n\n" + tree.toSPARQLQueryString());
//		Generalisation<String> generalisation = new Generalisation<String>();
//		QueryTree<String> genTree = generalisation.generalise(tree);
//		String query = genTree.toSPARQLQueryString();
//		logger.info("QUERY AFTER GENERALISATION: \n\n" + query);
//		
//		query = query + " LIMIT 10";
//		String result = "";
//		try {
//			result = selectCache.executeSelectQuery(endpoint, query);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new SPARQLQueryException(e, encodeHTML(query));
//		}
//		
//		ResultSetRewindable rs = ExtractionDBCache.convertJSONtoResultSet(result);
//		String uri;
//		QuerySolution qs;
//		while(rs.hasNext()){
//			qs = rs.next();
//			uri = qs.getResource("x0").getURI();
//			if(!posExamples.contains(uri) && !negExamples.contains(uri)){
//				return getExample(uri);
//			}
//		}
//		return null;
//	}
	
	private Example findExampleByGeneralisation(QueryTree<String> tree) throws SPARQLQueryException{
		logger.info("Using generalisation");
		logger.info("Query before generalisation: \n\n" + tree.toSPARQLQueryString(true));
		Generalisation<String> generalisation = new Generalisation<String>();
		
		QueryTree<String> genTree = generalisation.generalise(tree);
		
		currentQuery = genTree.toSPARQLQueryString(true);
		currentQueryTree = genTree;
		logger.info("Query after generalisation: \n\n" + currentQuery);
		
		String result = "";
		try {
			logger.info(tree.getChildren().isEmpty());
			if(testedQueries.contains(currentQuery) && !currentQueryTree.getChildren().isEmpty()){
				return findExampleByGeneralisation(currentQueryTree);
			} else {
				result = selectCache.executeSelectQuery(endpoint, getLimitedQuery(currentQuery, (posExamples.size()+negExamples.size()+1)));
				testedQueries.add(currentQuery);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new SPARQLQueryException(e, encodeHTML(currentQuery));
		}
		
		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(result);
		String uri;
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			uri = qs.getResource("x0").getURI();
			logger.info(uri);
			if(!posExamples.contains(uri) && !negExamples.contains(uri)){
				logger.info("Found new example: " + uri);
				return getExample(uri);
			}
		}
		logger.info("Found no new example. Trying again generalisation...");
		return findExampleByGeneralisation(genTree);
	}

//	private Example findExampleByLGG(List<String> posExamples,
//			List<String> negExamples) throws SPARQLQueryException{
//		logger.info("USING LGG");
//		SPARQLQueryGenerator gen = new SPARQLQueryGeneratorImpl(endpoint.getURL().toString());
//		if(negExamples.isEmpty()){
//			logger.info("No negative examples given. Avoiding big queries by GENERALISATION");
//			List<QueryTree<String>> trees = gen.getSPARQLQueryTrees(new HashSet<String>(posExamples), new HashSet<String>(negExamples));
//			return findExampleByGeneralisation(trees.get(0));
//		}
//		
//		List<String> queries = gen.getSPARQLQueries(new HashSet<String>(posExamples), new HashSet<String>(negExamples));
//		for(String query : queries){
//			logger.info("Trying query");
//			currentQuery = query + " LIMIT 10";
//			logger.info(query);
//			String result = "";
//			try {
//				result = selectCache.executeSelectQuery(endpoint, currentQuery);
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new SPARQLQueryException(e, encodeHTML(query));
//			}
//			
//			ResultSetRewindable rs = ExtractionDBCache.convertJSONtoResultSet(result);
//			String uri;
//			QuerySolution qs;
//			while(rs.hasNext()){
//				qs = rs.next();
//				uri = qs.getResource("x0").getURI();
//				if(!posExamples.contains(uri) && !negExamples.contains(uri)){
//					return getExample(uri);
//				}
//			}
//			logger.info("Query result contains no new examples. Trying another query...");
//		}
//		logger.info("None of the queries contained a new example.");
//		logger.info("Changing to Generalisation...");
//		return findExampleByGeneralisation(gen.getLastLGG());
//	}
	
	private Example findExampleByLGG(List<QueryTree<String>> posExamplesTrees,
			List<QueryTree<String>> negExamplesTrees) throws SPARQLQueryException{
		logger.info("USING LGG");
		if(negExamplesTrees.isEmpty()){
			logger.info("No negative examples given. Avoiding big queries by GENERALISATION");
			queryGen.getSPARQLQueries(posExamplesTrees);
			QueryTree<String> lgg = queryGen.getLastLGG();
			return findExampleByGeneralisation(lgg);
		}
		
		List<String> queries = queryGen.getSPARQLQueries(posExamplesTrees, negExamplesTrees);
		for(String query : queries){
			if(testedQueries.contains(query)){
				logger.info("Skipping query because it was already tested before:\n" + query);
				continue;
			}
			logger.info("Trying query");
			currentQuery = query;
			logger.info(query);
			String result = "";
			try {
				result = selectCache.executeSelectQuery(endpoint, getLimitedQuery(currentQuery, 10));
				testedQueries.add(currentQuery);
			} catch (Exception e) {
				e.printStackTrace();
				throw new SPARQLQueryException(e, encodeHTML(query));
			}
			
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(result);
			String uri;
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				uri = qs.getResource("x0").getURI();
				if(!posExamples.contains(uri) && !negExamples.contains(uri)){
					return getExample(uri);
				}
			}
			logger.info("Query result contains no new examples. Trying another query...");
		}
		logger.info("None of the queries contained a new example.");
		logger.info("Changing to Generalisation...");
		return findExampleByGeneralisation(queryGen.getLastLGG());
	}
	
	private Example getExample(String uri){
		logger.info("Retrieving data for resource " + uri);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ?label ?imageURL ?comment WHERE{\n");
		sb.append("OPTIONAL{\n");
		sb.append("<").append(uri).append("> <").append(RDFS.label.getURI()).append("> ").append("?label.\n");
		sb.append("}\n");
		sb.append("OPTIONAL{\n");
		sb.append("<").append(uri).append("> <").append(FOAF.depiction.getURI()).append("> ").append("?imageURL.\n");
		sb.append("}\n");
		sb.append("OPTIONAL{\n");
		sb.append("<").append(uri).append("> <").append(RDFS.comment.getURI()).append("> ").append("?comment.\n");
		sb.append("FILTER(LANGMATCHES(LANG(?comment),'en'))\n");
		sb.append("}\n");
		sb.append("FILTER(LANGMATCHES(LANG(?label),'en'))\n");
		sb.append("}");
		
		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, sb.toString()));
		String label = uri;
		String imageURL = "";
		String comment = "";
		if(rs.hasNext()){
			QuerySolution qs = rs.next();		
			
			if(qs.getLiteral("label") != null){
				label = qs.getLiteral("label").getLexicalForm();
			}
			
			if(qs.getResource("imageURL") != null){
				imageURL = qs.getResource("imageURL").getURI();
			}
			
			if(qs.getLiteral("comment") != null){
				comment = qs.getLiteral("comment").getLexicalForm();
			}
		}
		
		return new Example(uri, label, imageURL, comment);
	}
	
	public String encodeHTML(String s) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 127 || c == '"' || c == '<' || c == '>') {
				out.append("&#" + (int) c + ";");
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}
	
	public void setEndpoint(SPARQLEndpointEx endpoint){
		this.endpoint = endpoint;
	}
	
	public String getCurrentQuery(){
		return currentQuery;
	}
	
	public String getCurrentQueryHTML(){
		return encodeHTML(currentQuery);
	}
	
	public String getLimitedQuery(String query, int limit){
		return query + " LIMIT " + limit;
	}
}
