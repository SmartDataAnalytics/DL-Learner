package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.dllearner.algorithm.tbsl.exploration.Index.SQLiteIndex;
import org.dllearner.algorithm.tbsl.exploration.Index.Index_utils;
import org.dllearner.algorithm.tbsl.sparql.BasicQueryTemplate;
import org.dllearner.algorithm.tbsl.sparql.Path;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Filter;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Having;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Term;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;

public class TemplateBuilder {

static BasicTemplator btemplator;
private static SQLiteIndex myindex;
	
	
public TemplateBuilder() throws MalformedURLException, ClassNotFoundException, SQLException{
		
		TemplateBuilder.btemplator = new BasicTemplator();
    	//btemplator.UNTAGGED_INPUT = false;
		TemplateBuilder.myindex = new SQLiteIndex();
	}
		
	
	public ArrayList<Template> createTemplates(String question) throws IOException{
		
		long start = System.currentTimeMillis();
		
		ArrayList<Template> resultArrayList = new ArrayList<Template>();
		Set<BasicQueryTemplate> querytemps =null;
		querytemps = btemplator.buildBasicQueries(question);
		
		/*
		 * check if templates were build, if not, safe the question and delete it for next time from the xml file.
		 */
		if(querytemps.contains("could not be parsed") || querytemps.isEmpty()){
			String dateiname="/home/swalter/Dokumente/Auswertung/NotParsed.txt";
			String result_string ="";
			//Open the file for reading
		     try {
		       BufferedReader br = new BufferedReader(new FileReader(dateiname));
		       String thisLine;
			while ((thisLine = br.readLine()) != null) { // while loop begins here
		         result_string+=thisLine+"\n";
		       } // end while 
		     } // end try
		     catch (IOException e) {
		       System.err.println("Error: " + e);
		     }
		     
		     File file = new File(dateiname);
		     BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		        bw.write(result_string+"\n"+question);
		        bw.flush();
		        bw.close();
    
    
		}
		
		long stop_template = System.currentTimeMillis();
     	for (BasicQueryTemplate bqt : querytemps) {
     		ArrayList<ArrayList<String>> condition = new ArrayList<ArrayList<String>>();
     		//ArrayList<ArrayList<Hypothesis>> hypotesen = new ArrayList<ArrayList<Hypothesis>>();
     		String selectTerm = "";
     		String having= "";
     		String filter= "";
     		String OrderBy= "";
     		String limit= "";
     		//String condition_String = "";
     		
     		boolean addTemplate=true;
     		try{
     			for(SPARQL_Term terms :bqt.getSelTerms()) selectTerm=selectTerm+(terms.toString())+" ";
     		}
     		catch (Exception e){
     			selectTerm="";
     			addTemplate=false;
     		}
     		
     		//ArrayList<String> temp_array = new ArrayList<String>();
			try{
     			for(Path conditions1: bqt.getConditions()) {
     				ArrayList<String> temp_array = new ArrayList<String>();
     				String[] tmp_array = conditions1.toString().split(" -- ");
     				for(String s: tmp_array){
     					//System.out.println(s);
     					temp_array.add(s);
     				}
     				condition.add(temp_array);
         			
     			}	
     		}
     		catch (Exception e){
     			//condition_String="";
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
 
     			
     			/*
     			 * SLOT_title: PROPERTY {title,name,label} mitfuehren
     			 */
 //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    			     		
     			Template template = new Template(condition, having, filter, selectTerm,OrderBy, limit,question);
     			//TODO: Iterate over slots
     			ArrayList<Hypothesis> list_of_hypothesis = new ArrayList<Hypothesis>();
     			for(Slot slot : bqt.getSlots()){
     				//System.out.println("Slot: "+slot.toString());
     				if(slot.toString().contains("UNSPEC")){
     					String tmp= slot.toString().replace(" UNSPEC {", "");
     					tmp=tmp.replace("}","");
     					String[] tmp_array = tmp.split(":");
     					Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0],tmp_array[1], tmp_array[1], "UNSPEC", 0);
     					//tmp_hypothesis.printAll();
     					list_of_hypothesis.add(tmp_hypothesis);
     				}
     				if(slot.toString().contains("PROPERTY")){
     					String tmp= slot.toString().replace(" PROPERTY {", "");
     					tmp=tmp.replace("}","");
     					String[] tmp_array = tmp.split(":");
     					Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0], tmp_array[1],tmp_array[1], "PROPERTY", 0);
     					list_of_hypothesis.add(tmp_hypothesis);
     					
     				}
     				if(slot.toString().contains("RESOURCE")){
     					String tmp= slot.toString().replace(" RESOURCE {", "");
     					tmp=tmp.replace("}","");
     					String[] tmp_array = tmp.split(":");
     					Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0],tmp_array[1], tmp_array[1], "RESOURCE", 0);
     					list_of_hypothesis.add(tmp_hypothesis);
     				}
     			}
     			ArrayList<ArrayList<Hypothesis>> final_list_set_hypothesis = new ArrayList<ArrayList<Hypothesis>>();
 //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    			
     			
     			for(Hypothesis x : list_of_hypothesis){
     				if(x.getType().contains("RESOURCE")|| x.getType().contains("UNSPEC") ){
     					ArrayList<String> result= new ArrayList<String>();
     					try {
     						/* here I have to check the hypothesis if I have an isA in my Condition,
							* if so, only look up Yago and OntologyClass.
							*/
							result = Index_utils.searchIndex(x.getUri(), 3, myindex);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
     					for(String s : result){
     						ArrayList<Hypothesis> new_list = new ArrayList<Hypothesis>();
     						
     						//String variable, String uri, String type, float rank
     						for(Hypothesis h : list_of_hypothesis){
     							if (h.getUri().equals(x.getUri())){
     								if(s!=null){
     									Hypothesis new_h = new Hypothesis(h.getVariable(),h.getName(), s, h.getType(), 1);
         								new_list.add(new_h);
     								}
     								else{
     									Hypothesis new_h = new Hypothesis(h.getVariable(),h.getName(), h.getUri(), h.getType(), 1);
         								new_list.add(new_h);
     								}
     								
     							}
     							else{
     								Hypothesis new_h = new Hypothesis(h.getVariable(),h.getName(), h.getUri(), h.getType(), h.getRank());
     								new_list.add(new_h);
     							}
     						}
     						final_list_set_hypothesis.add(new_list);
     					}
     				}
     			}
     			
     			
     			
 //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  
     			
     			/*
 				 * safe lookups for properties, so we dont have to access sql database every time
 				 */
 				HashMap<String,String> hm = new HashMap<String, String>();
 				
     			for(ArrayList<Hypothesis> x : final_list_set_hypothesis){
     				
     				
     				for(Hypothesis h : x){
     					
     					//only if you have a Property or an Unspec, which still has no http:/dbpedia etc
     					if(h.getType().contains("PROPERTY") || (h.getType().contains("UNSPEC")&& !h.getUri().contains("http"))){
         					ArrayList<String> result= new ArrayList<String>();
         					try {
         						if(hm.containsKey(h.getUri().toLowerCase())){
         							result.add(hm.get(h.getUri().toLowerCase()));
         						}
         						else{
         							result = Index_utils.searchIndex(h.getUri(), 1, myindex);
         							if(!result.isEmpty())hm.put(h.getUri().toLowerCase(),result.get(0));
         						}
    							if(!result.isEmpty()){
    								h.setUri(result.get(0));
        							h.setRank(1);
    							}
    							
    							else{
    								String tmp = "http://dbpedia.org/ontology/"+h.getUri().toLowerCase().replace(" ", "_");

    								h.setUri(tmp);
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
     			Template template_reverse_conditions = new Template(template.getCondition(), template.getHaving(), template.getFilter(), template.getSelectTerm(), template.getOrderBy(), template.getLimit(), template.getQuestion());
     			
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
     			
     			long stop = System.currentTimeMillis();
     			template_reverse_conditions.setOverallTime(stop-start);
     			template.setOverallTime(stop-start);
     			
     			template_reverse_conditions.setTime_Templator(stop_template-start);
     			template.setTime_Templator(stop_template-start);
     			
     			template_reverse_conditions.setCondition(condition_reverse_new);
     			template_reverse_conditions.setHypothesen(template.getHypothesen());

     			resultArrayList.add(template);
     			resultArrayList.add(template_reverse_conditions);
     		}
     	}
     	/*for(Template temp : resultArrayList){
     		temp.printAll();
     	}*/
		return resultArrayList;
	}
}
