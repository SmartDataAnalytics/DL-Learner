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
import org.autosparql.shared.Example;

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
	
	public List<String> getResources(String query, String type) {
		return getResources(query, type, LIMIT, OFFSET);
	}

	public List<String> getResources(String query, String type, int limit) {
		return getResources(query, type, limit, OFFSET);
	}

	public List<String> getResources(String query, String type, int limit, int offset) {
		List<String> resources = new ArrayList<String>();
		
		SolrQuery q = new SolrQuery(buildQueryString(query, type));
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
	public List<Example> getExamples(String query) {
		return getExamples(query, LIMIT, OFFSET);
	}

	@Override
	public List<Example> getExamples(String query, int limit) {
		return getExamples(query, limit, OFFSET);
	}

	@Override
	public List<Example> getExamples(String query, int limit, int offset) {
		List<Example> resources = new ArrayList<Example>();
		
		SolrQuery q = new SolrQuery(buildQueryString(query));
		q.setRows(limit);
		q.setStart(offset);
		try {
			QueryResponse response = server.query(q);
			SolrDocumentList docList = response.getResults();
			String uri;
			String label;
			String imageURL;
			String comment;
			for(SolrDocument d : docList){
				uri = (String) d.get("uri");
				label = (String) d.get("label");
				imageURL = (String) d.get("imageURL");
				comment = (String) d.get("comment");
				resources.add(new Example(uri, label, imageURL, comment));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return resources;
	}
	
	public List<Example> getExamples(String query, String type) {
		return getExamples(query, type, LIMIT, OFFSET);
	}

	public List<Example> getExamples(String query, String type, int limit) {
		return getExamples(query, type, limit, OFFSET);
	}

	public List<Example> getExamples(String query, String type, int limit, int offset) {
		List<Example> resources = new ArrayList<Example>();
		
		SolrQuery q = new SolrQuery(buildQueryString(query, type));
		q.setRows(limit);
		q.setStart(offset);
		try {
			QueryResponse response = server.query(q);
			SolrDocumentList docList = response.getResults();
			String uri;
			String label;
			String imageURL;
			String comment;
			for(SolrDocument d : docList){
				uri = (String) d.get("uri");
				label = (String) d.get("label");
				imageURL = (String) d.get("imageURL");
				comment = (String) d.get("comment");
				resources.add(new Example(uri, label, imageURL, comment));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return resources;
	}
	
	public List<String> getTypes(String term){
		List<String> types = new ArrayList<String>();
		try {
			CommonsHttpSolrServer server = new CommonsHttpSolrServer("http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_classes");
			server.setRequestWriter(new BinaryRequestWriter());
			SolrQuery q = new SolrQuery("label:" + term);
			QueryResponse response = server.query(q);
			SolrDocumentList docList = response.getResults();
			for(SolrDocument d : docList){
				types.add((String) d.get("uri"));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return types;
	}
	
	private String buildQueryString(String query){
		return "comment:(" + query + ") AND NOT label:(" + query + ")";
	}
	
	private String buildQueryString(String query, String type){
		return "comment:(" + query + ") AND NOT label:(" + query + ") AND types:\"" + type + "\"";
	}

}
