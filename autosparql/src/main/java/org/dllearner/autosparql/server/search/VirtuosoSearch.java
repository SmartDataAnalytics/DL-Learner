package org.dllearner.autosparql.server.search;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.RDFS;

public class VirtuosoSearch {
	
	private static Logger logger = Logger.getLogger(VirtuosoSearch.class);
	
	private SparqlEndpoint endpoint;
	private int hitsPerPage = 10;
	
	public VirtuosoSearch(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
	}
	
	public VirtuosoSearch(URL endpointURL){
		this(new SparqlEndpoint(endpointURL));
	}
	
	public List<String> getResources(String queryString){
		return getResources(queryString, 0);
	}
	
	public List<String> getResources(String queryString, int offset){
		List<String> resources = new ArrayList<String>();
		
		String query = buildSearchQuery(queryString, hitsPerPage, offset);
		try {
			ResultSet rs = executeQuery(query);
			
			String uri;
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				uri = qs.getResource("s").getURI();
				resources.add(uri);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resources;
	}
	
	public List<Example> getExamples(String queryString){
		return getExamples(queryString, 0);
	}
	
	public List<Example> getExamples(String queryString, int offset){
		List<Example> examples = new ArrayList<Example>();
		
		String query = buildSearchQueryExtended(queryString, hitsPerPage, offset);
		
		ResultSet rs = executeQuery(query);
		String uri;
		String label;
		String imageURL;
		String comment;
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			uri = qs.getResource("s").getURI();
			label = qs.getLiteral("label").getLexicalForm();
			imageURL = qs.getResource("image").getURI();
			comment = qs.getLiteral("comment").getLexicalForm();
			examples.add(new Example(uri, label, imageURL, comment));
		}
		return examples;
	}
	
	public int getTotalHits(String queryString){
		ResultSet rs = executeQuery(buildCountQuery(queryString));
		return rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
	}
	
	public void setHitsPerPage(int hitsPerPage){
		this.hitsPerPage = hitsPerPage;
	}
	
	private String getAngleBracketsString(String str){
		return "<" + str + ">";
	}
	
	private String buildSearchQuery(String queryString, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT distinct(?s) WHERE {\n");
		sb.append("?s ").append(getAngleBracketsString(RDFS.label.toString())).append(" ?label.\n");
		sb.append("FILTER(LANGMATCHES(LANG(?label), \"en\"))\n");
		sb.append("FILTER(bif:contains(?label, '" + queryString + "'))\n");
		sb.append("}\n");
		if(hitsPerPage > 0){
			sb.append("LIMIT ").append(hitsPerPage);
		}
		if(offset > 0){
			sb.append(" OFFSET ").append(offset);
		}
		return sb.toString();
	}
	
	private String buildSearchQueryExtended(String queryString, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT distinct(?s) WHERE {\n");
		sb.append("?s a ?class.\n");
		sb.append("?s ").append(getAngleBracketsString(RDFS.label.toString())).append(" ?label.\n");
		sb.append("FILTER(LANGMATCHES(LANG(?label), \"en\"))\n");
		sb.append("FILTER(bif:contains(?label, '" + queryString + "'))\n");
		sb.append("OPTIONAL{?s <http://dbpedia.org/ontology/thumbnail> ?imageURL.}\n");
		sb.append("OPTIONAL{?s <").append(RDFS.comment).append("> ?comment.\n");
		sb.append("FILTER(LANGMATCHES(LANG(?comment), \"en\"))}");
		sb.append("}\n");
		if(hitsPerPage > 0){
			sb.append("LIMIT ").append(hitsPerPage);
		}
		if(offset > 0){
			sb.append(" OFFSET ").append(offset);
		}
		return sb.toString();
	}
	
	private ResultSet executeQuery(String query){
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		return queryExecution.execSelect();
	}
	
	private String buildCountQuery(String searchTerm){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(distinct ?object) WHERE {\n");
		sb.append("?object a ?class.\n");
		sb.append("?object <").append(RDFS.label).append("> ?label.\n");
		sb.append("?label bif:contains '\"").append(searchTerm).append("\"'.\n");
		sb.append("}\n");
		return sb.toString();
	}

}
