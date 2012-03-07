package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.util.ArrayList;

public class Template {

	private ArrayList<ElementList_new> list_of_element_uri_pair = new ArrayList<ElementList_new>();
	private ArrayList<ArrayList<String>> condition = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<Hypothesis>> hypothesen = new ArrayList<ArrayList<Hypothesis>>();
	private String selectTerm;
	private String having;
	private String filter;
	private String OrderBy;
	private String limit;
	private String question;
	private float overallTime;
	private float time_Templator;
	
	public String getHaving() {
		return having;
	}
	public void setHaving(String having) {
		this.having = having;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public String getOrderBy() {
		return OrderBy;
	}
	public void setOrderBy(String orderBy) {
		OrderBy = orderBy;
	}
	public String getLimit() {
		return limit;
	}
	public void setLimit(String limit) {
		this.limit = limit;
	}
	
	public String getSelectTerm() {
		return selectTerm;
	}
	public void setSelectTerm(String selectTerm) {
		this.selectTerm = selectTerm;
	}
	
	
	public Template(ArrayList<ArrayList<String>>condition_new, String having_new, String filter_new, String SelectTerm_new, String OrderBy_new, String limit_new, String question_new){
		this.setCondition(condition_new);
		this.setHaving(having_new);
		this.setFilter(filter_new);
		this.setOrderBy(OrderBy_new);
		this.setLimit(limit_new);
		this.setSelectTerm(SelectTerm_new);
		this.setQuestion(question_new);
	}
	public ArrayList<ArrayList<String>> getCondition() {
		return condition;
	}
	public void setCondition(ArrayList<ArrayList<String>> condition) {
		this.condition = condition;
	}
	public ArrayList<ArrayList<Hypothesis>> getHypothesen() {
		return hypothesen;
	}
	public void setHypothesen(ArrayList<ArrayList<Hypothesis>> hypotesen) {
		this.hypothesen = hypotesen;
	}
	
	public void addHypothese(ArrayList<Hypothesis> ht){
		this.hypothesen.add(ht);
	}
	
	
	public void printAll(){
		System.out.println("###### Template ######");
		System.out.println("question: "+ question);
		System.out.println("condition: "+condition);
		//System.out.println("hypotesen: "+hypothesen);
		int anzahl = 1;
		for(ArrayList<Hypothesis> x : hypothesen){
			System.out.println("\nSet of Hypothesen"+anzahl+":");
			anzahl+=1;
			for ( Hypothesis z : x){
				z.printAll();
			}
		}
		System.out.print("\n");
		System.out.println("selectTerm: "+selectTerm);
		System.out.println("having: "+having);
		System.out.println("filter: "+filter);
		System.out.println("OrderBy: "+OrderBy);
		System.out.println("limit: "+limit);
		System.out.println("###### Template printed ######\n");
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public float getOverallTime() {
		return overallTime;
	}
	public void setOverallTime(float overallTime) {
		this.overallTime = overallTime;
	}
	public float getTime_Templator() {
		return time_Templator;
	}
	public void setTime_Templator(float time_Templator) {
		this.time_Templator = time_Templator;
	}
	public ArrayList<ElementList_new> getList_of_element_uri_pair() {
		return list_of_element_uri_pair;
	}
	public void setList_of_element_uri_pair(ArrayList<ElementList_new> list_of_element_uri_pair) {
		this.list_of_element_uri_pair = list_of_element_uri_pair;
	}
	
	public void addToList_of_element_uri_pair(ElementList_new newElement) {
		this.list_of_element_uri_pair.add(newElement);
	}
	

}

