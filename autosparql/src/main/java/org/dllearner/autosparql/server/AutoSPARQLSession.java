package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.autosparql.client.exception.AutoSPARQLException;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.cache.SPARQLQueryCache;
import org.dllearner.autosparql.server.search.Search;
import org.dllearner.autosparql.server.search.SolrSearch;
import org.dllearner.autosparql.server.store.Store;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlQuery;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDFS;

public class AutoSPARQLSession {
	
	private static final Logger logger = Logger.getLogger(AutoSPARQLSession.class);
	
	private SPARQLEndpointEx endpoint;
	
	private ExtractionDBCache constructCache;
	private ExtractionDBCache selectCache;
	private ExampleFinder exampleFinder;
	
	private Search search;
	private Store store;
	private SPARQLQueryCache cache;
	
	private String servletContextPath;
	
	private List<String> topKResources;
	
	private String question;
	
	public AutoSPARQLSession(){
	}
	
	public AutoSPARQLSession(SPARQLEndpointEx endpoint, String cacheDir, String servletContextPath, String solrURL){
		this.endpoint = endpoint;
		this.servletContextPath = servletContextPath;
		
		constructCache = new ExtractionDBCache(cacheDir + "/" + endpoint.getPrefix() + "/construct-cache");
		selectCache = new ExtractionDBCache(cacheDir + "/" + endpoint.getPrefix() + "/select-cache");
		search = new SolrSearch(solrURL);
		exampleFinder = new ExampleFinder(endpoint, selectCache, constructCache);
	}
	
	public AutoSPARQLSession(String cacheDir, String servletContextPath, String solrURL){
		this.servletContextPath = servletContextPath;
		
		constructCache = new ExtractionDBCache(cacheDir + "/" + endpoint.getPrefix() + "/construct-cache");
		selectCache = new ExtractionDBCache(cacheDir + "/" + endpoint.getPrefix() + "/select-cache");
		search = new SolrSearch(solrURL);
		exampleFinder = new ExampleFinder(endpoint, selectCache, constructCache);
	}
	
	public void setEndpoint(SPARQLEndpointEx endpoint){
		this.endpoint = endpoint;
	}
	
	public void setQuestion(String question){
		this.question = question;
		exampleFinder.setQuestion(question);
	}
	
	public void setStore(Store store){
		this.store = store;
	}
	
	public void setSearch(Search search){
		this.search = search;
	}
	
	public void setCache(SPARQLQueryCache cache){
		this.cache = cache;
	}
	
	public PagingLoadResult<Example> getSearchResult(String searchTerm, PagingLoadConfig config) throws AutoSPARQLException{
		try {
			int limit = config.getLimit();
			int offset = config.getOffset();
			
			List<Example> searchResult = search.getExamples("label:" + getQuotedString(searchTerm), offset);
			int totalLength = search.getTotalHits(searchTerm);
			
			PagingLoadResult<Example> result = new BasePagingLoadResult<Example>(searchResult);
			result.setOffset(offset);
			result.setTotalLength(totalLength);
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AutoSPARQLException(e);
		}
	}
	
	public PagingLoadResult<Example> getQueryResult(String query,
			PagingLoadConfig config) throws AutoSPARQLException {
		try {
			int limit = config.getLimit();
			int offset = config.getOffset();
			
			List<Example> searchResult = search.getExamples(query, offset);
			if(offset == 0){
				topKResources = new ArrayList<String>();
				for(Example ex : searchResult){
					topKResources.add(ex.getURI());
				}
			}
			int totalLength = search.getTotalHits(query);
			
			PagingLoadResult<Example> result = new BasePagingLoadResult<Example>(searchResult);
			result.setOffset(offset);
			result.setTotalLength(totalLength);
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AutoSPARQLException(e);
		}
	}

