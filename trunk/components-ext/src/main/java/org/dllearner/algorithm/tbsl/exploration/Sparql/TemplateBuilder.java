package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sparql.BasicQueryTemplate;
import org.dllearner.algorithm.tbsl.sparql.Path;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Filter;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Having;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Term;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;

public class TemplateBuilder {

	static BasicTemplator btemplator;
	
	
public TemplateBuilder() throws MalformedURLException, ClassNotFoundException, SQLException{
		
		TemplateBuilder.btemplator = new BasicTemplator();
    	//btemplator.UNTAGGED_INPUT = false;
    	//Object_new.myindex = new mySQLDictionary();
	}
		
	
	public ArrayList<Template> createTemplates(String question){
		ArrayList<Template> resultArrayList = new ArrayList<Template>();
		Set<BasicQueryTemplate> querytemps = btemplator.buildBasicQueries(question);
     	for (BasicQueryTemplate bqt : querytemps) {
     		ArrayList<ArrayList<String>> condition = new ArrayList<ArrayList<String>>();
     		ArrayList<ArrayList<Hypothesis>> hypotesen = new ArrayList<ArrayList<Hypothesis>>();
     		String selectTerm = "";
     		String having= "";
     		String filter= "";
     		String OrderBy= "";
     		String limit= "";
     		String condition_String = "";
     		
     		boolean addTemplate=true;
     		try{
     			for(SPARQL_Term terms :bqt.getSelTerms()) selectTerm=selectTerm+(terms.toString())+" ";
     		}
     		catch (Exception e){
     			selectTerm="";
     			addTemplate=false;
     		}
     		
     		ArrayList<String> temp_array = new ArrayList<String>();
			try{
     			for(Path conditions1: bqt.getConditions()) condition_String=condition_String+(conditions1.toString())+".";
     			for(Path conditions1: bqt.getConditions()) {
     				temp_array.clear();
     				String[] tmp_array = conditions1.toString().split(" -- ");
     				for(String s: tmp_array){
     					temp_array.add(s);
     				}
     				condition.add(temp_array);
     			}
     					
     		}
     		catch (Exception e){
     			condition_String="";
     			addTemplate=false;
     		}
     		
     		try{
     			for(SPARQL_Filter tmp : bqt.getFilters()) filter=filter+tmp+" ";
     		}
     		catch(Exception e){
     			filter="";
     			addTemplate=false;
     		}
     		try{
     			for(SPARQL_Having tmp : bqt.getHavings()) having=having+tmp+" ";
     		}
     		catch(Exception e){
     			having="";
     			addTemplate=false;
     		}	
     		
     		//if there is no order by, replace with ""
     		OrderBy="ORDER BY ";
     		try{
     			for(SPARQL_Term tmp : bqt.getOrderBy()) {
     				System.out.println("Yeah");
     				OrderBy=OrderBy+tmp+" ";
     			}
     			if((bqt.getOrderBy()).size()==0)OrderBy="";
     		}
     		catch(Exception e){
     			OrderBy="";
     			addTemplate=false;
     		}
     		
     		
     		try{
     			limit="LIMIT "+bqt.getLimit();
     			
     			if(bqt.getLimit()==0)limit="";
     		}
     		catch(Exception e){
     			limit="";
     			addTemplate=false;
     		}
     		
     		Template template = new Template(condition, having, filter, selectTerm,OrderBy, limit);
     		
     		//TODO: Add Hypothesis
     		//TODO: Take Template like it is and change Condition
     		
     		
     		resultArrayList.add(template);
     	}
     	for(Template temp : resultArrayList){
     		temp.printAll();
     	}
		return resultArrayList;
	}
}
