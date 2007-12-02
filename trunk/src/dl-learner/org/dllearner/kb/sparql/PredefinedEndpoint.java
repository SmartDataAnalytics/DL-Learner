package org.dllearner.kb.sparql;

import java.net.URL;
import java.util.HashMap;

public class PredefinedEndpoint {
    public static SpecificSparqlEndpoint getEndpoint(int i) {

	switch (i) {
	case 1:
		return dbpediaEndpoint();

	}
	return null;
    }
    
    public static SpecificSparqlEndpoint dbpediaEndpoint(){
    	URL u = null;
	HashMap<String, String> m = new HashMap<String, String>();
	m.put("default-graph-uri", "http://dbpedia.org");
	m.put("format", "application/sparql-results.xml");
	try {
		u = new URL("http://dbpedia.openlinksw.com:8890/sparql");
	} catch (Exception e) {
		e.printStackTrace();
	}
	return new SpecificSparqlEndpoint(u, "dbpedia.openlinksw.com", m);
}
}
