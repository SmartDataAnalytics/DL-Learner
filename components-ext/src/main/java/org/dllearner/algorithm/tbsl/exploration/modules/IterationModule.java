package org.dllearner.algorithm.tbsl.exploration.modules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.didion.jwnl.JWNLException;

import org.dllearner.algorithm.tbsl.exploration.Index.SQLiteIndex;
import org.dllearner.algorithm.tbsl.exploration.Sparql.ElementList;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Elements;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Utils.DebugMode;
import org.dllearner.algorithm.tbsl.exploration.Utils.ServerUtil;
import org.dllearner.algorithm.tbsl.exploration.exploration_main.Setting;
import org.dllearner.algorithm.tbsl.nlp.StanfordLemmatizer;
import org.dllearner.algorithm.tbsl.nlp.WordNet;

/**
 * Gets Elements, Condition and Hypothesen and returns HypothesenSets.
 * Also the different Modules, like Levensthein, Wordnet are used here. 
 * @author swalter
 *
 */
public class IterationModule {

	
	/*
	 * Use Here only one Hypothesen Set at each time, so for each "AusgangshypothesenSet" start this function
	 */
	public static ArrayList<ArrayList<Hypothesis>> new_iteration(Elements elm,ArrayList<Hypothesis> givenHypothesenList,ArrayList<ArrayList<String>> givenConditionList, String type,SQLiteIndex myindex,WordNet wordnet,StanfordLemmatizer lemmatiser) throws SQLException, JWNLException, IOException{
		
		//System.err.println("Startet new_iteration");
		ArrayList<ArrayList<Hypothesis>> finalHypothesenList = new ArrayList<ArrayList<Hypothesis>>();
		
		boolean simple_structure = false;
		
		
		/*	String dateiname="/home/swalter/Dokumente/Auswertung/ConditionsList.txt";
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
		     BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(file));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		      String condition_string="";
		     for(ArrayList<String> cl : givenConditionList){
		    	 condition_string+="[";
					for(String s : cl){
						condition_string+=s+" ";
					}
					condition_string+="]";
				}
		     
		     

		        try {
					bw.write(result_string+condition_string);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        try {
					bw.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		        try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
		
		
		
		
		
		/*for(ArrayList<String> als : givenConditionList){
			for(String s : als) System.err.println(s);
		}*/
		/*try {
			DebugMode.waitForButton();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		if(givenConditionList.size()==1){
			simple_structure=true;
			
			boolean resource_case=false;
			boolean isa_case=false;
			
			for(Hypothesis h : givenHypothesenList){
				/*
				 * if there is an ISA you cant to any thing, except returning HypothesenList to be send to the Server
				 */
				if(h.getType().contains("ISA")){
					isa_case=true;
					finalHypothesenList.add(givenHypothesenList);
				}
				if(h.getType().contains("RESOURCE")){
					/*
					 * Check if Property is left or right of oneself
					 */
					String  case_side = "RIGHT";
						
					ArrayList<String> condition = new ArrayList<String>();
					condition = givenConditionList.get(0);
					if(condition.get(2).contains(h.getVariable())) case_side="LEFT";
					
					ArrayList<ElementList> resources = new ArrayList<ElementList>();
					boolean gotResource=true;
					try{
						resources = elm.getElements();
					}
					catch (Exception e){
						gotResource=false;
						if(Setting.isDebugModus())DebugMode.debugErrorPrint("Didnt get any Resource");
					}
					
					if(gotResource){
						for(ElementList el : resources){
							if(el.getVariablename().contains(h.getName())&&el.getVariablename().contains(case_side)){
								
								String property_name="";
								for(Hypothesis h_t : givenHypothesenList){
									if(h_t.getVariable().contains(condition.get(1))){
										property_name=h_t.getName();
										break;
									}
								}
								ArrayList<Hypothesis> resultHypothesenList=new ArrayList<Hypothesis>();
								/*
								 * Here start levenstehin, wordnet etc etc
								 */
								if(type.contains("LEVENSTHEIN"))resultHypothesenList= LevenstheinModule.doLevensthein(condition.get(1),property_name,el.getHm(),h.getName());
								if(type.contains("WORDNET"))resultHypothesenList= WordnetModule.doWordnet(condition.get(1),property_name,el.getHm(),myindex,wordnet,lemmatiser);
								if(type.contains("RELATE"))resultHypothesenList= SemanticRelatenes.doSemanticRelatenes(condition.get(1),property_name,el.getHm());
								for(Hypothesis h_temp : resultHypothesenList) {
									ArrayList<Hypothesis> temp_al = new ArrayList<Hypothesis>();
									temp_al.add(h);
									temp_al.add(h_temp);
									finalHypothesenList.add(temp_al);
								}
							}
						}
						
					}
					
				}
			}
		
			
			
