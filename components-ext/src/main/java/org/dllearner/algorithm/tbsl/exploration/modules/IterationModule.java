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
	 */
	public static ArrayList<ArrayList<Hypothesis>> doIteration(Elements elm,ArrayList<ArrayList<Hypothesis>> givenHypothesenList,ArrayList<ArrayList<String>> givenConditionList){

		for(ArrayList<Hypothesis> hl :givenHypothesenList){
			
			/*
			 * First look for resources and generate List with properties
			 */
			for(Hypothesis h : hl){
				if(h.getType().contains("RESOURCE")){
					/*
					 * Get Variable from Resource
					 */
					String variable = h.getVariable();
					
					/*
					 * Look in Condition for the Set, in which the variable appears
					 */
					ArrayList<String> propertyVariableList = new ArrayList<String>();
					for(ArrayList<String> cl : givenConditionList){
						for(String s : cl){
							if(s.contains(variable)){
								/*
								 * Mark Also if the Property is left or right from the Resource
								 */
								if(s.equals(cl.get(0))) propertyVariableList.add(cl.get(1)+"RIGHT::"+variable);
								else propertyVariableList.add(cl.get(1)+"LEFT::"+variable);
							}
						}
					}
					
					/*
					 * Now get for each found Property the Name
					 */
					ArrayList<String> propertyNameList = new ArrayList<String>();
					for(String s : propertyVariableList){
						for(Hypothesis h_t : hl){
							//System.out.println("s in creating propertyNameList: "+s);
							String variable_t = s;
							variable_t=variable_t.replace("RIGHT", "");
							variable_t=variable_t.replace("LEFT", "");
							if(h_t.getVariable().contains(variable_t)) propertyNameList.add(h_t.getName()+"::"+s);
							
						}
					}
					
					/*
					 * Now find for each Property the list of Propertys of the resource in Elements and compare with Levensthein/Wordnet etc
					 */
					ArrayList<ElementList> resources = elm.getResources();
					for(String s : propertyNameList){
						String[] array = s.split("::");
						
						System.out.println("s: "+s);
						System.out.println("Array:");
						for(String t : array){
							System.out.println(t);
						}
						
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
									resultHypothesenList= LevenstheinModule.doLevensthein(propertyVariable,array[0],el.getHm());
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								for(Hypothesis h_p : resultHypothesenList) h_p.printAll();
								
							}
						}
						
					}
					
				}
			}
			
			
			
			/*
			 * Iterate over all "founded" properties and generate new Hypothesensets, using the "old" resource and isA case
			 */
		}
			
		return null;
		
	}

	
}
