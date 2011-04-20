package org.dllearner.autosparql.client.model;

import java.io.Serializable;

public class StoredSPARQLQuery implements Serializable{
	
	private static final long serialVersionUID = 8195456787955752936L;
	
	private String question;
	private String query;
	private String queryHTML;
	private String endpoint;
	
	private int hitCount = 0;
	
	public StoredSPARQLQuery(){
	}
	
	public StoredSPARQLQuery(String question, String query, String queryHTML, String endpoint){
		this.question = question;
		this.query = query;
		this.endpoint = endpoint;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getQueryHTML() {
		return queryHTML;
	}

	public void setQueryHTML(String queryHTML) {
		this.queryHTML = queryHTML;
	}

	public int getHitCount() {
		return hitCount;
	}
	
	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	@Override
	public String toString() {
		return question + "@" + endpoint + ":\n" + query;
	}

}
