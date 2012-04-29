package org.dllearner.algorithm.tbsl.exploration.modules;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Utils.Levenshtein;
import org.dllearner.algorithm.tbsl.exploration.exploration_main.Setting;

public class LevenstheinModule {
	//private final static double LevenstheinMin=0.65;
	
	public static ArrayList<Hypothesis> doLevensthein(String variable, String property_to_compare_with, HashMap<String, String> properties)
			throws SQLException {
		ArrayList<Hypothesis> listOfNewHypothesen= new ArrayList<Hypothesis>();

		
		 //iterate over properties
		 for (Entry<String, String> entry : properties.entrySet()) {
			 String key = entry.getKey();
			 key=key.replace("\"","");
			 key=key.replace("@en","");
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
				 
				 //if(nld>=LevenstheinMin||key.contains(lemmatiser.stem(property_to_compare_with))||property_to_compare_with.contains(lemmatiser.stem(key))){
				 
				 if((key.contains(compare_property)||compare_property.contains(key))){
					 
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
		 
		 return listOfNewHypothesen;
	}
	 
}
