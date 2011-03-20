package org.dllearner.algorithm.qtl.util;

import java.util.HashMap;
import java.util.Map;

public class Prefixes {
	
	public static Map<String,String> getPrefixes(){
		
		Map<String,String> prefixes = new HashMap<String,String>();
		prefixes.put("dbo","http://dbpedia.org/ontology/");
		prefixes.put("dbprop","http://dbpedia.org/property/");
		prefixes.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		prefixes.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("skos","http://www.w3.org/2004/02/skos/core#");
		prefixes.put("geo","http://www.w3.org/2003/01/geo/wgs84_pos#");
		prefixes.put("georss","http://www.georss.org/georss/");
		prefixes.put("owl","http://www.w3.org/2002/07/owl#");
		prefixes.put("yago","http://dbpedia.org/class/yago/");
		prefixes.put("cyc","http://sw.opencyc.org/concept/");
		prefixes.put("foaf","http://xmlns.com/foaf/0.1/");
		
		return prefixes;
	}
	
	public static String getDBpediaBaseURI(){
		return  "http://dbpedia.org/resource/";
	}

}
