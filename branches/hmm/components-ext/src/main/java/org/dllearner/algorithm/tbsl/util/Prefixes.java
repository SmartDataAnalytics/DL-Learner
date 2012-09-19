package org.dllearner.algorithm.tbsl.util;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class Prefixes {
	
	private static Map<String, String> prefixes = new HashMap<String, String>();
	
	static {
		prefixes.put(RDF.getURI(), "rdf");
		prefixes.put(RDFS.getURI(), "rdfs");
		prefixes.put("http://dbpedia.org/ontology/", "dbo");
		prefixes.put("http://dbpedia.org/property/", "dbp");
		prefixes.put("http://dbpedia.org/resource/", "dbr");
		prefixes.put(FOAF.getURI(), "foaf");
		prefixes.put("http://dbpedia.org/class/yago/", "yago");
	}
	
	public static Map<String, String> getPrefixes(){
		return prefixes;
	}

}
