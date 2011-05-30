package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

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
	
	private Map<String, String> property2LabelMap;
	
	private SortedMap<String, Map<String, String>> propertiesCache;
	private int currentQueryResultSize = -1;
	
	private Map<String, Example> examplesCache;
	
	public AutoSPARQLSession(){
	}
	
	public AutoSPARQLSession(SPARQLEndpointEx endpoint, String cacheDir, String servletContextPath, String solrURL){
		this.endpoint = endpoint;
		this.servletContextPath = servletContextPath;
		
		constructCache = new ExtractionDBCache(cacheDir + "/" + endpoint.getPrefix() + "/construct-cache");
		selectCache = new ExtractionDBCache(cacheDir + "/" + endpoint.getPrefix() + "/select-cache");
		search = new SolrSearch(solrURL);
		exampleFinder = new ExampleFinder(endpoint, selectCache, constructCache);
		
		property2LabelMap = new TreeMap<String, String>();
		propertiesCache = new TreeMap<String, Map<String,String>>();
		examplesCache = new HashMap<String, Example>();
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
			for(Example example : searchResult){
				examplesCache.put(example.getURI(), example);
			}
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
			for(Example example : searchResult){
				examplesCache.put(example.getURI(), example);
			}
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
//			negExamples = getIntermediateNegativeExamples(posExamples);
		}
		try {
			Example example = exampleFinder.findSimilarExample(posExamples, negExamples);
			examplesCache.put(example.getURI(), example);
			return example;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new SPARQLQueryException(exampleFinder.getCurrentQueryHTML());
		}
	}
	
	public Map<String, String> getProperties(String query) throws AutoSPARQLException{
		property2LabelMap = new TreeMap<String, String>();
		
		String queryTriples = query.substring(18, query.length()-1);
		
		String newQuery = "SELECT DISTINCT ?p ?label WHERE {" + queryTriples + "?x0 ?p ?o. " +
				"?p <" + RDFS.label + "> ?label. FILTER(LANGMATCHES(LANG(?label), 'en'))} " +
				"LIMIT 1000";
		
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
		property2LabelMap.put(RDFS.label.getURI(),"label");
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
//		properties.remove("label");
		logger.info("SPARQL query:\n");
		logger.info(query);
		int limit = config.getLimit();
		int offset = config.getOffset();
		int totalLength = 10;
		
		if(currentQueryResultSize == -1){
			try {
				ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, getCountQuery(query)));
				currentQueryResultSize = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
			} catch (Exception e) {
				e.printStackTrace();
				currentQueryResultSize = 10;
			}
		}
		totalLength = currentQueryResultSize;
		
		List<String> propertiesToDo = new ArrayList<String>(properties);
		for(Map<String, String> prop2Value : propertiesCache.values()){
			propertiesToDo.removeAll(prop2Value.keySet());
		}
		
		if(propertiesToDo.size() > 0){
			String queryTriples = query.substring(18, query.length()-1);
			StringBuilder newQuery = new StringBuilder();
			Map<String, String> var2URIMap = new HashMap<String, String>(propertiesToDo.size());
			
			if(propertiesToDo.size() == 1 && propertiesToDo.get(0).equals(RDFS.label.getURI())){
				newQuery.append("SELECT DISTINCT ?x0 ?label {");
				newQuery.append(queryTriples);
				newQuery.append("?x0 <").append(RDFS.label).append("> ?label.\n");
				newQuery.append("FILTER(LANGMATCHES(LANG(?label),'en'))");
				newQuery.append("}");
			} else {
				for(String property : propertiesToDo){
					var2URIMap.put(property2LabelMap.get(property).replace(" ", "_"), property);
				}
				
				newQuery.append("SELECT DISTINCT ?x0 ?label ");
				for(String var : var2URIMap.keySet()){
					newQuery.append("?").append(var).append(" ");
					newQuery.append("?").append(var).append("_label ");
				}
				newQuery.append("{");
				newQuery.append(queryTriples);
				newQuery.append("?x0 <").append(RDFS.label).append("> ?label.\n");
				for(Entry<String, String> entry : var2URIMap.entrySet()){
					newQuery.append("OPTIONAL{?x0 <").append(entry.getValue()).append("> ?").append(entry.getKey()).append(".}\n");
				}
				for(Entry<String, String> entry : var2URIMap.entrySet()){
					newQuery.append("OPTIONAL{?").append(entry.getKey()).append(" <").append(RDFS.label).append("> ?").append(entry.getKey()).append("_label.\n");
					newQuery.append("FILTER(LANGMATCHES(LANG(?" + entry.getKey() + "_label),'en'))}\n");
				}
				newQuery.append("FILTER(LANGMATCHES(LANG(?label),'en'))");
				newQuery.append("}");
			}
			
			System.out.println("Query with properties:\n" + newQuery.toString());
			try {
				ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, modifyQuery(newQuery + " LIMIT 100")));
				
				String uri;
				String label = "";
				QuerySolution qs;
				RDFNode object;
				while(rs.hasNext()){
					qs = rs.next();
					uri = qs.getResource("x0").getURI();
					label = qs.getLiteral("label").getLexicalForm();
					Map<String, String> properties2Value = propertiesCache.get(uri);
					if(properties2Value == null){
						properties2Value = new HashMap<String, String>();
						properties2Value.put(RDFS.label.getURI(), label);
						propertiesCache.put(uri, properties2Value);
					} 
					
					String value;
					String property;
					for(Entry<String, String> entry : var2URIMap.entrySet()){
						value = "";
						property = entry.getValue();
						
						object = qs.get(entry.getKey() + "_label");
						if(object == null){
							object = qs.get(entry.getKey());
						}
						if(object != null){
							if(object.isURIResource()){
								value = object.asResource().getURI();
							} else if(object.isLiteral()){
								value = object.asLiteral().getLexicalForm();
							}
						}
						String oldValue = properties2Value.get(property);
						if(oldValue != null){
							value = oldValue + ", " + value;
						}
						properties2Value.put(property, value);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Example example;
		int cnt = 0;
		for(Entry<String, Map<String, String>> uri2PropertyValues : propertiesCache.entrySet()){
			if(cnt++ == limit+offset){
				break;
			}
			if(cnt > offset){
				example = new Example();
				example.setAllowNestedValues(false);
				example.set("uri", uri2PropertyValues.getKey());
				for(Entry<String, String> property2Value : uri2PropertyValues.getValue().entrySet()){
					example.set(property2Value.getKey(), property2Value.getValue());
				}
				queryResult.add(example);
			}
			
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
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, modifyQuery(currentQuery + " LIMIT " + limit + " OFFSET " + offset)));
			
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
//			return exampleFinder.getCurrentQueryHTML();
			return exampleFinder.getCurrentQuery();
		} catch (Exception e){
			logger.error(e);
			throw new AutoSPARQLException(e);
		}
	}
	
	public void setExamples(List<String> posExamples,
			List<String> negExamples){
		try {
			exampleFinder.setExamples(posExamples, negExamples);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveSPARQLQuery(Store store) throws AutoSPARQLException {
		List<Example> posExamples = new ArrayList<Example>();
		for(String uri : exampleFinder.getPositiveExamples()){
			posExamples.add(examplesCache.get(uri));
		}
		List<Example> negExamples = new ArrayList<Example>();
		for(String uri : exampleFinder.getNegativeExamples()){
			negExamples.add(examplesCache.get(uri));
		}
		store.saveSPARQLQuery(question, exampleFinder.getCurrentQuery(), endpoint.getLabel(), posExamples, negExamples, exampleFinder.getLastSuggestedExample());
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
