package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.util.ArrayList;

public class Template {

	private ArrayList<ArrayList<String>> condition = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<Hypothesis>> hypothesen = new ArrayList<ArrayList<Hypothesis>>();
	private String selectTerm;
	private String having;
	private String filter;
	private String OrderBy;
	private String limit;
	
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
	
	
	public Template(ArrayList<ArrayList<String>>condition_new, String having_new, String filter_new, String SelectTerm_new, String OrderBy_new, String limit_new){
		setCondition(condition_new);
		setHaving(having_new);
		setFilter(filter_new);
		setOrderBy(OrderBy_new);
		setLimit(limit_new);
		setSelectTerm(SelectTerm_new);
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
		System.out.println("condition: "+condition);
		//System.out.println("hypotesen: "+hypothesen);
		int anzahl = 1;
		for(ArrayList<Hypothesis> x : hypothesen){
			System.out.println("\nSet of Hypothesen"+anzahl+":");
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

}

