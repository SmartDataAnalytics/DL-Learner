package org.dllearner.algorithm.tbsl.exploration.modules;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.nlp.StanfordLemmatizer;



public class SemanticRelatenes {
	//private final static double LevenstheinMin=0.65;
	
		public static ArrayList<Hypothesis> doSemanticRelatenes(String variable, String property_to_compare_with, HashMap<String, String> properties)
				throws SQLException {
			ArrayList<Hypothesis> listOfNewHypothesen= new ArrayList<Hypothesis>();

			
			 //iterate over properties
			 for (Entry<String, String> entry : properties.entrySet()) {
				 String key = entry.getKey();
				 key=key.replace("\"","");
				 key=key.replace("@en","");
				 key=key.replace("<th>x</th>","");
				 key=key.toLowerCase();
				 String value = entry.getValue();
				 
				 ArrayList<String> property_array=new ArrayList<String>();
				 property_array.add(property_to_compare_with);
				 if(property_to_compare_with.contains(" ")){
					 
					 String[] array_temp = property_to_compare_with.split(" ");
					 for(String s : array_temp) property_array.add(s);
				 }
				 property_array.add(property_to_compare_with.replace(" ", ""));
				 for(String compare_property :property_array ){
					 
					
					 
					 double score=0;
					try {
						//System.out.println("key: "+key);
						//System.out.println("compare_property: "+compare_property);
						//score = CallSemRelatNess.returnSemRelat(key, compare_property);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("key: "+key);
						System.out.println("compare_property: "+compare_property);
						//e.printStackTrace();
						score=0;
					}
					 if(score>0.5){
						 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", score);
						 listOfNewHypothesen.add(h);
					 }
					 
					 
				 }
				 //compare property gotten from the resource with the property from the original query
				 
			     
			 }

				
			 
			 return listOfNewHypothesen;
		}
		 
	}
