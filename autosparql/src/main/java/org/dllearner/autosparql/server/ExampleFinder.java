package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.QTL;
import org.dllearner.algorithm.qtl.exception.QTLException;
import org.dllearner.algorithm.qtl.filters.QueryTreeFilter;
import org.dllearner.algorithm.qtl.filters.QuestionBasedQueryTreeFilter;
import org.dllearner.algorithm.qtl.filters.QuestionBasedQueryTreeFilterAggressive;
import org.dllearner.algorithm.qtl.filters.QuestionBasedStatementFilter;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.autosparql.client.exception.AutoSPARQLException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.search.QuestionProcessor;
import org.dllearner.autosparql.server.search.SolrSearch;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.openrdf.vocabulary.RDFS;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;

public class ExampleFinder {
	
	private static final Logger logger = Logger.getLogger(ExampleFinder.class);
	
	private SPARQLEndpointEx endpoint;
	private ExtractionDBCache selectCache;
	private QTL qtl;
	private SolrSearch resourceIndex;
	
	private List<String> posExamples = new ArrayList<String>();
	private List<String> negExamples = new ArrayList<String>();
	
	private String question;
	private String currentQuery;
	private Example lastSuggestedExample;
	
	private Map<String, Example> examplesCache;
	
	boolean dirty = true;
	
	public ExampleFinder(SPARQLEndpointEx endpoint, ExtractionDBCache selectCache, ExtractionDBCache constructCache){
		this.endpoint = endpoint;
		this.selectCache = selectCache;
		
		qtl = new QTL(endpoint, selectCache);
		qtl.setMaxExecutionTimeInSeconds(1000);
		resourceIndex = new SolrSearch("http://139.18.2.173:8080/apache-solr-1.4.1/dbpedia_resources");
		
		examplesCache = new HashMap<String, Example>();
	}
	
	
	public void setQuestion(String question){
		this.question = question;
		QuestionProcessor qp = new QuestionProcessor();
		List<String> relevantWords = qp.getRelevantWords(question);
		QuestionBasedStatementFilter stmtFilter = new QuestionBasedStatementFilter(new HashSet<String>(relevantWords));
		stmtFilter.setThreshold(0.6);
//		QueryTreeFilter treeFilter = new QuestionBasedQueryTreeFilter(new HashSet<String>(relevantWords));
		QuestionBasedQueryTreeFilterAggressive treeFilter = new QuestionBasedQueryTreeFilterAggressive(new HashSet<String>(relevantWords));
		qtl.addStatementFilter(stmtFilter);
		qtl.addQueryTreeFilter(treeFilter);
	}
	
	
	public Example findSimilarExample(List<String> posExamples,
			List<String> negExamples) throws AutoSPARQLException{
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		logger.info("Searching similiar example");
		logger.info("Positive examples: " + posExamples);
		logger.info("Negative examples: " + negExamples);
		
		String resource = "";
		try{
			 resource = qtl.getQuestion(posExamples, negExamples);
		} catch(QTLException e){
			throw new AutoSPARQLException(e);
		}
		lastSuggestedExample = getExample(resource);
		
		return lastSuggestedExample;
	}
	
	public void setExamples(List<String> posExamples,
			List<String> negExamples){
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		qtl.setExamples(posExamples, negExamples);
	}
	
	public List<String> getPositiveExamples(){
		return this.posExamples;
	}
	
	public List<String> getNegativeExamples(){
		return this.negExamples;
	}
	
//	public List<Example> getPositiveExamples(){
//		System.out.println("CACHE:" + examplesCache);
//		List<Example> examples = new ArrayList<Example>();
//		for(String uri : posExamples){
//			examples.add(examplesCache.get(uri));
//		}
//		return examples;
//	}
//	
//	public List<Example> getNegativeExamples(){
//		List<Example> examples = new ArrayList<Example>();
//		for(String uri : negExamples){
//			examples.add(examplesCache.get(uri));
//		}
//		return examples;
//	}
	
	public Example getLastSuggestedExample(){
		return lastSuggestedExample;
	}
	
	
	private SortedSet<String> getResources(String query){
		SortedSet<String> resources = new TreeSet<String>();
		String result = selectCache.executeSelectQuery(endpoint, getLimitedQuery(query, (posExamples.size()+negExamples.size()+1), true));
		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(result);
		String uri;
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			uri = qs.getResource("x0").getURI();
			resources.add(uri);
		}
		return resources;
	}
	
	private String getAngleBracketsString(String str){
		return "<" + str + ">";
	}
	
	private Example getExample(String uri){
		List<Example> examples = resourceIndex.getExamples("uri:\"" + uri + "\"");
		Example example;
		if(examples.isEmpty()){
			example = getExampleFromSPARQLEndpoint(uri);
		} else {
			example = examples.get(0);
		}
		examplesCache.put(uri, example);
		
		return example;
	}
	
	private Example getExampleFromSPARQLEndpoint(String uri){
		String query = "SELECT ?label ?comment ?imageURL WHERE {" +
			getAngleBracketsString(uri) + getAngleBracketsString(RDFS.LABEL) + "?label.FILTER(LANGMATCHES(LANG(?label), 'en'))" +
			"OPTIONAL{" + getAngleBracketsString(uri) + getAngleBracketsString(RDFS.COMMENT) + "?comment.FILTER(LANGMATCHES(LANG(?comment), 'en'))}" +
			"OPTIONAL{" + getAngleBracketsString(uri) + getAngleBracketsString("http://dbpedia.org/ontology/thumbnail") + "?imageURL.}" +
					"}";
		System.out.println(query);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, query));
		QuerySolution qs = rs.next();
		String label = qs.getLiteral("label").getLexicalForm();
		String comment = qs.getLiteral("comment").getLexicalForm();
		String imageURL = qs.getResource("imageURL").getURI();
		
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
		return qtl.getSPARQLQuery();
	}
	
	public String getCurrentQueryHTML(){
		return encodeHTML(qtl.getSPARQLQuery());
	}
	
	public String getLimitedQuery(String query, int limit, boolean distinct){
		if(distinct){
			query = "SELECT DISTINCT " + query.substring(7);
		}
		return query + " LIMIT " + limit;
	}
}
