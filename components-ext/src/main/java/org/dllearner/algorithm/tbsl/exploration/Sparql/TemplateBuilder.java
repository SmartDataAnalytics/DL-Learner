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
import org.dllearner.algorithm.tbsl.exploration.Utils.DebugMode;
import org.dllearner.algorithm.tbsl.exploration.exploration_main.Setting;
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
	
	
public TemplateBuilder(BasicTemplator bt, SQLiteIndex sq) throws MalformedURLException, ClassNotFoundException, SQLException{
		
		TemplateBuilder.btemplator = bt;
    	//btemplator.UNTAGGED_INPUT = false;
		TemplateBuilder.myindex = sq;
	}
		
	
	public ArrayList<Template> createTemplates(String question) throws IOException{
		
		long start = System.currentTimeMillis();
		
		ArrayList<Template> resultArrayList = new ArrayList<Template>();
		Set<BasicQueryTemplate> querytemps =null;
		querytemps = btemplator.buildBasicQueries(question);
	
		
		/*
		 * check if templates were build, if not, safe the question and delete it for next time from the xml file.
		 * Only in Debug Mode
		 */
		if(Setting.isDebugModus()){
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
		}
		
		
		long stop_template = System.currentTimeMillis();
		if(Setting.isDebugModus())DebugMode.waitForButton();
     	for (BasicQueryTemplate bqt : querytemps) {
     		
     		long start_part1= System.currentTimeMillis();
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
     					s=s.replace("isA", "ISA");
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
     		
     		long stop_part1= System.currentTimeMillis();
     		
     		if(addTemplate!=false){
     			long start_part2= System.currentTimeMillis();
     			
     			/*
     			 * SLOT_title: PROPERTY {title,name,label} mitfuehren
     			 */
 //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    			     		
     			Template template = new Template(condition,bqt.getQt().toString(), having, filter, selectTerm,OrderBy, limit,question);
     			
     			for(ArrayList<String> al : condition){
     				String con_temp="";
     				for(String s : al){
     					con_temp+=" " + s;
     				}
     				if(Setting.isDebugModus())DebugMode.debugPrint("Condition: "+con_temp);
     			}
     			
     			template.setTime_part1(stop_part1-start_part1);
     			boolean add_reverse_template = true;
     			
     			
     			ArrayList<Hypothesis> list_of_hypothesis = new ArrayList<Hypothesis>();
     			for(Slot slot : bqt.getSlots()){
     				//System.out.println("Slot: "+slot.toString());
     				if(slot.toString().contains("UNSPEC")){
     					String tmp= slot.toString().replace(" UNSPEC {", "");
     					tmp=tmp.replace("}","");
     					String[] tmp_array = tmp.split(":");
     					boolean no_iaA_found=true;
     					for(ArrayList<String> x : condition){
     						if(x.get(1).equals("ISA") && x.get(2).equals("?"+tmp_array[0])){
     							no_iaA_found=false;
     							Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0],tmp_array[1], tmp_array[1], "ISA", 0.0);
     	     					//tmp_hypothesis.printAll();
     	         				list_of_hypothesis.add(tmp_hypothesis);
     	         				
     	         				/*
     	         				 * if you have already found an isA -Class-Pair, you don't have to create the up-side-down, because it will be false
     	         				 */
     	            			add_reverse_template = false;
     						}
     					}
     					
     					if(no_iaA_found){
     						Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0],tmp_array[1], tmp_array[1], "PROPERTY", 0.0);
 							//tmp_hypothesis.printAll();
 							list_of_hypothesis.add(tmp_hypothesis);
     					}
     					
     				}
     				if(slot.toString().contains("PROPERTY")){
     					String tmp= slot.toString().replace(" PROPERTY {", "");
     					tmp=tmp.replace("}","");
     					String[] tmp_array = tmp.split(":");
     					Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0], tmp_array[1],tmp_array[1], "PROPERTY", 0.0);
     					list_of_hypothesis.add(tmp_hypothesis);
     					
     				}
     				if(slot.toString().contains("RESOURCE")){
     					String tmp= slot.toString().replace(" RESOURCE {", "");
     					tmp=tmp.replace("}","");
     					String[] tmp_array = tmp.split(":");
     					Hypothesis tmp_hypothesis = new Hypothesis("?"+tmp_array[0],tmp_array[1], tmp_array[1], "RESOURCE", 0.0);
     					list_of_hypothesis.add(tmp_hypothesis);
     				}
     			}
     			ArrayList<ArrayList<Hypothesis>> final_list_set_hypothesis = new ArrayList<ArrayList<Hypothesis>>();
 //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    	
     			
     			if(Setting.isDebugModus())DebugMode.printHypothesen(list_of_hypothesis,"Alle Hypothesen VOR der Verarbeitung");

     			
     			for(Hypothesis x : list_of_hypothesis){
     				/*
     				 * TODO: Change if ISA only ask classes, else resource
     				 */
     				if(x.getType().contains("RESOURCE")|| x.getType().contains("UNSPEC")|| x.getType().contains("ISA") ){
     					ArrayList<String> result= new ArrayList<String>();
     					try {
     						if(x.getType().contains("ISA")){
     							result = Index_utils.searchIndexForClass(x.getUri(), myindex);
     						}
     						else{
     							result = Index_utils.searchIndexForResource(x.getUri(), myindex);
     						}
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
     									Hypothesis new_h = new Hypothesis(h.getVariable(),h.getName(), s, h.getType(), 1.0);
         								new_list.add(new_h);
         								//new_h.printAll();
     								}
     								else{
     									Hypothesis new_h = new Hypothesis(h.getVariable(),h.getName(), h.getUri(), h.getType(), 1.0);
         								new_list.add(new_h);
         								//new_h.printAll();
     								}
     								
     							}
     							else{
     								Hypothesis new_h = new Hypothesis(h.getVariable(),h.getName(), h.getUri(), h.getType(), h.getRank());
     								new_list.add(new_h);
     								//new_h.printAll();
     							}
     						}
     						final_list_set_hypothesis.add(new_list);
     					}
     				}
     			}
     			
     			
     			if(Setting.isDebugModus())DebugMode.printHypothesenSet(final_list_set_hypothesis,"Alle Hypothesen nach der ERSTEN Verarbeitung");
     			
 //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  
     			
     			/*
 				 * safe lookups for properties, so we dont have to access sql database every time
 				 */
 				HashMap<String,String> hm = new HashMap<String, String>();
 				
     			for(ArrayList<Hypothesis> x : final_list_set_hypothesis){
     				
     				
     				for(Hypothesis h : x){
     					if(h.getType().contains("PROPERTY")){
         					ArrayList<String> result= new ArrayList<String>();
         					try {
         						if(hm.containsKey(h.getUri().toLowerCase())){
         							result.add(hm.get(h.getUri().toLowerCase()));
         						}
         						else{
         							result = Index_utils.searchIndexForProperty(h.getUri(), myindex);
         							if(!result.isEmpty())hm.put(h.getUri().toLowerCase(),result.get(0));
         						}
    							if(!result.isEmpty()){
    								h.setUri(result.get(0));
        							h.setRank(1.0);
    							}
    						} catch (SQLException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
     					}
     				}
     			}
     			
     			
     			
     			
     			/*
     			 * BUGFIX: Before adding Hypothesis to template check, if each Hypothesis has an uri
     			 * TODO: check all functions before
     			 */
     			for(ArrayList<Hypothesis> al:final_list_set_hypothesis){
     				for(Hypothesis h : al){
     					if(!h.getUri().contains("http")){
     						if(h.getType().contains("ISA")){
     							try {
									ArrayList<String> tmp = Index_utils.searchIndexForClass(h.getUri(), myindex);
									System.out.println("Laenge tmp: "+tmp.size());
									if(tmp.size()>0){
										h.setUri(tmp.get(0));
										h.setRank(1.0);
									}
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
     							
     						}
     						if(h.getType().contains("RESOURCE")){
     							try {
									ArrayList<String> tmp = Index_utils.searchIndexForResource(h.getUri(), myindex);
									System.out.println("Laenge tmp: "+tmp.size());
									if(tmp.size()>0){
										h.setUri(tmp.get(0));
										h.setRank(1.0);
									}
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
     							
     						}
     					}
     				}
     			}
     			
     			if(Setting.isDebugModus())DebugMode.printHypothesenSet(final_list_set_hypothesis,"Alle Hypothesen nach der ZWEITEN Verarbeitung");
     			
     			template.setHypothesen(final_list_set_hypothesis);
     			
     			
     			
     			Template template_reverse_conditions = new Template(template.getCondition(),template.getQueryType(), template.getHaving(), template.getFilter(), template.getSelectTerm(), template.getOrderBy(), template.getLimit(), template.getQuestion());
     			
     			ArrayList<ArrayList<String>> condition_template_reverse_conditions = template_reverse_conditions.getCondition();
     			ArrayList<ArrayList<String>> condition_reverse_new= new ArrayList<ArrayList<String>>();
     			if(add_reverse_template){
     				for (ArrayList<String> x : condition_template_reverse_conditions){
         				ArrayList<String> new_list = new ArrayList<String>();
         				if(x.get(1).contains("ISA")){
         					new_list.add(x.get(0));
             				new_list.add(x.get(1));
             				new_list.add(x.get(2));
             				condition_reverse_new.add(new_list);
             				if(condition_template_reverse_conditions.size()>=2) add_reverse_template=true;
         				}
         				else{
         					new_list.add(x.get(2));
             				new_list.add(x.get(1));
             				new_list.add(x.get(0));
             				condition_reverse_new.add(new_list);
         				}
         				
         			}
     			}
     			
     			long stop = System.currentTimeMillis();
     			template_reverse_conditions.setOverallTime(stop-start);
     			template.setOverallTime(stop-start);
     			
     			template_reverse_conditions.setTime_Templator(stop_template-start);
     			template.setTime_Templator(stop_template-start);
     			
     			template_reverse_conditions.setCondition(condition_reverse_new);
     			template_reverse_conditions.setHypothesen(template.getHypothesen());
     			
     			/*
     			 * Before adding Templates, generate for each Template a set of Properties and Elements
     			 */
     			long start_elements = System.currentTimeMillis();
     			Elements elm = new Elements(template.getCondition(),template.getHypothesen());
     			long stop_elements = System.currentTimeMillis();
     			template.setTime_generateElements(stop_elements-start_elements);
     			
     			/*
     			 * If no Elements are created, dont add Template!
     			 */
     			long stop_part2= System.currentTimeMillis();
     			template.setTime_part2(stop_part2-start_part2);
     			if(elm.isElementEmty()==false){
     				//elm.printAll();
     				template.setElm(elm);
         			resultArrayList.add(template);
     			}
     			/*
     			 * Also change the condition, if you have two Conditions in which is one an isa 
     			 */
     			//if(add_reverse_template ||template_reverse_conditions.getCondition().size()>1 ){
     				start_elements = System.currentTimeMillis();
     				Elements elm_reverse = new Elements(template_reverse_conditions.getCondition(),template_reverse_conditions.getHypothesen());
     				stop_elements = System.currentTimeMillis();
     				template_reverse_conditions.setTime_generateElements(stop_elements-start_elements);
     				template_reverse_conditions.setTime_part1(stop_part1-start_part1);
     				template_reverse_conditions.setTime_part2(stop_part2-start_part2);
         			
     				if(elm_reverse.isElementEmty()==false){
     				//elm_reverse.printAll();
     				template_reverse_conditions.setElm(elm_reverse);
         				resultArrayList.add(template_reverse_conditions);
     				}
     			//}
     		}
     	}
     	
     	
     	if(Setting.isDebugModus())DebugMode.printTemplateList(resultArrayList, "Templates nach allen Verarbeitungsschritten");
     	
     	
		return resultArrayList;
	}
}
