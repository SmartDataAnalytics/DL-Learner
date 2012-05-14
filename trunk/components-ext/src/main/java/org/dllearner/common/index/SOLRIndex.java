package org.dllearner.common.index;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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
	
	public SOLRIndex(String solrServerURL){
		try {
			server = new CommonsHttpSolrServer(solrServerURL);
			server.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
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

}
