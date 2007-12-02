package org.dllearner.kb.sparql;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PredefinedConfigurations {

    public static Configuration get(int i){
	
	switch (i){
		case 0: return dbpediaYago();
	
	}
	return null;
    }
    
    public static  Configuration dbpediaYago(){
	URL u=null;
	HashMap<String, String>m=new HashMap<String, String>();
	m.put("default-graph-uri","http://dbpedia.org");
	m.put("format","application/sparql-results.xml");
	try{
	    u=new URL("http://dbpedia.openlinksw.com:8890/sparql");
	}catch (Exception e) {e.printStackTrace();}
	SpecificSparqlEndpoint sse=new SpecificSparqlEndpoint(
		 u,"dbpedia.openlinksw.com",m);
	//System.out.println(u);
	Set<String>pred=new HashSet<String>();
	pred.add("http://www.w3.org/2004/02/skos/core");
	pred.add("http://www.w3.org/2002/07/owl#sameAs");
	pred.add("http://xmlns.com/foaf/0.1/");
	pred.add("http://dbpedia.org/property/reference");
	pred.add("http://dbpedia.org/property/website");
	pred.add("http://dbpedia.org/property/wikipage");
	
	
	Set<String>obj=new HashSet<String>();
	obj.add("http://dbpedia.org/resource/Category:Wikipedia_");
	obj.add("http://dbpedia.org/resource/Category:Articles_");
	obj.add("http://xmlns.com/foaf/0.1/");
	obj.add("http://upload.wikimedia.org/wikipedia/commons");
	obj.add("http://upload.wikimedia.org/wikipedia");
	obj.add("http://www.geonames.org");
	obj.add("http://www.w3.org/2006/03/wn/wn20/instances/synset");
	obj.add("http://www4.wiwiss.fu-berlin.de/flickrwrappr");
	obj.add("http://www.w3.org/2004/02/skos/core");
	
	SparqlQueryType sqt=new SparqlQueryType("forbid",obj,pred,"false");
	
	
	
	return new Configuration(sse,sqt,2,true);
	
    }
    
    
}
