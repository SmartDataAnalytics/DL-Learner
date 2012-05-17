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
		if(Setting.isLoadedProperties()){
			System.out.println("in Loaded!!");
			//Open the file for reading
		     try {
		       BufferedReader br = new BufferedReader(new FileReader(dateiname));
		       String thisLine;
			while ((thisLine = br.readLine()) != null) { // while loop begins here
		         String[] tmp_array = thisLine.split("::");
		         if(tmp_array.length>1){
		        	 loaded_properties.put(tmp_array[0].replace("\n", ""), tmp_array[1].replace("\n", ""));
		         }
		         
		       } // end while 
		     } // end try
		     catch (IOException e) {
		       System.err.println("Error: " + e);
		     }
		     for (Entry<String, String> entry_loaded : loaded_properties.entrySet()) {
		    	 
		    	 String key_loaded= entry_loaded.getKey();
		    	 String value_loaded= entry_loaded.getValue();
		    	 if(key_loaded.replace("_", " ").equals(property_to_compare_with.replace("_", " ").toLowerCase())){
		    		 System.out.println("FOUND!!");
		    		 for (Entry<String, String> entry : properties.entrySet()) {
						 String key = entry.getKey();
						 key=key.replace("\"","");
						 key=key.replace("@en","");
						 key=key.replace("(μ)", "");
						 key=key.replace("(cm)", "");
						 key=key.toLowerCase();
						 String value = entry.getValue();
						 /*System.out.println("KEY_old:"+key);
						 System.out.println("value_loaded:"+value_loaded+"DONE");
						 System.out.println("Value:"+value);
						 System.out.println("\n");*/
						 if(key.equals(value_loaded)){
							 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", 2.0); 
							 listOfNewHypothesen.add(h); 
							 h.printAll();
							 found_property_in_loaded_property=true;
						 }
				     }
		    	 }
		    	 
		     }
		}
		
		/*
		 * createdPropertyCombinations
		 */
	
		/*
		 * Second read in File with the propertys, which were created, when a query produced an answer and query had the rank >0.8
		 * and the property hast the given "matserresource"
		 */
		
	/*	if(found_property_in_loaded_property==false&&Setting.isSaveAnsweredQueries()){
			HashMap<String, String> created_properties = new HashMap<String, String>();
			System.out.println("in Save!!");
			dateiname="/home/swalter/workspace/createdPropertyCombinations";
			System.out.println("Resource to compare: "+resource_name);
			//Open the file for reading
		     try {
		       BufferedReader br = new BufferedReader(new FileReader(dateiname));
		       String thisLine;
			while ((thisLine = br.readLine()) != null) { // while loop begins here
		         String[] tmp_array = thisLine.split("::");
		         if(tmp_array.length>2){
		        	 
		        	 // check, if the given resource is equal to the reosource loaded from the file!
		        	  
		        	 if(tmp_array[0].toLowerCase().replace("\n", "").equals(resource_name.toLowerCase())){
		        		 created_properties.put(tmp_array[1].replace("\n", ""), tmp_array[2].replace("\n", ""));
		        	 }
		        	 
		         }
		         
		       } // end while 
		     } // end try
		     catch (IOException e) {
		       System.err.println("Error: " + e);
		     }
		     if(!created_properties.isEmpty()){
		    	 for (Entry<String, String> entry_loaded : created_properties.entrySet()) {
			    	 
			    	 String key_loaded= entry_loaded.getKey();
			    	 String value_loaded= entry_loaded.getValue();
			    	 if(key_loaded.replace("_", " ").equals(property_to_compare_with.replace("_", " ").toLowerCase())){
			    		 System.out.println("FOUND!!");
			    		 for (Entry<String, String> entry : properties.entrySet()) {
							 String key = entry.getKey();
							 key=key.replace("\"","");
							 key=key.replace("@en","");
							 key=key.replace("(μ)", "");
							 key=key.replace("(cm)", "");
							 key=key.toLowerCase();
							 String value = entry.getValue();
							 System.out.println("KEY_old:"+key);
							 System.out.println("value_loaded:"+value_loaded+"DONE");
							 System.out.println("Value:"+value);
							 System.out.println("\n");
							 if(key.equals(value_loaded)){
								 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", 2.0); 
								 listOfNewHypothesen.add(h); 
								 h.printAll();
								 found_property_in_created_property=true;
							 }
					     }
			    	 }
			    	 
			     }
		     }
		     
			
		}*/
		
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
							System.out.println("NEW compare_property: "+compare_property_neu);
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
