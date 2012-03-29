package org.dllearner.algorithm.tbsl.exploration.modules;

import java.sql.SQLException;
import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.exploration.Sparql.ElementList;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Elements;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;

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
	public static ArrayList<ArrayList<Hypothesis>> doIteration(Elements elm,ArrayList<ArrayList<Hypothesis>> givenHypothesenList,ArrayList<ArrayList<String>> givenConditionList, String type) throws SQLException{

		boolean gotResource=true;
		ArrayList<ElementList> resources = new ArrayList<ElementList>();
		try{
			resources = elm.getElements();
		}
		catch (Exception e){
			gotResource=false;
			System.out.println("Didnt get any Resource");
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
					System.out.println("Found Resource");
					
					
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
							
							System.out.println("String: "+s);
							/*System.out.println("Array:");
							for(String t : array){
								System.out.println(t);
							}*/
							
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
										if(!PL.contains(propertyVariable)) PL.add(propertyVariable+"::"+h.getVariable());
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									for(Hypothesis h_p : resultHypothesenList)HL.add(h_p);
									
								}
							}
							
						}
					}
					
					
				}
				
				if(h.getType().contains("isA")){
					/*
					 * TODO:Add special case, if we have only one condition but with an isA in it. 
					 */
					RL.add(h);
					System.out.println("Found Class");
					
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
								if(!cl.get(1).contains("isA")){
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

	
}
