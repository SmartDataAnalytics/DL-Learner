/**
 * Copyright (C) 2007, Sebastian Hellmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.kb.sparql;

import java.net.URL;
import java.util.HashMap;

// holds some predefined endpoints
public class PredefinedEndpoint {
	public static SpecificSparqlEndpoint getEndpoint(int i) {

		switch (i) {
		case 1:
			return dbpediaEndpoint();
		case 2:
			return localJoseki();
		case 3: 
			return govTrack();
		case 4:
			return revyu();
		case 5:
			return myopenlink();
		case 6: 
			return worldFactBook();
		}
		return null;
	}

	public static SpecificSparqlEndpoint dbpediaEndpoint() {
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

	public static SpecificSparqlEndpoint localJoseki() {
		URL u = null;
		HashMap<String, String> m = new HashMap<String, String>();
		// m.put("default-graph-uri", "http://dbpedia.org");
		// m.put("format", "application/sparql-results.xml");
		try {
			u = new URL("http://localhost:2020/books");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SpecificSparqlEndpoint(u, "localhost", m);
	}
	public static SpecificSparqlEndpoint worldFactBook() {
		URL u = null;
		HashMap<String, String> m = new HashMap<String, String>();
		// m.put("default-graph-uri", "http://dbpedia.org");
		// m.put("format", "application/sparql-results.xml");
		try {
			u = new URL("http://www4.wiwiss.fu-berlin.de/factbook/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SpecificSparqlEndpoint(u, "www4.wiwiss.fu-berlin.de", m);
	}
	
	/*
	 * it only has 4 classes
	 public static SpecificSparqlEndpoint dblp() {
		URL u = null;
		HashMap<String, String> m = new HashMap<String, String>();
		// m.put("default-graph-uri", "http://dbpedia.org");
		// m.put("format", "application/sparql-results.xml");
		try {
			u = new URL("http://www4.wiwiss.fu-berlin.de/dblp/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SpecificSparqlEndpoint(u, "www4.wiwiss.fu-berlin.de", m);
	}
	*/
	public static SpecificSparqlEndpoint govTrack() {
		URL u = null;
		HashMap<String, String> m = new HashMap<String, String>();
		// m.put("default-graph-uri", "http://dbpedia.org");
		// m.put("format", "application/sparql-results.xml");
		try {
			u = new URL("http://www.rdfabout.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SpecificSparqlEndpoint(u, "www.rdfabout.com", m);
	}
	public static SpecificSparqlEndpoint revyu() {
		URL u = null;
		HashMap<String, String> m = new HashMap<String, String>();
		// m.put("default-graph-uri", "http://dbpedia.org");
		// m.put("format", "application/sparql-results.xml");
		//http://revyu.com/sparql?query=SELECT DISTINCT * WHERE {[] a ?c}
		try {
			u = new URL("http://revyu.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SpecificSparqlEndpoint(u, "revyu.com", m);
	}
	
	// returns strange xml
	/*public static SpecificSparqlEndpoint dbtune() {
		URL u = null;
		HashMap<String, String> m = new HashMap<String, String>();
		// m.put("default-graph-uri", "http://dbpedia.org");
		// m.put("format", "application/sparql-results.xml");
		//http://dbtune.org:2020/sparql/?query=SELECT DISTINCT * WHERE {[] a ?c}Limit 10 
		http://dbtune.org:2020/evaluateQuery?repository=default&serialization=rdfxml&queryLanguage=SPARQL&query=SELECT+DISTINCT+*+WHERE+%7B%5B%5D+a+%3Fc%7D
			&resultFormat=xml
			&resourceFormat=ns&entailment=none
			http://dbtune.org:2020/evaluateQuery	
			?repository=default&serialization=rdfxml&queryLanguage=SPARQL
					&query=SELECT+DISTINCT+*+WHERE+%7B%5B%5D+a+%3Fc%7D
			&resultFormat=xml
			&resourceFormat=ns&entailment=none
		try {
			u = new URL("http://dbtune.org:2020/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SpecificSparqlEndpoint(u, "dbtune.org", m);
	}*/
	
	public static SpecificSparqlEndpoint myopenlink() {
		URL u = null;
		HashMap<String, String> m = new HashMap<String, String>();
		m.put("default-graph-uri", "http://myopenlink.net/dataspace");
		 m.put("format", "application/sparql-results.xml");
		//http://myopenlink.net:8890/sparql/?query=select+distinct+%3FConcept+where+%7B%5B%5D+a+%3FConcept%7D
		try {
			u = new URL("http://myopenlink.net:8890/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SpecificSparqlEndpoint(u, "myopenlink.net", m);
	}
	
}
