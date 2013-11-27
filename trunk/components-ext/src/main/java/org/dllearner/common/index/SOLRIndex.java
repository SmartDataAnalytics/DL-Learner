package org.dllearner.common.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SOLRIndex extends Index{
	
private HttpSolrServer server;
	
	private String primarySearchField;
	private String secondarySearchField;
	
	private String sortField;
	
	private boolean restrictiveSearch = true;
	
	public SOLRIndex(String solrServerURL){
		server = new HttpSolrServer(solrServerURL);
		server.setRequestWriter(new BinaryRequestWriter());
	}
	
	public SOLRIndex(String solrServerURL, String primarySearchField){
		server = new HttpSolrServer(solrServerURL);
		server.setRequestWriter(new BinaryRequestWriter());
		this.primarySearchField = primarySearchField;
	}
	
	public void setSearchFields(String primarySearchField, String secondarySearchField){
		this.primarySearchField = primarySearchField;
		this.secondarySearchField = secondarySearchField;
	}
	
	public void setPrimarySearchField(String primarySearchField) {
		this.primarySearchField = primarySearchField;
	}
	
	public void setSecondarySearchField(String secondarySearchField) {
		this.secondarySearchField = secondarySearchField;
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
	public IndexResultSet getResourcesWithScores(String queryString, int limit, int offset) {
		IndexResultSet rs = new IndexResultSet();
		
		QueryResponse response;
		try {
			String solrString = queryString;
			if(primarySearchField != null){
				solrString = primarySearchField + ":" + "\"" + queryString + "\"" + "^2 ";
				if(restrictiveSearch){
					String[] tokens = queryString.split(" ");
					if(tokens.length > 1){
						solrString += " OR (";
						for(int i = 0; i < tokens.length; i++){
							String token = tokens[i];
							solrString += primarySearchField + ":" + token;
							if(i < tokens.length-1){
								solrString += " AND ";
							}
						}
						solrString += ")";
					}
					
				} else {
					solrString += queryString;
				}
			}			
			SolrQuery query = new SolrQuery(solrString);
		    query.setRows(limit);
		    query.setStart(offset);
		    query.addField("uri");
		    if(sortField != null){
		    	query.addSortField(sortField, ORDER.desc);
		    }
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
	
	public void setSortField(String sortField){
		this.sortField = sortField;
	}

}