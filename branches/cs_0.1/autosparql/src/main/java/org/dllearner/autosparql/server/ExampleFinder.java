package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.sparqlquerygenerator.SPARQLQueryGenerator;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorImpl;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

public class ExampleFinder {
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache selectCache;
	private org.dllearner.kb.sparql.ExtractionDBCache constructCache;
	
	private List<String> posExamples;
	private List<String> negExamples;
	
	private static final Logger logger = Logger.getLogger(ExampleFinder.class);
	
	private static final int RECURSION_DEPTH = 2;
	
	private String currentQuery;
	
	private QueryTree<String> currentQueryTree;
	
	public ExampleFinder(SparqlEndpoint endpoint, ExtractionDBCache selectCache, ExtractionDBCache constructCache){
		this.endpoint = endpoint;
		this.selectCache = selectCache;
		this.constructCache = constructCache;
	}
	
	public Example findSimilarExample(List<String> posExamples,
			List<String> negExamples) throws SPARQLQueryException{
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		ModelGenerator modelGen = new ModelGenerator(endpoint, constructCache);
		QueryTreeGenerator treeGen = new QueryTreeGenerator(constructCache, endpoint, 5000);
		
		List<QueryTree<String>> posExampleTrees = new ArrayList<QueryTree<String>>();
		List<QueryTree<String>> negExampleTrees = new ArrayList<QueryTree<String>>();
		
		Model model;
		for(String resource : posExamples){
			model = modelGen.createModel(resource, ModelGenerator.Strategy.CHUNKS, RECURSION_DEPTH);
			posExampleTrees.add(treeGen.getQueryTree(resource, model));
		}
		for(String resource : negExamples){
			model = modelGen.createModel(resource, ModelGenerator.Strategy.CHUNKS, RECURSION_DEPTH);
			negExampleTrees.add(treeGen.getQueryTree(resource, model));
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
		logger.info("USING GENERALISATION");
		logger.info("QUERY BEFORE GENERALISATION: \n\n" + tree.toSPARQLQueryString(true));
		Generalisation<String> generalisation = new Generalisation<String>();
		
		QueryTree<String> genTree = generalisation.generalise(tree);
		
		currentQuery = genTree.toSPARQLQueryString(true);
		currentQueryTree = genTree;
		logger.info("QUERY AFTER GENERALISATION: \n\n" + currentQuery);
		
		currentQuery = currentQuery + " ORDER BY ?x0 LIMIT 10";
		String result = "";
		try {
			result = selectCache.executeSelectQuery(endpoint, currentQuery);
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
			if(!posExamples.contains(uri) && !negExamples.contains(uri)){
				logger.info("Found new example: " + uri);
				return getExample(uri);
			}
		}
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
		SPARQLQueryGenerator gen = new SPARQLQueryGeneratorImpl(endpoint.getURL().toString());
		if(negExamplesTrees.isEmpty()){
			logger.info("No negative examples given. Avoiding big queries by GENERALISATION");
			List<QueryTree<String>> trees = gen.getSPARQLQueryTrees(posExamplesTrees, negExamplesTrees);
			return findExampleByGeneralisation(trees.get(0));
		}
		
		List<String> queries = gen.getSPARQLQueries(posExamplesTrees, negExamplesTrees);
		for(String query : queries){
			logger.info("Trying query");
			currentQuery = query + " LIMIT 10";
			logger.info(query);
			String result = "";
			try {
				result = selectCache.executeSelectQuery(endpoint, currentQuery);
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
		return findExampleByGeneralisation(gen.getLastLGG());
	}
	
	private Example getExample(String uri){
		logger.info("Retrieving data for resouce " + uri);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ?label ?imageURL ?comment WHERE{\n");
		sb.append("<").append(uri).append("> <").append(RDFS.label.getURI()).append("> ").append("?label.\n");
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
		QuerySolution qs = rs.next();
		
		String label = qs.getLiteral("label").getLexicalForm();
		
		String imageURL = "";
		if(qs.getResource("imageURL") != null){
			imageURL = qs.getResource("imageURL").getURI();
		}
		
		String comment = "";
		if(qs.getLiteral("comment") != null){
			comment = qs.getLiteral("comment").getLexicalForm();
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
	
	public void setEndpoint(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
	}
	
	public String getCurrentQuery(){
		return currentQuery;
	}
	
	public String getCurrentQueryHTML(){
		return encodeHTML(currentQuery);
	}
}