	public Example getNextQueryResult(String query)
			throws AutoSPARQLException {
			return search.getExamples(query).get(0);
//		Example result = search.getNextQueryResult(query, endpoint);
//		exampleFinder.setObjectFilter(new ExactMatchFilter(new HashSet<String>(search.getAllQueryRelatedResources())));
//		return result;
	}
	
	public Example getSimilarExample(List<String> posExamples,
			List<String> negExamples) throws SPARQLQueryException{
		logger.info("Retrieving similiar example");
		logger.info("Pos examples: " + posExamples);
		logger.info("Neg examples: " + negExamples);
		if(negExamples.isEmpty()){
			negExamples = getIntermediateNegativeExamples(posExamples);
		}
		try {
			Example example = exampleFinder.findSimilarExample(posExamples, negExamples);
			return example;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new SPARQLQueryException(exampleFinder.getCurrentQueryHTML());
		}
	}
	
	public Map<String, String> getProperties(String query) throws AutoSPARQLException{
		Map<String, String> property2LabelMap = new TreeMap<String, String>();
		
		String queryTriples = query.substring(18, query.length()-1);
		
		String newQuery = "SELECT DISTINCT ?p ?label WHERE {" + queryTriples + "?x0 ?p ?o. " +
				"?p <" + RDFS.label + "> ?label. FILTER(LANGMATCHES(LANG(?label), 'en'))} " +
				"LIMIT 1000";
		System.out.println(newQuery);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(
				selectCache.executeSelectQuery(endpoint, newQuery));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			property2LabelMap.put(qs.getResource("p").getURI(), qs.getLiteral("label").getLexicalForm());
		}
		
