package org.dllearner.kb.sparql;

import java.util.HashSet;
import java.util.Set;

public class PredefinedFilter {
    
	    
	public static SparqlQueryType getFilter(int i) {

		switch (i) {
		case 1:
			return YagoFilter();

		}
		return null;
	}
	
	
	public static SparqlQueryType YagoFilter(){
	Set<String> pred = new HashSet<String>();
		pred.add("http://www.w3.org/2004/02/skos/core");
		pred.add("http://www.w3.org/2002/07/owl#sameAs");
		pred.add("http://xmlns.com/foaf/0.1/");
		pred.add("http://dbpedia.org/property/reference");
		pred.add("http://dbpedia.org/property/website");
		pred.add("http://dbpedia.org/property/wikipage");

		Set<String> obj = new HashSet<String>();
		obj.add("http://dbpedia.org/resource/Category:Wikipedia_");
		obj.add("http://dbpedia.org/resource/Category:Articles_");
		obj.add("http://xmlns.com/foaf/0.1/");
		obj.add("http://upload.wikimedia.org/wikipedia/commons");
		obj.add("http://upload.wikimedia.org/wikipedia");
		obj.add("http://www.geonames.org");
		obj.add("http://www.w3.org/2006/03/wn/wn20/instances/synset");
		obj.add("http://www4.wiwiss.fu-berlin.de/flickrwrappr");
		obj.add("http://www.w3.org/2004/02/skos/core");

		return new SparqlQueryType("forbid", obj, pred, "false");
	}
}
