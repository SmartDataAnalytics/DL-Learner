package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.util.strings.BifContains;
import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.util.SortableValueMap;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.simba.ner.QueryProcessor;

public class SPARQLSearch {
	
	private static final Logger logger = Logger.getLogger(SPARQLSearch.class);
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	
	private Map<String, Integer> relatedResources;
	private String lastQuery = "";
	
	private String servletContextPath;
	
	public SPARQLSearch(ExtractionDBCache cache, String servletContextPath){
		this.cache = cache;
		this.servletContextPath = servletContextPath;
	}
	
	public List<Example> searchFor(String query, SparqlEndpoint endpoint, int limit, int offset){
		List<Example> searchResult = new ArrayList<Example>();
		
		if(!query.equals(lastQuery)){
			QueryProcessor qp = new QueryProcessor(servletContextPath + "WEB-INF/classes/de/simba/ner/models/left3words-wsj-0-18.tagger",
			endpoint.getURL().toString(), servletContextPath + "WEB-INF/classes/de/simba/ner/dictionary");
			qp.setSynonymExpansion(false);
			qp.runQuery(query);
			relatedResources = qp.getRelatedResources();
			lastQuery = query;
		}
//		Ordering.natural().onResultOf(Functions.forMap(relatedResources));
		
		SortableValueMap<String, Integer> sortedMap = new SortableValueMap<String, Integer>(relatedResources);
		sortedMap.sortByValue();
		int i=0;
		for(Entry<String, Integer> entry : sortedMap.entrySet()){
			if(i > offset){
				searchResult.add(new Example(entry.getKey(), entry.getKey(), "", ""));
			}
			i++;
			if((i-offset) > limit){
				break;
			}
		}
		
//
//		searchTerm = new BifContains(searchTerm).makeWithAnd();
//		logger.info("Searching for term: " + searchTerm);
//		
//		String query = buildSearchQuery(searchTerm, limit, offset);
//		logger.info("Sending query:\n" + query);
//		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
//		String uri;
//		String label;
//		String imageURL = "";
//		String comment = "";
//		QuerySolution qs;
//		while(rs.hasNext()){
//			qs = rs.next();
//			uri = qs.getResource("object").getURI();
//			label = qs.getLiteral("label").getLexicalForm();
//			if(uri.startsWith("http://dbpedia.org/resource/Category:")){
//				label = "Category:" + label;
//			}
//			if(qs.getResource("imageURL") != null){
//				imageURL = qs.getResource("imageURL").getURI();
//			}
//			if(qs.getLiteral("comment") != null){
//				comment = qs.getLiteral("comment").getLexicalForm();
//			}
//			searchResult.add(new Example(uri, label, imageURL, comment));
//		}
		return searchResult;
	}
	
	public int count(String searchTerm, SparqlEndpoint endpoint){
		String query = buildCountQuery(searchTerm);
		int cnt = 0;
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			cnt = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cnt;
	}
	
	public List<Example> searchFor(String searchTerm, int limit, int offset) throws SPARQLQueryException{
		List<Example> searchResult = new ArrayList<Example>();
		
		String query = buildSearchQuery(searchTerm, limit, offset);
		try {
			ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			
			
			String uri;
			String label;
			String imageURL;
			String comment;
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				uri = qs.getResource("object").getURI();
				label = qs.getLiteral("label").getLexicalForm();
				imageURL = qs.getResource("image").getURI();
				comment = qs.getLiteral("comment").getLexicalForm();
				searchResult.add(new Example(uri, label, imageURL, comment));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return searchResult;
	}
	
	private String buildSearchQuery(String searchTerm, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT distinct(?object) ?label ?imageURL ?comment WHERE {\n");
		sb.append("?object a ?class.\n");
		sb.append("?object <").append(RDFS.label).append("> ?label.\n");
		sb.append("FILTER(LANGMATCHES(LANG(?label), \"en\"))\n");
		sb.append("FILTER(REGEX(?label, '" + searchTerm + "'))\n");
//		sb.append("FILTER(bif:contains(?label, '" + searchTerm + "'))\n");
		sb.append("OPTIONAL{?object <http://dbpedia.org/ontology/thumbnail> ?imageURL.}\n");
		sb.append("OPTIONAL{?object <").append(RDFS.comment).append("> ?comment.\n");
		sb.append("FILTER(LANGMATCHES(LANG(?comment), \"en\"))}");
		sb.append("}\n");
		sb.append("LIMIT ").append(limit);
		sb.append(" OFFSET ").append(offset);
		return sb.toString();
	}
	
	private String buildCountQuery(String searchTerm){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(distinct ?object) WHERE {\n");
		sb.append("?object a ?class.\n");
		sb.append("?object <").append(RDFS.label).append("> ?label.\n");
		sb.append("?label bif:contains \"").append(searchTerm).append("\".\n");
		sb.append("}\n");
		return sb.toString();
	}

}