			return finalHypothesenList;
		}

		
		
		
		
		/*
		 * two conditions!
		 */
		if(givenConditionList.size()==2){
			ArrayList<ElementList> resources = new ArrayList<ElementList>();
			boolean gotResource=true;
			try{
				resources = elm.getElements();
			}
			catch (Exception e){
				gotResource=false;
				if(Setting.isDebugModus())DebugMode.debugErrorPrint("Didnt get any Resource");
			}
			
			
			ArrayList<String> condition1 = givenConditionList.get(0);
			ArrayList<String> condition2 = givenConditionList.get(1);
			
			/*
			 * ISA
			 */
			boolean condition1_exists_isa = false;
			boolean condition2_exists_isa = false;
			if(condition1.get(1).contains("ISA")) condition1_exists_isa=true;
			if(condition2.get(1).contains("ISA")) condition2_exists_isa=true;
			
			
			/*
			 * Resource: Find out the Resource, the Side of the depending Property and mark the Hypothesis for the Resource
			 */
			
			boolean condition1_exists_resource = false;
			boolean condition2_exists_resource = false;
			String resource_variable=null;
			Hypothesis resource_h=null;
			
			
			String property_Side = "RIGHT";
			for(Hypothesis h : givenHypothesenList){
				if(h.getVariable().contains(condition1.get(0))&&h.getType().contains("RESOURCE")){
					condition1_exists_resource=true;
					property_Side="RIGHT";
					resource_variable=h.getVariable();
					resource_h=h;
				}
				if(h.getVariable().contains(condition1.get(2))&&h.getType().contains("RESOURCE")){
					condition1_exists_resource=true;
					property_Side="LEFT";
					resource_variable=h.getVariable();
					resource_h=h;
				}
				
				if(h.getVariable().contains(condition2.get(0))&&h.getType().contains("RESOURCE")){
					condition2_exists_resource=true;
					property_Side="RIGHT";
					resource_variable=h.getVariable();
					resource_h=h;
				}
				if(h.getVariable().contains(condition2.get(2))&&h.getType().contains("RESOURCE")){
					condition2_exists_resource=true;
					property_Side="LEFT";
					resource_variable=h.getVariable();
					resource_h=h;
				}
				
			}
			
			
			/*
			 * for the case: [?y rdf:type Klasse][?y Proptery Resource]
			 * or: [?y rdf:type Klasse][Resource Proptery ?y]
			 * 
			 */
			if((condition1_exists_isa||condition2_exists_isa)&&gotResource&&(condition1_exists_resource||condition2_exists_resource)){
				String class_variable=null;
				String class_property_variable=null;
				ArrayList<String> working_condition=new ArrayList<String>();
				/*
				 * selcet "working_condition"
				 */
				if(condition1_exists_isa){
					class_variable= condition1.get(2);
					class_property_variable=condition1.get(0);
					working_condition= condition2;
				}
				if(condition2_exists_isa){
					class_variable= condition2.get(2);
					class_property_variable=condition2.get(0);
					working_condition= condition1;
				}
				
				Hypothesis class_h=null;
				
				for(Hypothesis h_t : givenHypothesenList){
					if(h_t.getVariable().contains(class_variable)){
						class_h=h_t;
						break;
					}
				}
				
				//System.out.println("class_variable: " + class_variable);
				//System.out.println("Class Hypothese: ");
				
				/*
				 * check now, which side the classVariable is in the other condition
				 * 
				 */
				
				String property_variable_local=null;
				String resource_variable_local=null;
				String side_of_property=null;
				
				if(working_condition.get(0).contains(class_property_variable)){
					property_variable_local=working_condition.get(1);
					resource_variable_local=working_condition.get(2);
					side_of_property="RIGHT";
				}
				else{
					property_variable_local=working_condition.get(1);
					resource_variable_local=working_condition.get(2);
					side_of_property="LEFT";
				}
				
				String property_name=null;
				for(Hypothesis h_t : givenHypothesenList){
					if(h_t.getVariable().contains(property_variable_local)){
						property_name=h_t.getName();
						
					}
				}
				for(ElementList el : resources){
					//System.out.println("el.getVariablename(): "+el.getVariablename());
					if(el.getVariablename().contains(resource_h.getName())&&el.getVariablename().contains(side_of_property)){
						ArrayList<Hypothesis> resultHypothesenList=new ArrayList<Hypothesis>();
						
						resultHypothesenList = creatNewPropertyList(type,
								myindex, wordnet, lemmatiser,
								property_variable_local, property_name, el.getHm(),resource_h.getName());
						for(Hypothesis h_temp : resultHypothesenList) {
							ArrayList<Hypothesis> temp_al = new ArrayList<Hypothesis>();
							temp_al.add(class_h);
							temp_al.add(h_temp);
							temp_al.add(resource_h);
							//System.out.println("Hypothesen:");
							//class_h.printAll();
							//h_temp.printAll();
							finalHypothesenList.add(temp_al);
						}
					}
				}
				
				
			}
				
			
			/*
			 * ISA
			 */
			else if((condition1_exists_isa||condition2_exists_isa)&&gotResource){
				/*
				 * get Hypothese for the Class
				 */
				String class_variable=null;
				if(condition1_exists_isa)class_variable= condition1.get(2);
				if(condition2_exists_isa)class_variable= condition2.get(2);
				
				Hypothesis class_h=null;
				
				for(Hypothesis h_t : givenHypothesenList){
					if(h_t.getVariable().contains(class_variable)){
						class_h=h_t;
						break;
					}
				}
				
				//System.out.println("class_variable: " + class_variable);
				//System.out.println("Class Hypothese: ");
				//class_h.printAll();
				for(ElementList el : resources){
					//System.out.println("el.getVariablename(): "+el.getVariablename());
					if(el.getVariablename().contains(class_h.getName())){
						//System.out.println("In If Abfrage bei der Iteration ueber el");
						String property_name="";
						String property_variable="";
						
						if(condition1_exists_isa)property_variable= condition2.get(1);
						if(condition2_exists_isa)property_variable= condition1.get(1);
						
						//System.out.println("property_variable: " + property_variable);
						
						for(Hypothesis h_t : givenHypothesenList){
							if(h_t.getVariable().contains(property_variable)){
								property_name=h_t.getName();
								break;
							}
						}
						//System.out.println("property_name: " + property_name);
						ArrayList<Hypothesis> resultHypothesenList=new ArrayList<Hypothesis>();
						resultHypothesenList = creatNewPropertyList(type,
								myindex, wordnet, lemmatiser,
								property_variable, property_name, el.getHm(),class_h.getName());
						for(Hypothesis h_temp : resultHypothesenList) {
							ArrayList<Hypothesis> temp_al = new ArrayList<Hypothesis>();
							temp_al.add(class_h);
							temp_al.add(h_temp);
							//System.out.println("Hypothesen:");
							//class_h.printAll();
							//h_temp.printAll();
							finalHypothesenList.add(temp_al);
						}
					}
				}
				
				
/*				try {
					DebugMode.waitForButton();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	*/			
				return finalHypothesenList;
				
			}
			
			
			
			
			/*
			 * Resource
			 */
			
			else if((condition1_exists_resource||condition2_exists_resource)&&gotResource){
				
				String property_name="";
				String second_property_name="";
				String property_variable="";
				String second_property_variable="";
				String property_side_new="LEFT";
				
				if(condition1_exists_resource){
					property_variable= condition2.get(1);
					second_property_variable=condition1.get(1);
					property_side_new="RIGHT";
				}
				if(condition2_exists_resource){
					property_variable= condition1.get(1);
					second_property_variable=condition2.get(1);
					property_side_new="LEFT";
				}
				
				
				for(Hypothesis h_t : givenHypothesenList){
					if(h_t.getVariable().contains(property_variable)){
						property_name=h_t.getName();
						
					}
					if(h_t.getVariable().contains(second_property_variable)){
						second_property_name=h_t.getName();
						
					}
				}
		
				
				if(Setting.isWaitModus())DebugMode.waitForButton();
				
				
				for(ElementList el : resources){
					//System.out.println("el.getVariablename(): "+el.getVariablename());
					if(el.getVariablename().contains(resource_h.getName())&&el.getVariablename().contains(property_Side)){
						//System.out.println("In If Abfrage bei der Iteration ueber el");
						
						
						//System.out.println("property_name: " + property_name);
						ArrayList<Hypothesis> resultHypothesenList=new ArrayList<Hypothesis>();
						resultHypothesenList = creatNewPropertyList(type,
								myindex, wordnet, lemmatiser,
								property_variable, property_name, el.getHm(),resource_h.getName());
						
						for(Hypothesis h_temp : resultHypothesenList) {
							String Query="";
							if(property_side_new.contains("LEFT")){
								//{ [] foaf:name ?name1 } UNION { [] vCard:FN ?name2 }
								Query= "SELECT DISTINCT ?s ?x WHERE {{<"+ resource_h.getUri()+"> <"+h_temp.getUri()+"> ?x} UNION {<"+ resource_h.getUri()+"> <"+h_temp.getUri().replace("property", "ontology")+"> ?x}. ?x rdfs:label ?s. FILTER (lang(?s) = 'en') }";

							}
							else{
								Query= "SELECT DISTINCT ?s ?x WHERE {{?x <"+h_temp.getUri()+"> <"+ resource_h.getUri()+">} UNION {?x <"+h_temp.getUri().replace("property", "ontology")+"> <"+ resource_h.getUri()+">} . ?x rdfs:label ?s. FILTER (lang(?s) = 'en') }";

							}
							/*
							 * Now use the variable from the second condition which does not has an Resource in the Hypothesis
							 */
							HashMap<String, String> hm_newClasses=ServerUtil.generatesQueryForOutsideClasses(Query);
							
							
							ArrayList<Hypothesis> second_resultHypothesenList=new ArrayList<Hypothesis>();
							second_resultHypothesenList = creatNewPropertyList(type,
									myindex, wordnet, lemmatiser, second_property_variable,
									second_property_name,hm_newClasses,resource_h.getName());
							
							
							for(Hypothesis second_h_temp : second_resultHypothesenList) {
								ArrayList<Hypothesis> temp_al = new ArrayList<Hypothesis>();
								temp_al.add(resource_h);
								temp_al.add(h_temp);
								temp_al.add(second_h_temp);
								//resource_h.printAll();
								//h_temp.printAll();
								//second_h_temp.printAll();
								/*
								 * for each hypothesis now get the x from the Server an generate for second condition depending on the x new Hypothesen Set.
								 * afterwars add new_h_temp, h_temp, resource_h to the array Set temp_al and add temp_all to final Hypothesen Set
								 */

								finalHypothesenList.add(temp_al);
							}
							
						}
					}
				}
					
				return finalHypothesenList;
			}
			
			
		}
		
		/*
		 * 3Conditions!!
		 * 
		 * PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x0  WHERE {?x0 <http://dbpedia.org/property/cave> ?y0 .?x0 <http://dbpedia.org/ontology/place> ?y .?x0 rdf:type <http://dbpedia.org/ontology/Country> . } HAVING (COUNT(?y0) > 2)    1.0
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x0  WHERE {?y0 <http://dbpedia.org/property/cave> ?x0 .?y <http://dbpedia.org/ontology/place> ?x0 .?x0 rdf:type <http://dbpedia.org/ontology/Country> . } HAVING (COUNT(?y0) > 2)    1.0
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x0  WHERE {?x0 <http://dbpedia.org/property/cave> ?y0 .?x0 <http://dbpedia.org/ontology/place> ?y .?x0 rdf:type <http://dbpedia.org/ontology/Country> . FILTER(?y0 > 2) }    1.0
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x0  WHERE {?y0 <http://dbpedia.org/property/cave> ?x0 .?y <http://dbpedia.org/ontology/place> ?x0 .?x0 rdf:type <http://dbpedia.org/ontology/Country> . FILTER(?y0 > 2) }    1.0
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x  WHERE {?y <http://dbpedia.org/property/cave> ?y0 .?x rdf:type <http://dbpedia.org/ontology/Country> .?x <http://dbpedia.org/ontology/place> ?y . FILTER(?y0 > 2) }    1.0
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x  WHERE {?y0 <http://dbpedia.org/property/cave> ?y .?x rdf:type <http://dbpedia.org/ontology/Country> .?y <http://dbpedia.org/ontology/place> ?x . FILTER(?y0 > 2) }    1.0
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x  WHERE {?y <http://dbpedia.org/property/cave> ?y0 .?x rdf:type <http://dbpedia.org/ontology/Country> .?x <http://dbpedia.org/ontology/place> ?y . } HAVING (COUNT(?y0) > 2)    1.0
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?x  WHERE {?y0 <http://dbpedia.org/property/cave> ?y .?x rdf:type <http://dbpedia.org/ontology/Country> .?y <http://dbpedia.org/ontology/place> ?x . } HAVING (COUNT(?y0) > 2)

[[?y1, ISA, ?y177], [?y2, ?p76, ?y], [?y2, ?p75, ?y0]] one clase, one resource depending on two properties
[[?y1, ISA, ?y177], [?y, ?p76, ?y2], [?y0, ?p75, ?y2]]
		 */
		if(givenConditionList.size()==3){
			boolean gotResource=true;
			ArrayList<ElementList> resources = new ArrayList<ElementList>();
			try{
				resources = elm.getElements();
			}
			catch (Exception e){
				gotResource=false;
				if(Setting.isDebugModus())DebugMode.debugErrorPrint("Didnt get any Resource");
			}
			
			/*
			 * First one Class and two Properties
			 */
			
			boolean class_condition1=false;
			boolean class_condition2=false;
			boolean class_condition3=false;
			ArrayList<String> condition1=givenConditionList.get(0);
			ArrayList<String> condition2=givenConditionList.get(1);
			ArrayList<String> condition3=givenConditionList.get(2);
			
			String class_variable=null;
			String property_variable1=null;
			String property_variable2=null;
			String class_name=null;
			String property_name1=null;
			String property_name2=null;
			
			Hypothesis class_hypothesis=null;
			Hypothesis proptery1_hypothesis=null;
			Hypothesis property2_hypothesis=null;
			
			if(condition1.get(1).contains("ISA")){
				class_condition1=true;
				class_variable=condition1.get(0);
				property_variable1=condition2.get(1);
				property_variable2=condition3.get(1);
			}
			if(condition2.get(1).contains("ISA")){
				class_condition2=true;
				class_variable=condition2.get(0);
				property_variable1=condition1.get(1);
				property_variable2=condition3.get(1);
			}
			if(condition3.get(1).contains("ISA")) {
				class_condition3=true;
				class_variable=condition3.get(0);
				property_variable1=condition1.get(1);
				property_variable2=condition2.get(1);
			}
			
			
			for(Hypothesis h : givenHypothesenList){
				if(h.getVariable().contains(class_variable)&&h.getType().contains("ISA")){
					class_hypothesis=h;
				}
				if(h.getVariable().contains(property_variable1)&&h.getType().contains("PROPERTY")){
					proptery1_hypothesis=h;
				}
				if(h.getVariable().contains(property_variable2)&&h.getType().contains("PROPERTY")){
					property2_hypothesis=h;
				}
			}
			
		
		/*
		 * get a list with properties for property one
		 */
		ArrayList<Hypothesis> resultHypothesenListPropertyOne=new ArrayList<Hypothesis>();
		for(ElementList el : resources){
			//System.out.println("el.getVariablename(): "+el.getVariablename());
			if(el.getVariablename().contains(class_hypothesis.getName())){
				
				property_name1=proptery1_hypothesis.getName();
				resultHypothesenListPropertyOne = creatNewPropertyList(type,
						myindex, wordnet, lemmatiser, property_variable1,
						property_name1, el.getHm(),class_hypothesis.getName());
			}
		}
	
		
		
		/*
		 * get a list with properties for property two
		 */
		ArrayList<Hypothesis> resultHypothesenListPropertyTwo=new ArrayList<Hypothesis>();
		for(ElementList el : resources){
			//System.out.println("el.getVariablename(): "+el.getVariablename());
			if(el.getVariablename().contains(class_hypothesis.getName())){
				
				property_name1=property2_hypothesis.getName();
				resultHypothesenListPropertyTwo = creatNewPropertyList(type,
						myindex, wordnet, lemmatiser, property_variable2,
						property_name2, el.getHm(),class_hypothesis.getName());
			}
		}
		
		if(resultHypothesenListPropertyTwo.size()>0 &&resultHypothesenListPropertyOne.size()>0){
			for(Hypothesis h_1 : resultHypothesenListPropertyOne){
				for(Hypothesis h_2 : resultHypothesenListPropertyTwo){
					ArrayList<Hypothesis> temp_al = new ArrayList<Hypothesis>();
					temp_al.add(h_1);
					temp_al.add(h_2);
					temp_al.add(class_hypothesis);

					finalHypothesenList.add(temp_al);
				}
			}
		}
		
		/*
		 * create new HypothesisSet combining properties from one and two with the class hypothesis.
		 */
		
		
	}
		
		return finalHypothesenList;
		
		
	}


	private static ArrayList<Hypothesis> creatNewPropertyList(String type,
			SQLiteIndex myindex, WordNet wordnet,
			StanfordLemmatizer lemmatiser, String property_variable,
			String property_name,
			HashMap<String, String> hm, String resourceName) throws SQLException, JWNLException {
		/*
		 * Here start levenstehin, wordnet etc etc
		 */
		ArrayList<Hypothesis> resultHypothesenList = new ArrayList<Hypothesis>();
		if(type.contains("LEVENSTHEIN"))resultHypothesenList= LevenstheinModule.doLevensthein(property_variable,property_name,hm,resourceName);
		if(type.contains("RELATE"))resultHypothesenList= SemanticRelatenes.doSemanticRelatenes(property_variable,property_name,hm);

		if(type.contains("WORDNET"))resultHypothesenList= WordnetModule.doWordnet(property_variable,property_name,hm,myindex,wordnet,lemmatiser);
		//System.out.println("After generating new Hypothesen.\n "+resultHypothesenList.size()+" new were generated");
		return resultHypothesenList;
	}

	
}
