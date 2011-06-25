package org.dllearner.autosparql.client.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModel;

public class StoredSPARQLQuery extends BaseModel{
	
	private static final long serialVersionUID = 8195456787955752936L;
	
	private String question;
	private String query;
	private String queryHTML;
	private String endpoint;
	private List<Example> posExamples;
	private List<Example> negExamples;
	private Example lastSuggestedExample;
	private Date date;
	
	private int hitCount = 0;
	
	public StoredSPARQLQuery(){
	}
	
	public StoredSPARQLQuery(String question, String query, String queryHTML, String endpoint, List<Example> posExamples, 
			List<Example> negExamples, Example lastSuggestedExample, Date date, int hitCount){
		this.question = question;
		this.query = query;
		this.endpoint = endpoint;
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		this.lastSuggestedExample = lastSuggestedExample;
		this.hitCount = hitCount;
		this.date = date;
		set("question", question);
		set("endpoint", endpoint);
		set("date", date);
		set("hitCount", hitCount);
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
	
	public List<Example> getPositiveExamples() {
		return posExamples;
	}

	public List<Example> getNegativeExamples() {
		return negExamples;
	}
	
	public Example getLastSuggestedExample() {
		return lastSuggestedExample;
	}

	@Override
	public String toString() {
		return question + "@" + endpoint + ":\n" + query;
	}
	
	public StoredSPARQLQuerySer toStoredSPARQLQuerySer(){
		List<ExampleSer> posExamples = new ArrayList<ExampleSer>();
		List<ExampleSer> negExamples = new ArrayList<ExampleSer>();
		for(Example ex : this.posExamples){
			posExamples.add(ex.toExampleSer());
		}
		for(Example ex : this.negExamples){
			negExamples.add(ex.toExampleSer());
		}
		ExampleSer lastSuggestedExample = null;
		if(this.lastSuggestedExample != null){
			lastSuggestedExample = this.lastSuggestedExample.toExampleSer();
		}
		return new StoredSPARQLQuerySer(question, query, queryHTML, endpoint, posExamples, negExamples, lastSuggestedExample, date, hitCount);
	}

}
