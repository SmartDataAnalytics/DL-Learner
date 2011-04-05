package org.dllearner.autosparql.client.model;

import java.io.Serializable;

public class StoredSPARQLQuery implements Serializable{
	
	private static final long serialVersionUID = 8195456787955752936L;
	
	private String question;
	private String query;
	private Endpoint endpoint;
	private int hitCount = 0;
	
	public StoredSPARQLQuery(){
	}
	
	public StoredSPARQLQuery(String question, String query, Endpoint endpoint){
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
	public int getHitCount() {
		return hitCount;
	}
	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

}
