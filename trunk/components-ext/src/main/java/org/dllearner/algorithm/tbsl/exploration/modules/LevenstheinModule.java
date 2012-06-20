package org.dllearner.algorithm.tbsl.exploration.modules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Utils.DebugMode;
import org.dllearner.algorithm.tbsl.exploration.Utils.Levenshtein;
import org.dllearner.algorithm.tbsl.exploration.exploration_main.Setting;

public class LevenstheinModule {
	//private final static double LevenstheinMin=0.65;
	
	public static ArrayList<Hypothesis> doLevensthein(String variable, String property_to_compare_with, HashMap<String, String> properties, String resource_name)
			throws SQLException {
		ArrayList<Hypothesis> listOfNewHypothesen= new ArrayList<Hypothesis>();
		
		/*
		 * First Read in propertyFile, then iterate with keys over the entries. But first look, if there is a property for the property_to_compare with and if so, check if the
		 * resulting giving property is in keys. if it is so, set a high rank and close levenstheinMode.
		 * 
		 * File: MostPropertyCombinations
		 */
		
		String dateiname="/home/swalter/workspace/MostPropertyCombinations";
		HashMap<String, String> loaded_properties = new HashMap<String, String>();
		
		boolean found_property_in_loaded_property=false;
		boolean found_property_in_created_property=false;
		
		
	     if(found_property_in_loaded_property==false||found_property_in_created_property==false){
	    	 
	     //}
	     
			 //iterate over properties
			 for (Entry<String, String> entry : properties.entrySet()) {
				 String key = entry.getKey();
				 key=key.replace("\"","");
				 key=key.replace("@en","");
				 key=key.replace("(μ)", "");
				 key=key.replace("(cm)", "");
				 key=key.toLowerCase();
				 String value = entry.getValue();
				// System.out.println("Key: "+key);
				 
				 ArrayList<String> property_array=new ArrayList<String>();
				 property_array.add(property_to_compare_with);
				 if(property_to_compare_with.contains(" ")){
					 
					 String[] array_temp = property_to_compare_with.split(" ");
					 for(String s : array_temp) property_array.add(s);
				 }
				 for(String compare_property :property_array ){
					// System.out.println("compare_property: "+compare_property);
					 double nld=Levenshtein.nld(compare_property.toLowerCase(), key);
					 
					 /*
					  * At the beginning first realy test with the original Property, to make sure, if there is a match, that only this one is taken.
					  */
					 if(key.toLowerCase().equals(property_to_compare_with.toLowerCase())||key.toLowerCase().equals(property_to_compare_with.replace(" ", "").toLowerCase()) ){
						 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", 2.0); 
						 listOfNewHypothesen.add(h);
					 }
					 //else
					 else if((key.contains(compare_property)||compare_property.contains(key))&&key.length()>1){
						 
						 double score=0;
							if(compare_property.length()>key.length()){
								score = 0.8+(key.length()/compare_property.length());
							}
							else{
								score=0.8+(compare_property.length()/key.length());
							}
							
							
						 if(compare_property.length()>4&&key.length()>4) {
							 //0.95
								Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", score); 
								listOfNewHypothesen.add(h);
							}
							else{
								//0.7
								Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", score-0.2); 
								listOfNewHypothesen.add(h);
							}
						 
						 
						// Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", (key.length()/compare_property.length()));
						// listOfNewHypothesen.add(h); 
						 
					 }
					 else if(compare_property.substring(compare_property.length()-2).contains("ed")){
							String compare_property_neu = compare_property.substring(0, compare_property.length() -2);
							if(key.contains(compare_property_neu)||compare_property_neu.contains(key)){
							
								 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", 0.95);
								 listOfNewHypothesen.add(h);
							 }
					 }
					 else if(nld>=Setting.getLevenstheinMin()){
						 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", nld);
						 listOfNewHypothesen.add(h);
					 }
				 }
				 //compare property gotten from the resource with the property from the original query
				 
			     
			 }
	     }
		 
		 return listOfNewHypothesen;
	}
	 
}
