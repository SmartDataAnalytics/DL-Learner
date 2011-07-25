package org.autosparql.server;

import java.util.List;

import org.autosparql.server.search.Search;
import org.autosparql.server.search.SolrSearch;
import org.autosparql.server.search.TBSLSearch;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class AutoSPARQLSession {
	
	
	private Search primarySearch;
	private Search secondarySearch;
	
	public AutoSPARQLSession(SparqlEndpoint endpoint, String solrServerURL) {
		primarySearch = new TBSLSearch(endpoint);
		secondarySearch = new SolrSearch(solrServerURL);
	}
	
	public List<String> getResources(String query){
		
		List<String> resources = primarySearch.getResources(query);
		if(resources.isEmpty()){
			resources = secondarySearch.getResources(query);
		}
		
		return resources;
	}

}