		Iterator<String> it = property2LabelMap.keySet().iterator();
		while(it.hasNext()){
			String uri = it.next();
			if(!uri.startsWith("http://dbpedia.org/ontology")){
				it.remove();
			}
		}
		return property2LabelMap;
	}
	
	public PagingLoadResult<Example> getSPARQLQueryResult(String query,
			PagingLoadConfig config) throws AutoSPARQLException{
		List<Example> queryResult = new ArrayList<Example>();
		
		logger.info("SPARQL query:\n");
		logger.info(query);
		int limit = config.getLimit();
		int offset = config.getOffset();
		int totalLength = 10;
		
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, getCountQuery(query)));
			totalLength = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, modifyQuery(query + " OFFSET " + offset)));
			
			String uri;
			String label = "";
			String imageURL = "";
			String comment = "";
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				uri = qs.getResource("x0").getURI();
				label = qs.getLiteral("label").getLexicalForm();
				queryResult.add(new Example(uri, label, imageURL, comment));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PagingLoadResult<Example> result = new BasePagingLoadResult<Example>(queryResult);
		result.setOffset(offset);
		result.setTotalLength(totalLength);
		
		return result;
	}
	
	public PagingLoadResult<Example> getSPARQLQueryResultWithProperties(String query, List<String> properties,
			PagingLoadConfig config) throws AutoSPARQLException {
		List<Example> queryResult = new ArrayList<Example>();
		properties.remove("label");
		logger.info("SPARQL query:\n");
		logger.info(query);
		int limit = config.getLimit();
		int offset = config.getOffset();
		int totalLength = 10;
		
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, getCountQuery(query)));
			totalLength = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Map<String, String> var2URIMap = new HashMap<String, String>(properties.size()); 
		for(String property : properties){
			var2URIMap.put(property.substring(property.lastIndexOf("/")+1), property);
		}
		var2URIMap.put("label", RDFS.label.toString());
		
		String queryTriples = query.substring(18, query.length()-1);
		
		StringBuilder newQuery = new StringBuilder();
		newQuery.append("SELECT DISTINCT ?x0 ");
		for(String var : var2URIMap.keySet()){
			newQuery.append("?").append(var).append(" ");
		}
		newQuery.append("{");
		newQuery.append(queryTriples);
		for(Entry<String, String> entry : var2URIMap.entrySet()){
			newQuery.append("?x0 <").append(entry.getValue()).append("> ?").append(entry.getKey()).append(".\n");
		}
		newQuery.append("FILTER(LANGMATCHES(LANG(?label),'en'))");
		newQuery.append("}");
		System.out.println("Query with properties:\n" + newQuery.toString());
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, modifyQuery(newQuery + " OFFSET " + offset)));
			
			String uri;
			String label = "";
			String imageURL = "";
			String comment = "";
			QuerySolution qs;
			Example example;
			RDFNode object;
			while(rs.hasNext()){
				qs = rs.next();
				uri = qs.getResource("x0").getURI();
				label = qs.getLiteral("label").getLexicalForm();
				example = new Example(uri, label, imageURL, comment);
				example.setAllowNestedValues(false);
				for(Entry<String, String> entry : var2URIMap.entrySet()){
					object = qs.get(entry.getKey());
					if(object.isURIResource()){
						example.set(entry.getValue(), object.asResource().getURI());
					} else if(object.isLiteral()){
						example.set(entry.getValue(), object.asLiteral().getLexicalForm());
					}
				}
				queryResult.add(example);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PagingLoadResult<Example> result = new BasePagingLoadResult<Example>(queryResult);
		result.setOffset(offset);
		result.setTotalLength(totalLength);
		
		return result;
	}

	public PagingLoadResult<Example> getCurrentQueryResult(
			PagingLoadConfig config) throws SPARQLQueryException {
		logger.info("Retrieving results for current query.");
		List<Example> queryResult = new ArrayList<Example>();
		
		String currentQuery = exampleFinder.getCurrentQuery();
		logger.info("Current query:\n");
		logger.info(currentQuery);
		int limit = config.getLimit();
		int offset = config.getOffset();
		int totalLength = 10;
		
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, getCountQuery(currentQuery)));
			totalLength = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, modifyQuery(currentQuery + " OFFSET " + offset)));
			
			String uri;
			String label = "";
			String imageURL = "";
			String comment = "";
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				uri = qs.getResource("x0").getURI();
				label = qs.getLiteral("label").getLexicalForm();
				queryResult.add(new Example(uri, label, imageURL, comment));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PagingLoadResult<Example> result = new BasePagingLoadResult<Example>(queryResult);
		result.setOffset(offset);
		result.setTotalLength(totalLength);
		
		return result;
	}
	
	public String getCurrentQuery() throws AutoSPARQLException {
		try{
			return exampleFinder.getCurrentQueryHTML();
		} catch (Exception e){
			logger.error(e);
			throw new AutoSPARQLException(e);
		}
	}
	
	public void setExamples(List<String> posExamples,
			List<String> negExamples){
		exampleFinder.setExamples(posExamples, negExamples);
	}
	
	public void saveSPARQLQuery(Store store) throws AutoSPARQLException{
		store.saveSPARQLQuery(question, exampleFinder.getCurrentQuery(), endpoint.getLabel());
	}
	
	private List<String> getIntermediateNegativeExamples(List<String> posExamples){
		List<String> negExamples = new ArrayList<String>();
		for(String resource : topKResources){
			if(!posExamples.contains(resource)){
				negExamples.add(resource);
				break;
			}
		}
		return negExamples;
	}
	
	private String getQuotedString(String str){
		return "\"" + str + "\"";
	}
	
	private String modifyQuery(String query){
		String newQuery = query.replace("SELECT ?x0 WHERE {", 
				"SELECT DISTINCT(?x0) ?label WHERE{\n?x0 <" + RDFS.label + "> ?label.FILTER(LANGMATCHES(LANG(?label), 'en'))");
		
		return newQuery;
	}
	
	private String getCountQuery(String query){
		String newQuery = query.replace("SELECT ?x0", 
				"SELECT COUNT(DISTINCT ?x0)");
		newQuery = newQuery.substring(0, newQuery.indexOf('}') + 1);
		
		return newQuery;
	}
}
