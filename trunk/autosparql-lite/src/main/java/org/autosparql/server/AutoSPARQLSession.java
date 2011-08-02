package org.autosparql.server;

import java.util.ArrayList;
import java.util.List;

import org.autosparql.server.search.SolrSearch;
import org.autosparql.server.search.TBSLSearch;
import org.autosparql.shared.Example;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class AutoSPARQLSession {
	
	
	private TBSLSearch primarySearch;
	private SolrSearch secondarySearch;
	
	public AutoSPARQLSession(SparqlEndpoint endpoint, String solrServerURL) {
		primarySearch = new TBSLSearch(endpoint);
		secondarySearch = new SolrSearch(solrServerURL);
	}
	
	public List<String> getResources(String query){
		List<String> resources = new ArrayList<String>();
		
		resources = primarySearch.getResources(query);
		if(resources.isEmpty()){
			List<String> answerType = primarySearch.getLexicalAnswerType();
			List<String> types = secondarySearch.getTypes(answerType.get(0));
			
			for(String type : types){
				resources = secondarySearch.getResources(query, type);
				if(!resources.isEmpty()){
					return resources;
				}
			}
		}
		
		return resources;
	}
	
	public List<Example> getExamples(String query){
		List<Example> examples = new ArrayList<Example>();
		
		examples = primarySearch.getExamples(query);
		if(examples.isEmpty()){
			List<String> answerType = primarySearch.getLexicalAnswerType();
			List<String> types = secondarySearch.getTypes(answerType.get(0));
			
			for(String type : types){
				examples = secondarySearch.getExamples(query, type);
				if(!examples.isEmpty()){
					return examples;
				}
			}
		}
		
		return examples;
	}

}
