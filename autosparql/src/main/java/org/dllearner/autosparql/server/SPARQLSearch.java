package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.exception.SPARQLQueryException;
import org.dllearner.autosparql.client.model.Example;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

public class SPARQLSearch {
	
	private static final Logger logger = Logger.getLogger(SPARQLSearch.class);
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	
	public SPARQLSearch(ExtractionDBCache cache){
		this.cache = cache;
	}
	
	public List<Example> searchFor(String searchTerm, SparqlEndpoint endpoint, int limit, int offset){
		logger.info("Searching for term: " + searchTerm);
		List<Example> searchResult = new ArrayList<Example>();
		
		String query = buildSearchQuery(searchTerm, limit, offset);
		logger.info("Sending query:\n" + query);
		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		String uri;
		String label;
		String imageURL = "";
		String comment = "";
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			uri = qs.getResource("object").getURI();
			label = qs.getLiteral("label").getLexicalForm();
			if(qs.getResource("imageURL") != null){
				imageURL = qs.getResource("imageURL").getURI();
			}
			if(qs.getLiteral("comment") != null){
				comment = qs.getLiteral("comment").getLexicalForm();
			}
			searchResult.add(new Example(uri, label, imageURL, comment));
		}
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
		sb.append("?label bif:contains \"").append(searchTerm).append("\".\n");
		sb.append("FILTER(LANGMATCHES(LANG(?label), \"en\"))\n");
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
