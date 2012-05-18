package org.dllearner.common.index;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SOLRIndex implements Index{
	
private CommonsHttpSolrServer server;
	
	private static final int DEFAULT_LIMIT = 10;
	private static final int DEFAULT_OFFSET = 0;
	
	private String searchField;
	
	public SOLRIndex(String solrServerURL){
		try {
			server = new CommonsHttpSolrServer(solrServerURL);
			server.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void setSearchField(String searchField) {
		this.searchField = searchField;
	}
	
	@Override
	public List<String> getResources(String queryString) {
		return getResources(queryString, DEFAULT_LIMIT);
	}

	@Override
	public List<String> getResources(String queryString, int limit) {
		return getResources(queryString, limit, DEFAULT_OFFSET);
	}

	@Override
	public List<String> getResources(String queryString, int limit, int offset) {
		List<String> resources = new ArrayList<String>();
		QueryResponse response;
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", queryString);
			params.set("rows", limit);
			params.set("start", offset);
			response = server.query(params);
			SolrDocumentList docList = response.getResults();
			for(SolrDocument d : docList){
				resources.add((String) d.get("uri"));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return resources;
	}

	@Override
	public IndexResultSet getResourcesWithScores(String queryString) {
		return getResourcesWithScores(queryString, DEFAULT_LIMIT);
	}

	@Override
	public IndexResultSet getResourcesWithScores(String queryString, int limit) {
		return getResourcesWithScores(queryString, limit, DEFAULT_OFFSET);
	}

	@Override
	public IndexResultSet getResourcesWithScores(String queryString, int limit, int offset) {
		IndexResultSet rs = new IndexResultSet();
		
		QueryResponse response;
		try {
			SolrQuery query = new SolrQuery((searchField != null) ? searchField  + ":" + queryString : queryString);
		    query.setRows(limit);
		    query.setStart(offset);
		    query.addField("score");
			response = server.query(query);
			SolrDocumentList docList = response.getResults();
			
			for(SolrDocument d : docList){
				float score = 0;
				if(d.get("score") instanceof ArrayList){
					score = ((Float)((ArrayList)d.get("score")).get(1));
				} else {
					score = (Float) d.get("score");
				}
				rs.addItem(new IndexResultItem((String) d.get("uri"), (String) d.get("label"), score));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return rs;
	}

}
