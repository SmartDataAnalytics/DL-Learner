package org.dllearner.kb.old;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.utilities.datastructures.StringTuple;

public class DBpediaNavigatorCityLocator {
	
	public static String getTypeToCoordinates(float lat, float lng){
		if (lat<71.08&&lat>33.39&&lng>-24.01&&lng<50.8){
			if (lat>50&&lat<52&&lng>12&&lng<13){
				return "http://dbpedia.org/class/custom/City_in_Saxony";
			}
			else return "http://dbpedia.org/class/custom/City_in_Europe";
		}
		else if (lng>-17.5&&lng<52.04&&lat>-36&&lat<36.6){
			if (lat>21.45&&lat<31.51&&lng>24.7&&lng<37.26){
				return "http://dbpedia.org/class/custom/City_in_Egypt";
			}
			else return "http://dbpedia.org/class/custom/City_in_Africa";
		}
		else if (((lng>27.4&&lng<180)||(lng<-168.75))&&lat>-11.2){
			return "http://dbpedia.org/class/custom/City_in_Asia";
		}
		else if (lng>113.9&&lng<179.65&&lat<-10.8&&lat>-47.04){
			return "http://dbpedia.org/class/custom/City_in_Australia";
		}
		else if (lng>-168.4&&lng<-19.7&&lat>6.6){
			return "http://dbpedia.org/class/custom/City_in_North_America";
		}
		else if (lng>-81.56&&lng<-34.1&&lat<6.6){
			return "http://dbpedia.org/class/custom/City_in_South_America";
		}	
		else return "http://dbpedia.org/class/custom/City_in_World";
	}
	
	public static Set<StringTuple> getTuplesToAdd(String uri){
		String subClass="http://www.w3.org/2000/01/rdf-schema#subClassOf";
			
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("http://dbpedia.org/class/custom/City_in_Saxony", "http://dbpedia.org/class/custom/City_in_Europe");
		map.put("http://dbpedia.org/class/custom/City_in_Egypt", "http://dbpedia.org/class/custom/City_in_Africa");
		map.put("http://dbpedia.org/class/custom/City_in_Europe", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_Asia", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_Australia", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_North_America", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_South_America", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_Africa", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_World", "http://dbpedia.org/class/yago/City108524735");
		Set<StringTuple> toAdd = new HashSet<StringTuple>();
		if (map.containsKey(uri)){
			toAdd.add(new StringTuple(subClass,map.get(uri)));
		}
		return toAdd;
	}
}
