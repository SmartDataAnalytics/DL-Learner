package org.dllearner.algorithm.tbsl.search;

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

public class SolrSearch implements Search{
	
	private CommonsHttpSolrServer server;
	
	private int hitsPerPage = 10;
	private int lastTotalHits = 0;
	
	private String searchField;
	
	public SolrSearch() {
		// TODO Auto-generated constructor stub
	}
	
	public SolrSearch(String solrServerURL){
		try {
			server = new CommonsHttpSolrServer(solrServerURL);
			server.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public SolrSearch(String solrServerURL, String searchField){
		this(solrServerURL);
		this.searchField = searchField;
	}

	@Override
	public List<String> getResources(String queryString) {
		return getResources(queryString, hitsPerPage);
	}
	
	@Override
	public List<String> getResources(String queryString, int limit) {
		return getResources(queryString, limit, 0);
	}

	@Override
	public List<String> getResources(String queryString, int limit, int offset) {
		List<String> resources = new ArrayList<String>();
		QueryResponse response;
		try {
		SolrQuery q = new SolrQuery((searchField != null) ? searchField  + ":" + queryString : queryString);
		q.setStart(offset);
		q.setRows(limit);
		response = server.query(q);
//			ModifiableSolrParams params = new ModifiableSolrParams();
//			params.set("q", queryString);
//			params.set("rows", hitsPerPage);
//			params.set("start", offset);
//			response = server.query(params);
			
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
	
	public Map<String, Float> getResourcesWithScores(String queryString, boolean sorted) {
		return getResourcesWithScores(queryString, hitsPerPage);
	}
	
	public Map<String, Float> getResourcesWithScores(String queryString, int limit) {
		return getResourcesWithScores(queryString, limit, 0, false);
	}
	
	public Map<String, Float> getResourcesWithScores(String queryString, int limit, boolean sorted) {
		return getResourcesWithScores(queryString, limit, 0, sorted);
	}
	
	public Map<String, Float> getResourcesWithScores(String queryString, int limit, int offset, boolean sorted) {
		Map<String, Float> resource2ScoreMap = new HashMap<String, Float>();
		
		QueryResponse response;
		try {
			SolrQuery query = new SolrQuery();
		    query.setQuery(queryString);
		    query.setRows(hitsPerPage);
		    query.setStart(offset);
		    query.addField("score");
		    if(sorted){
		    	 query.addSortField("score", SolrQuery.ORDER.desc);
				    query.addSortField( "pagerank", SolrQuery.ORDER.desc );
		    }
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
	public int getTotalHits(String queryString) {
		return lastTotalHits;
	}

	@Override
	public void setHitsPerPage(int hitsPerPage) {
		this.hitsPerPage = hitsPerPage;
	}

}
