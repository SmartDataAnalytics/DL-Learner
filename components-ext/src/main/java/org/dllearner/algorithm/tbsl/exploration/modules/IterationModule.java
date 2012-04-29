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
	
	/**
	 * returns ArrayList<ArrayList<Hypothesis>> which than can be added using
	 * @param elm
	 * @param hypothesen
	 * @param condition
	 * @return ArrayList<ArrayList<Hypothesis>>
	 * @throws SQLException 
	 */
	public static ArrayList<ArrayList<Hypothesis>> doIteration(Elements elm,ArrayList<ArrayList<Hypothesis>> givenHypothesenList,ArrayList<ArrayList<String>> givenConditionList, String type,SQLiteIndex myindex,WordNet wordnet,StanfordLemmatizer lemmatiser) throws SQLException{

		
		
		
		boolean gotResource=true;
		ArrayList<ElementList> resources = new ArrayList<ElementList>();
		try{
			resources = elm.getElements();
		}
		catch (Exception e){
			gotResource=false;
			if(Setting.isDebugModus())DebugMode.debugErrorPrint("Didnt get any Resource");
		}
		
		//System.out.println("Anzahl der Resource Listen: "+resources.size());
		
		ArrayList<ArrayList<Hypothesis>>finalHypothesenList = new ArrayList<ArrayList<Hypothesis>>(); 
		
		
		for(ArrayList<Hypothesis> hl :givenHypothesenList){
			/*
			 * foundedResourcesClasses
			 */
			ArrayList<Hypothesis> RL = new ArrayList<Hypothesis>();
			
			/*
			 * foundedProperty 
			 */
			ArrayList<String> PL = new ArrayList<String>();
			
			/*
			 * hypothesenListBeforSorting
			 */
			ArrayList<Hypothesis>HL = new ArrayList<Hypothesis>(); 
			
			
			/*
			 * AL abhängig von jeder einzelnen Resource R aus RL
			 */
			ArrayList<ArrayList<ArrayList<Hypothesis>>> ALR = new ArrayList<ArrayList<ArrayList<Hypothesis>>>();
			
			
			/*
			 * First look for resources and generate List with properties
			 */
			for(Hypothesis h : hl){
				
				//System.out.println("In Hypothesis Loop");
				if(h.getType().contains("RESOURCE")){
					/*
					 * Get Variable from Resource
					 */
					String variable = h.getVariable();
					String name = h.getName();
					RL.add(h);
					if(Setting.isDebugModus())DebugMode.debugPrint("Found Resource "+h.getName() +" "+h.getUri());
					
					
					/*
					 * Look in Condition for the Set, in which the variable appears
					 */
					ArrayList<String> propertyVariableList = new ArrayList<String>();
					for(ArrayList<String> cl : givenConditionList){
						for(String s : cl){
							if(s.contains(variable)){
								if(s.equals(cl.get(0))) propertyVariableList.add(cl.get(1)+"RIGHT::"+name);
								else propertyVariableList.add(cl.get(1)+"LEFT::"+name);
							}
						}
					}
					
					/*
					 * Now get for each found Property the Name
					 */
					ArrayList<String> propertyNameList = new ArrayList<String>();
					for(String s : propertyVariableList){
						for(Hypothesis h_t : hl){
							String variable_t = s;
							variable_t=variable_t.replace("RIGHT", "");
							variable_t=variable_t.replace("LEFT", "");
							String[] variable_t1=variable_t.split("::");
							if(h_t.getVariable().contains(variable_t1[0])){
								propertyNameList.add(h_t.getName()+"::"+s);
							}
							
						}
					}
					/*
					 * Now find for each Property the list of Propertys of the resource in Elements and compare with Levensthein/Wordnet etc
					 */
					
					if(gotResource){
						for(String s : propertyNameList){
							String[] array = s.split("::");
							
							
							/*
							 * array[0] contains name of Property
							 * array[1] contains LEFT/RIGHT and Variable of Property
							 * array[2] contains Name of Resource
							 */
							
							String side="LEFT";
							if(array[1].contains("RIGHT")) side="RIGHT";
							
							for(ElementList el : resources){
								if(el.getVariablename().contains(array[2]) && el.getVariablename().contains(side)){
									String propertyVariable = array[1];
									propertyVariable=propertyVariable.replace("LEFT", "");
									propertyVariable=propertyVariable.replace("RIGHT", "");
									ArrayList<Hypothesis> resultHypothesenList = new ArrayList<Hypothesis>();
									try {
										/*
										 * Here start levenstehin, wordnet etc etc
										 */
										if(type.contains("LEVENSTHEIN"))resultHypothesenList= LevenstheinModule.doLevensthein(propertyVariable,array[0],el.getHm());
										if(type.contains("RELATE"))resultHypothesenList= SemanticRelatenes.doSemanticRelatenes(propertyVariable,array[0],el.getHm());
										if(type.contains("WORDNET"))resultHypothesenList= WordnetModule.doWordnet(propertyVariable,array[0],el.getHm(),myindex,wordnet,lemmatiser);
										if(!PL.contains(propertyVariable)) PL.add(propertyVariable+"::"+h.getVariable());
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (JWNLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									for(Hypothesis h_p : resultHypothesenList)HL.add(h_p);
									
								}
							}
							
						}
					}
					
					
				}
				
				if(h.getType().contains("ISA")){
					/*
					 * TODO:Add special case, if we have only one condition but with an isA in it. 
					 */
					RL.add(h);
					if(Setting.isDebugModus())DebugMode.debugPrint("Found Class "+h.getName() +" "+h.getUri());
					
					for(ElementList el:resources){
						/*
						 * Find the resource with the same uri as in h an then start Levensthein/Wordnet etc
						 */
						if(el.getURI().contains(h.getUri())){
							/*
							 * Iterate over condition and find the coressesponding variable of the Class, wich is used for the new Hypothesis
							 */
							ArrayList<String> propertyVariableList= new ArrayList<String>();
							for(ArrayList<String> cl : givenConditionList){
								/*
								 * Dont look for line, where e.g. ?x isA ?y
								 */
								//get(1) is the middle Term and if there is an isa, than its there
								if(!cl.get(1).contains("ISA")){
									for(String s : cl){
										if(s.contains(h.getVariable())){
											propertyVariableList.add(s);
										}
									}
								}
							
							}
							for(String propertyVariable : propertyVariableList){
								for(ArrayList<Hypothesis> hl_small :givenHypothesenList){
									for(Hypothesis h_small : hl_small){
										if(h_small.getVariable().contains(propertyVariable)){
											try {
												ArrayList<Hypothesis> resultHypothesenList=new ArrayList<Hypothesis>();
												/*
												 * Here start levenstehin, wordnet etc etc
												 */
												if(type.contains("LEVENSTHEIN"))resultHypothesenList= LevenstheinModule.doLevensthein(propertyVariable,h_small.getName(),el.getHm());
												if(type.contains("RELATE"))resultHypothesenList= SemanticRelatenes.doSemanticRelatenes(propertyVariable,h_small.getName(),el.getHm());
												if(type.contains("WORDNET"))resultHypothesenList= WordnetModule.doWordnet(propertyVariable,h_small.getName(),el.getHm(),myindex,wordnet,lemmatiser);
												if(!PL.contains(propertyVariable)) PL.add(propertyVariable);
												for(Hypothesis h_temp : resultHypothesenList) HL.add(h_temp);
											} catch (Exception e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}
								}
								
							}
							//for(Hypothesis h_p : finalHypothesenList) h_p.printAll();
							
						
						}
					}
					
					
					
				}
				
				
			}
			
			
			/*
			 * Iterate over all "founded" properties and generate new Hypothesensets, using the "old" resource and isA case
			 */
			
			/*for(Hypothesis h : RL){
				h.printAll();
			}
			
			for(String s : PL){
				System.out.println("Variable P: "+s);
			}*/
			
			/*
			 * Here add new function!
			 */
			for(Hypothesis R : RL){
				/*
				 * AL, abhängig von jeder einzelnen P aus PL und R aus RL
				 */
				ArrayList<ArrayList<Hypothesis>> AL = new ArrayList<ArrayList<Hypothesis>>();
				
				
				for(String P : PL){
					if(P.contains(R.getVariable())){
						for(Hypothesis H : HL){
							if(P.contains(H.getVariable())){
								ArrayList<Hypothesis> t_h_l = new ArrayList<Hypothesis>();
								t_h_l.add(H);
								t_h_l.add(R);
								AL.add(t_h_l);
							}
						}
					}
				}
				
				ALR.add(AL);
			}
			
			if(ALR.size()==1){
				System.out.println("ONLY One Element in ALR");
				finalHypothesenList=ALR.get(0);
				System.out.println("One Element in ALR added to finalHypothesenList");
			}
			if(ALR.size()==2){
				System.out.println("Two Elements in ALR");
				for(ArrayList<Hypothesis> hl_t : ALR.get(0) ){
					for(ArrayList<Hypothesis> hl1_t : ALR.get(1) ){
						ArrayList<Hypothesis> al_t = new ArrayList<Hypothesis>();
						for(Hypothesis h_t : hl_t) al_t.add(h_t);
						for(Hypothesis h_t : hl1_t) al_t.add(h_t);
						finalHypothesenList.add(al_t);
					}
				}
				System.out.println("Two Element in ALR added to finalHypothesenList");

			}
			
			if(ALR.size()==2){
				System.out.println("Three Elements in ALR");
				for(ArrayList<Hypothesis> hl_t : ALR.get(0) ){
					for(ArrayList<Hypothesis> hl1_t : ALR.get(1) ){
						for(ArrayList<Hypothesis> hl2_t : ALR.get(2)){
							ArrayList<Hypothesis> al_t = new ArrayList<Hypothesis>();
							for(Hypothesis h_t : hl_t) al_t.add(h_t);
							for(Hypothesis h_t : hl1_t) al_t.add(h_t);
							for(Hypothesis h_t : hl2_t) al_t.add(h_t);
							finalHypothesenList.add(al_t);	
						}

					}
				}
				System.out.println("Three Element in ALR added to finalHypothesenList");

			}
			
			
		}
		

		
		System.out.println("######################DONE######################");
			
		return finalHypothesenList;
		
	}
	
	
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
			System.err.println("Only one Condition => simple Struktur");
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
								if(type.contains("LEVENSTHEIN"))resultHypothesenList= LevenstheinModule.doLevensthein(condition.get(1),property_name,el.getHm());
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
			System.out.println("two Conditions => NOT simple Struktur");
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
				
				System.out.println("class_variable: " + class_variable);
				System.out.println("Class Hypothese: ");
				
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
						
						if(type.contains("LEVENSTHEIN"))resultHypothesenList= LevenstheinModule.doLevensthein(property_variable_local,property_name,el.getHm());
						if(type.contains("RELATE"))resultHypothesenList= SemanticRelatenes.doSemanticRelatenes(property_variable_local,property_name,el.getHm());

						if(type.contains("WORDNET"))resultHypothesenList= WordnetModule.doWordnet(property_variable_local,property_name,el.getHm(),myindex,wordnet,lemmatiser);
						System.out.println("After generating new Hypothesen.\n "+resultHypothesenList.size()+" new were generated");
						for(Hypothesis h_temp : resultHypothesenList) {
							ArrayList<Hypothesis> temp_al = new ArrayList<Hypothesis>();
							temp_al.add(class_h);
							temp_al.add(h_temp);
							temp_al.add(resource_h);
							System.out.println("Hypothesen:");
							class_h.printAll();
							h_temp.printAll();
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
				
				System.out.println("class_variable: " + class_variable);
				System.out.println("Class Hypothese: ");
				class_h.printAll();
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
						/*for (Entry<String, String> entry : el.getHm().entrySet()) {
							System.out.println(entry.getKey()+" "+entry.getValue());
						}*/
						/*
						 * Here start levenstehin, wordnet etc etc
						 */
						if(type.contains("LEVENSTHEIN"))resultHypothesenList= LevenstheinModule.doLevensthein(property_variable,property_name,el.getHm());
						if(type.contains("RELATE"))resultHypothesenList= SemanticRelatenes.doSemanticRelatenes(property_variable,property_name,el.getHm());

						if(type.contains("WORDNET"))resultHypothesenList= WordnetModule.doWordnet(property_variable,property_name,el.getHm(),myindex,wordnet,lemmatiser);
						System.out.println("After generating new Hypothesen.\n "+resultHypothesenList.size()+" new were generated");
						for(Hypothesis h_temp : resultHypothesenList) {
							ArrayList<Hypothesis> temp_al = new ArrayList<Hypothesis>();
							temp_al.add(class_h);
							temp_al.add(h_temp);
							System.out.println("Hypothesen:");
							class_h.printAll();
							h_temp.printAll();
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
				
				System.out.println("IN RESOURCE NOT SIMPLE CASE!!!");
				System.out.println("resource_variable: " + resource_variable);
				System.out.println("Resource Hypothese: ");
				resource_h.printAll();
				
				String property_name="";
				String second_property_name="";
				String property_variable="";
				String second_property_variable="";
				
				if(condition1_exists_resource){
					//property_variable= condition1.get(1);
					//second_property_variable=condition2.get(1);
					property_variable= condition2.get(1);
					second_property_variable=condition1.get(1);
				}
				if(condition2_exists_resource){
					//property_variable= condition2.get(1);
					//second_property_variable=condition1.get(1);
					property_variable= condition1.get(1);
					second_property_variable=condition2.get(1);
				}
				
				System.out.println("property_variable: " + property_variable);
				System.out.println("scond_property_variable: " + second_property_variable);
				for(ArrayList<String> al : givenConditionList){
					for(String s : al) System.out.println(s);
				}
				for(Hypothesis h : givenHypothesenList){
					h.printAll();
				}
				
				for(Hypothesis h_t : givenHypothesenList){
					if(h_t.getVariable().contains(property_variable)){
						property_name=h_t.getName();
						
					}
					if(h_t.getVariable().contains(second_property_variable)){
						second_property_name=h_t.getName();
						
					}
				}
				System.out.println("property_name: " + property_name);
				System.out.println("second_property_name: " + second_property_name);
				
				if(Setting.isWaitModus())DebugMode.waitForButton();
				
				
				for(ElementList el : resources){
					//System.out.println("el.getVariablename(): "+el.getVariablename());
					if(el.getVariablename().contains(resource_h.getName())&&el.getVariablename().contains(property_Side)){
						//System.out.println("In If Abfrage bei der Iteration ueber el");
						
						
						//System.out.println("property_name: " + property_name);
						ArrayList<Hypothesis> resultHypothesenList=new ArrayList<Hypothesis>();
						/*for (Entry<String, String> entry : el.getHm().entrySet()) {
							System.out.println(entry.getKey()+" "+entry.getValue());
						}
						
						if(Setting.isWaitModus())
							try {
								DebugMode.waitForButton();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
						/*
						 * Here start levenstehin, wordnet etc etc
						 */
						if(type.contains("LEVENSTHEIN"))resultHypothesenList= LevenstheinModule.doLevensthein(property_variable,property_name,el.getHm());
						if(type.contains("RELATE"))resultHypothesenList= SemanticRelatenes.doSemanticRelatenes(property_variable,property_name,el.getHm());

						if(type.contains("WORDNET"))resultHypothesenList= WordnetModule.doWordnet(property_variable,property_name,el.getHm(),myindex,wordnet,lemmatiser);
						System.out.println("After generating new Hypothesen.\n "+resultHypothesenList.size()+" new were generated");
						for(Hypothesis h_temp : resultHypothesenList) {
							String Query="";
							if(property_Side.contains("LEFT")){
								Query= "SELECT DISTINCT ?s ?x WHERE {<"+ resource_h.getUri()+"> <"+h_temp.getUri()+"> ?x. ?x rdfs:label ?s. FILTER (lang(?s) = 'en') }";

							}
							else{
								Query= "SELECT DISTINCT ?s ?x WHERE {?x <"+h_temp.getUri()+"> <"+ resource_h.getUri()+"> . ?x rdfs:label ?s. FILTER (lang(?s) = 'en') }";

							}
							/*
							 * Now use the variable from the second condition which does not has an Resource in the Hypothesis
							 */
							System.out.println("Query: "+Query);
							HashMap<String, String> hm_newClasses=ServerUtil.generatesQueryForOutsideClasses(Query);
							
							
							ArrayList<Hypothesis> second_resultHypothesenList=new ArrayList<Hypothesis>();
							
							
							
							if(type.contains("LEVENSTHEIN"))second_resultHypothesenList= LevenstheinModule.doLevensthein(second_property_variable,second_property_name,hm_newClasses);
							if(type.contains("RELATE"))second_resultHypothesenList= SemanticRelatenes.doSemanticRelatenes(second_property_variable,second_property_name,hm_newClasses);

							if(type.contains("WORDNET"))second_resultHypothesenList= WordnetModule.doWordnet(second_property_variable,second_property_name,hm_newClasses,myindex,wordnet,lemmatiser);
							System.out.println("SIze of second_resultHypothesenList: "+second_resultHypothesenList.size());
							
							for(Hypothesis second_h_temp : second_resultHypothesenList) {
								ArrayList<Hypothesis> temp_al = new ArrayList<Hypothesis>();
								temp_al.add(resource_h);
								temp_al.add(h_temp);
								temp_al.add(second_h_temp);
								resource_h.printAll();
								h_temp.printAll();
								second_h_temp.printAll();
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
		
		return finalHypothesenList;
		
		
	}

	
}
