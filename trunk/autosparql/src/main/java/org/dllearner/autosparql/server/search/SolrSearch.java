package org.dllearner.autosparql.server.search;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
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
	
	public SolrSearch(String solrServerURL){
		try {
			server = new CommonsHttpSolrServer(solrServerURL);
			server.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
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
			params.set("q", queryString);
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
