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
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;

public class TemplateBuilder {

static BasicTemplator btemplator;
private static mySQLDictionary myindex;
	
	
public TemplateBuilder() throws MalformedURLException, ClassNotFoundException, SQLException{
		
		TemplateBuilder.btemplator = new BasicTemplator();
    	//btemplator.UNTAGGED_INPUT = false;
		TemplateBuilder.myindex = new mySQLDictionary();
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
     		
     		if(addTemplate!=false){
 
 //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    			     		
     			Template template = new Template(condition, having, filter, selectTerm,OrderBy, limit);
     			//TODO: Iterate over slots
     			ArrayList<Hypothesis> list_of_hypothesis = new ArrayList<Hypothesis>();
     			for(Slot slot : bqt.getSlots()){
	    			
     				if(slot.toString().contains("USPEC")){
     					String tmp= slot.toString().replace(" UNSPEC {", "");
     					tmp=tmp.replace("}","");
     					String[] tmp_array = tmp.split(":");
     					Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0], tmp_array[1], "USPEC", 0);
     					list_of_hypothesis.add(tmp_hypothesis);
     				}
     				if(slot.toString().contains("PROPERTY")){
     					String tmp= slot.toString().replace(" PROPERTY {", "");
     					tmp=tmp.replace("}","");
     					String[] tmp_array = tmp.split(":");
     					Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0], tmp_array[1], "PROPERTY", 0);
     					list_of_hypothesis.add(tmp_hypothesis);
     					
     				}
     				if(slot.toString().contains("RESOURCE")){
     					String tmp= slot.toString().replace(" RESOURCE {", "");
     					tmp=tmp.replace("}","");
     					String[] tmp_array = tmp.split(":");
     					Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0], tmp_array[1], "RESOURCE", 0);
     					list_of_hypothesis.add(tmp_hypothesis);
     				}
     			}
     			ArrayList<ArrayList<Hypothesis>> final_list_set_hypothesis = new ArrayList<ArrayList<Hypothesis>>();
 
 //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    			
     			
     			for(Hypothesis x : list_of_hypothesis){
     				if(x.getType().contains("RESOURCE")){
     					ArrayList<String> result= new ArrayList<String>();
     					try {
							result = utils_new.searchIndex(x.getUri(), 3, myindex);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
     					for(String s : result){
     						ArrayList<Hypothesis> new_list = new ArrayList<Hypothesis>();
     						new_list=list_of_hypothesis;
     						for(Hypothesis z : new_list){
     							if(z.getUri().equals(x.getUri())){
     								z.setUri(s);
     								z.setRank(1);
     							}
     						}
     						final_list_set_hypothesis.add(new_list);
     					}
     				}
     			}
     			
     			
 //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  			
     			for(ArrayList<Hypothesis> x : final_list_set_hypothesis){
     				for(Hypothesis h : x){
     					if(h.getType().contains("PROPERTY")){
         					ArrayList<String> result= new ArrayList<String>();
         					try {
    							result = utils_new.searchIndex(h.getUri(), 1, myindex);
    							if(!result.isEmpty()){
    								h.setUri(result.get(0));
        							h.setRank(1);
    							}
    							
    							else{
    								h.setRank(0);
    							}
    						} catch (SQLException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
     					}
     				}
     			}
     			
     			template.setHypothesen(final_list_set_hypothesis);
     			//TODO: Take Template like it is and change Condition
     			Template template_reverse_conditions = new Template(template.getCondition(), template.getHaving(), template.getFilter(), template.getSelectTerm(), template.getOrderBy(), template.getLimit());
     			
     			//= template;
     			ArrayList<ArrayList<String>> condition_template_reverse_conditions = template_reverse_conditions.getCondition();
     			ArrayList<ArrayList<String>> condition_reverse_new= new ArrayList<ArrayList<String>>();
     			for (ArrayList<String> x : condition_template_reverse_conditions){
     				ArrayList<String> new_list = new ArrayList<String>();
     				new_list.add(x.get(2));
     				new_list.add(x.get(1));
     				new_list.add(x.get(0));
     				condition_reverse_new.add(new_list);
     			}
     			
     			template_reverse_conditions.setCondition(condition_reverse_new);

     			resultArrayList.add(template);
     			resultArrayList.add(template_reverse_conditions);
     		}
     	}
     	for(Template temp : resultArrayList){
     		temp.printAll();
     	}
		return resultArrayList;
	}
}
