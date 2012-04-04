package org.dllearner.algorithm.tbsl.exploration.Utils;

import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Template;

public class Query {

	/**
	 * Creates Queries
	 * @param t
	 * @return
	 */
	public static ArrayList<QueryPair> returnSetOfQueries(Template t, String type){
		
		ArrayList<QueryPair> queryList = new ArrayList<QueryPair>(); 
		/*
		 * TODO: Generate a Query for each Hypothesenset, replacing the variable in the condition with the uri from the hypothesenset in <>
		 */
		String condition="";
		for(ArrayList<String> conditionList :t.getCondition()){
			for(String s : conditionList) condition+=s+" ";
			//to End a triple of variables
			condition+=".";
		}
		/*
		 * Now replacing varibale with the uri from the Hypot.
		 */
		ArrayList<ArrayList<Hypothesis>> givenHypothesenList = new ArrayList<ArrayList<Hypothesis>>() ;
		if(type.contains("LEVENSTHEIN")){
			givenHypothesenList=t.getHypothesenLevensthein();
		}
		else if(type.contains("WORDNET")){
			givenHypothesenList=t.getHypothesenWordnet();
		}
		
		else {
			if(!type.contains("NORMAL"))System.err.println("ATTENTION\n Given Type: "+type+" was not found in generating Queries!!\n");
			givenHypothesenList=t.getHypothesen();
		}
		
		for(ArrayList<Hypothesis> hypothesenList : givenHypothesenList){
			String condition_new = condition;
			//System.out.println("New_Condition before replacing "+condition_new);
			double global_rank=0;
			boolean addQuery=true;
			for(Hypothesis h : hypothesenList){
				condition_new=condition_new.replace(h.getVariable(), "<"+h.getUri()+">");
				/*
				 * Dont create a Query with variables, which dont have a correct uri
				 */
				if(!h.getUri().contains("http")){
					addQuery=false;
				}
				condition_new=condition_new.replace("ISA", "rdf:type");
				//just in case...
				condition_new=condition_new.replace("isA", "rdf:type");
				global_rank=global_rank+h.getRank();
			}
			//System.out.println("New_Condition after replacing "+condition_new);
	    	String query="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+t.getQueryType()+" "+t.getSelectTerm()+" WHERE {"+ condition_new+" "+ t.getFilter()+"}"+t.getOrderBy()+" "+t.getHaving() +" "+t.getLimit();
	    	QueryPair qp = new QueryPair(query,global_rank);
	    	if(addQuery)queryList.add(qp);
		}
    	
		
		return queryList;
	}
	
	/*public static QueryPair getBestQuery(Template t){
		ArrayList<QueryPair> qp = returnSetOfQueries(t);
		
		
		return ;
	}*/
	
}
