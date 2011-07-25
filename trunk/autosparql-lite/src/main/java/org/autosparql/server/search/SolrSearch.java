package org.autosparql.server.search;

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

public class SolrSearch implements Search{
	
	private static final int LIMIT = 10;
	private static final int OFFSET = 0;
	
	private CommonsHttpSolrServer server;
	
	
	public SolrSearch(String serverURL){
		try {
			server = new CommonsHttpSolrServer(serverURL);
			server.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getResources(String query) {
		return getResources(query, LIMIT);
	}

	@Override
	public List<String> getResources(String query, int limit) {
		return getResources(query, limit, OFFSET);
	}

	@Override
	public List<String> getResources(String query, int limit, int offset) {
		List<String> resources = new ArrayList<String>();
		
		SolrQuery q = new SolrQuery(buildQueryString(query));
		q.setRows(limit);
		q.setStart(offset);
		try {
			QueryResponse response = server.query(q);
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
	public List<String> getExamples(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getExamples(String query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getExamples(String query, int limit, int offset) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String buildQueryString(String query){
		return "comment:(" + query + ") AND NOT label:(" + query + ")";
	}

}
