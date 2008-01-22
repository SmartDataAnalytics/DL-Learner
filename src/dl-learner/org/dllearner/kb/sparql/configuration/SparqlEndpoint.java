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
package org.dllearner.kb.sparql.configuration;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * One sparql endpoint configuration,
 * made to comply with Jena
 * 
 * @author Sebastian Hellmann
 *
 */
public class SparqlEndpoint {
	URL url;
	List<String> defaultGraphURIs;
	List<String> namedGraphURIs;
	//public HashMap<String, String> parameters = new HashMap<String, String>();

	public SparqlEndpoint(URL u) {
		this.url = u;
		this.defaultGraphURIs=new LinkedList<String>();
		this.namedGraphURIs=new LinkedList<String>();
	}
	
	public SparqlEndpoint(URL u,List<String> defaultGraphURIs,List<String> namedGraphURIs) {
		this.url = u;
		this.defaultGraphURIs=defaultGraphURIs;
		this.namedGraphURIs=namedGraphURIs;
	}
	

	public URL getURL() {
		return this.url;
	}

	public List<String> getDefaultGraphURIs() {
		return defaultGraphURIs;
	}

	public List<String> getNamedGraphURIs() {
		return namedGraphURIs;
	}
	
	public static SparqlEndpoint getEndpointByNumber(int i) {

		switch (i) {
		case 0:break;
			//should not be filled
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
	
	public static SparqlEndpoint dbpediaEndpoint() {
		URL u = null;
		try { 
			u = new URL("http://dbpedia.openlinksw.com:8890/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}

	public static SparqlEndpoint localJoseki() {
		URL u = null;
		try { 
			u = new URL("http://localhost:2020/books");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint worldFactBook() {
		URL u = null;
		try { 
			u = new URL("http://www4.wiwiss.fu-berlin.de/factbook/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	

	public static SparqlEndpoint govTrack() {
		URL u = null;
		try { 
			u = new URL("http://www.rdfabout.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint revyu() {
		URL u = null;
		try { 
			u = new URL("http://revyu.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint myopenlink() {
		URL u = null;
		try { 
			u = new URL("http://myopenlink.net:8890/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://myopenlink.net/dataspace");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());

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
	
	

}
