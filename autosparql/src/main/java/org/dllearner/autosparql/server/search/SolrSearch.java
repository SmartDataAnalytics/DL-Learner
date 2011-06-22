package org.dllearner.autosparql.server.search;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.dllearner.autosparql.client.model.Example;

public class SolrSearch implements Search{
	
	private CommonsHttpSolrServer server;
	
	private int hitsPerPage = 10;
	private int lastTotalHits = 0;
	
	private QuestionProcessor preprocessor;
	
	public SolrSearch(String solrServerURL){
		try {
			server = new CommonsHttpSolrServer(solrServerURL);
			server.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		preprocessor = new QuestionProcessor();
	}
	
	public SolrSearch(String solrServerURL, QuestionProcessor preprocessor){
		try {
			server = new CommonsHttpSolrServer(solrServerURL);
			server.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.preprocessor = preprocessor;
	}

	@Override
	public List<String> getResources(String queryString) {
		return getResources(queryString, 0);
	}

	@Override
	public List<String> getResources(String queryString, int offset) {
		List<String> resources = new ArrayList<String>();
		QueryResponse response;
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", queryString);
			params.set("rows", hitsPerPage);
			params.set("start", offset);
			response = server.query(params);
			SolrDocumentList docList = response.getResults();
			lastTotalHits = (int) docList.getNumFound();
			for(SolrDocument d : docList){
				resources.add((String) d.get("uri"));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return resources;
	}
	
	public Map<String, Float> getResourcesWithScores(String queryString) {
		return getResourcesWithScores(queryString, hitsPerPage);
	}
	
	public Map<String, Float> getResourcesWithScores(String queryString, int limit) {
		return getResourcesWithScores(queryString, limit, 0);
	}
	
	public Map<String, Float> getResourcesWithScores(String queryString, int limit, int offset) {
		Map<String, Float> resource2ScoreMap = new HashMap<String, Float>();
		
		QueryResponse response;
		try {
			SolrQuery query = new SolrQuery();
		    query.setQuery(queryString);
		    query.setRows(hitsPerPage);
		    query.setStart(offset);
		    query.addField("score");
		    query.addSortField("score", SolrQuery.ORDER.desc);
		    query.addSortField( "pagerank", SolrQuery.ORDER.desc );
		    
			response = server.query(query);
			SolrDocumentList docList = response.getResults();
			lastTotalHits = (int) docList.getNumFound();
			for(SolrDocument d : docList){
				resource2ScoreMap.put((String) d.get("uri"), (Float) d.get("score"));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return resource2ScoreMap;
	}

	@Override
	public List<Example> getExamples(String queryString) {
		return getExamples(queryString, 0);
	}

	@Override
	public List<Example> getExamples(String queryString, int offset) {
		List<Example> resources = new ArrayList<Example>();
		QueryResponse response;
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			if(queryString.startsWith("label:") || queryString.startsWith("uri:")){
				params.set("q", queryString);
			} else {
				List<String> relevantWords = preprocessor.getRelevantWords(queryString);
				params.set("q", "{!boost b=pagerank v=$qq}");
				StringBuilder sb = new StringBuilder();
				for(String word : relevantWords){
					sb.append("comment:\"").append(word).append("\" ");
				}
				sb.append(" AND ");
				for(int i = 0; i < relevantWords.size(); i++){
					sb.append("-label:\"").append(relevantWords.get(i)).append("\"");
					if(i < relevantWords.size()-1){
						sb.append(" AND ");
					}
				}
				params.set("qq", sb.toString());
			}
			params.set("rows", hitsPerPage);
			params.set("start", offset);
//			params.set("sort", "score+desc,pagerank+desc");
			response = server.query(params);
			SolrDocumentList docList = response.getResults();
			lastTotalHits = (int) docList.getNumFound();
			Example example;
			for(SolrDocument d : docList){
				example = new Example((String) d.get("uri"), (String) d.get("label"),
						(String) d.get("imageURL"), (String) d.get("comment"));
				resources.add(example);
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return resources;
	}

	@Override
	public int getTotalHits(String queryString) {
		return lastTotalHits;
	}

	@Override
	public void setHitsPerPage(int hitsPerPage) {
		this.hitsPerPage = hitsPerPage;
	}

}
