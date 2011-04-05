package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.QTL;
import org.dllearner.algorithm.qtl.exception.QTLException;
import org.dllearner.algorithm.qtl.filters.QueryTreeFilter;
import org.dllearner.algorithm.qtl.filters.QuestionBasedQueryTreeFilter;
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
	
	private SPARQLEndpointEx endpoint;
	private ExtractionDBCache selectCache;
	
	private List<String> posExamples = new ArrayList<String>();
	private List<String> negExamples = new ArrayList<String>();
	
	private static final Logger logger = Logger.getLogger(ExampleFinder.class);
	
	private String currentQuery;
	
	private QTL qtl;
	private SolrSearch resourceIndex;
	
	private String question;
	
	boolean dirty = true;
	
	public ExampleFinder(SPARQLEndpointEx endpoint, ExtractionDBCache selectCache, ExtractionDBCache constructCache){
		this.endpoint = endpoint;
		this.selectCache = selectCache;
		
		qtl = new QTL(endpoint, selectCache);
		qtl.setMaxExecutionTimeInSeconds(1000);
		resourceIndex = new SolrSearch("http://139.18.2.173:8080/apache-solr-1.4.1/dbpedia_resources");
	}
	
	
	public void setQuestion(String question){
		this.question = question;
		QuestionProcessor qp = new QuestionProcessor();
		List<String> relevantWords = qp.getRelevantWords(question);
		QuestionBasedStatementFilter stmtFilter = new QuestionBasedStatementFilter(new HashSet<String>(relevantWords));
		QueryTreeFilter treeFilter = new QuestionBasedQueryTreeFilter(new HashSet<String>(relevantWords));
		qtl.addStatementFilter(stmtFilter);
		qtl.addQueryTreeFilter(treeFilter);
	}
	
	
	public Example findSimilarExample(List<String> posExamples,
			List<String> negExamples) throws AutoSPARQLException{
		logger.info("Searching similiar example");
		logger.info("Positive examples: " + posExamples);
		logger.info("Negative examples: " + negExamples);
		
		String resource = "";
		try{
			 resource = qtl.getQuestion(posExamples, negExamples);
		} catch(QTLException e){
			throw new AutoSPARQLException(e);
		}
		
		return getExample(resource);
	}
	
	public void setExamples(List<String> posExamples,
			List<String> negExamples){
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		qtl.setExamples(posExamples, negExamples);
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
		if(examples.isEmpty()){
			return getExampleFromSPARQLEndpoint(uri);
		} else {
			return resourceIndex.getExamples("uri:\"" + uri + "\"").get(0);
		}
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
