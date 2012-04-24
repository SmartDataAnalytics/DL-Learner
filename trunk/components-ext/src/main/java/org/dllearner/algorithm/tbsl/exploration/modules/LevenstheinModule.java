package org.dllearner.algorithm.tbsl.exploration.modules;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Utils.Levenshtein;

public class LevenstheinModule {
	private final static double LevenstheinMin=0.65;
	
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
			 
			 ArrayList<String> property_array=new ArrayList<String>();
			 property_array.add(property_to_compare_with);
			 if(property_to_compare_with.contains(" ")){
				 
				 String[] array_temp = property_to_compare_with.split(" ");
				 for(String s : array_temp) property_array.add(s);
			 }
			 for(String compare_property :property_array ){
				 
				 double nld=Levenshtein.nld(compare_property.toLowerCase(), key);
				 
				 //if(nld>=LevenstheinMin||key.contains(lemmatiser.stem(property_to_compare_with))||property_to_compare_with.contains(lemmatiser.stem(key))){
				 
				 if(key.contains(compare_property)||compare_property.contains(key)){
					 if(nld<0.8){
						 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", 0.85);
						 listOfNewHypothesen.add(h); 
					 }
					 else{
						 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", nld);
						 listOfNewHypothesen.add(h);
					 }
					 
				 }
				 else if(key.substring(key.length()-1).contains("s")){
						String neuer_string = key.substring(0, key.length() -1);
						if(neuer_string.contains(compare_property)||compare_property.contains(neuer_string)){
							 Hypothesis h = new Hypothesis(variable, neuer_string, value, "PROPERTY", 1.5);
							 listOfNewHypothesen.add(h);
						 }
				 }
				 else if(nld>=LevenstheinMin){
					 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", nld);
					 listOfNewHypothesen.add(h);
				 }
			 }
			 //compare property gotten from the resource with the property from the original query
			 
		     
		 }
		 
		 return listOfNewHypothesen;
	}
	 
}
