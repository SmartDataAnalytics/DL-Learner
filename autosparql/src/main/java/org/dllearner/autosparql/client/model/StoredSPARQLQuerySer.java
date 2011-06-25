package org.dllearner.autosparql.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoredSPARQLQuerySer implements Serializable{
	
	private static final long serialVersionUID = 8195456787955752936L;
	
	private String question;
	private String query;
	private String queryHTML;
	private String endpoint;
	private List<ExampleSer> posExamples;
	private List<ExampleSer> negExamples;
	private ExampleSer lastSuggestedExample;
	private Date date;
	
	private int hitCount = 0;
	
	public StoredSPARQLQuerySer(){
	}
	
	public StoredSPARQLQuerySer(String question, String query, String queryHTML, String endpoint, 
			List<ExampleSer> posExamples, List<ExampleSer> negExamples, ExampleSer lastSuggestedExample, Date date, int hitCount){
		this.question = question;
		this.query = query;
		this.endpoint = endpoint;
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		this.lastSuggestedExample = lastSuggestedExample;
		this.hitCount = hitCount;
		this.date = date;
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
	
	public List<ExampleSer> getPositiveExamples() {
		return posExamples;
	}

	public List<ExampleSer> getNegativeExamples() {
		return negExamples;
	}
	
	public ExampleSer getLastSuggestedExample() {
		return lastSuggestedExample;
	}

	@Override
	public String toString() {
		return question + "@" + endpoint + ":\n" + query;
	}
	
	public StoredSPARQLQuery toStoredSPARQLQuery(){
		List<Example> posExamples = new ArrayList<Example>();
		List<Example> negExamples = new ArrayList<Example>();
		for(ExampleSer ex : this.posExamples){
			posExamples.add(ex.toExample());
		}
		for(ExampleSer ex : this.negExamples){
			negExamples.add(ex.toExample());
		}
		Example lastSuggestedExample = null;
		if(this.lastSuggestedExample != null){
			lastSuggestedExample = this.lastSuggestedExample.toExample();
		}
		StoredSPARQLQuery q = new StoredSPARQLQuery(question, query, queryHTML, endpoint, posExamples, negExamples, lastSuggestedExample, date, hitCount);
		return q;
	}

}
