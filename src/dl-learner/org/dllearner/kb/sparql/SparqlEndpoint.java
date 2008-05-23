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
	
	/*public static SparqlEndpoint getEndpointByNumber(int i) {

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
	}*/
	
	public static SparqlEndpoint getEndpointByName(String name) {

		name = name.toUpperCase();
		
		if (name.equals("DBPEDIA"))
			return EndpointDBpedia();
		if (name.equals("LOCALDBPEDIA"))
			return EndpointLOCALDBpedia();
		if (name.equals("LOCALJOSECKI"))
			return EndpointlocalJoseki();
		if (name.equals("GOVTRACK"))
			return EndpointGovTrack();
		if (name.equals("SPARQLETTE"))
			return EndpointSparqlette();
		if (name.equals("SWCONFERENCE"))
			return EndpointSWConference();
		if (name.equals("REVYU"))
			return EndpointRevyu();
		if (name.equals("MYOPENLINK"))
			return EndpointMyOpenlink();
		if (name.equals("FACTBOOK"))
			return EndpointWorldFactBook();
		if (name.equals("DBLP"))
			return EndpointDBLP();
		if (name.equals("MUSICBRAINZ"))
			return EndpointMusicbrainz();
		return null;
	}
	
	
	public static LinkedList<SparqlEndpoint> listEndpoints() {
		LinkedList<SparqlEndpoint> ll =new LinkedList<SparqlEndpoint>();
		ll.add(EndpointDBLP());
		ll.add(EndpointDBpedia());
		ll.add(EndpointDOAPspace());
		ll.add(EndpointGovTrack());
		ll.add(EndpointJamendo());
		ll.add(EndpointJohnPeel());
		ll.add(EndpointlocalJoseki());
		ll.add(EndpointMagnaTune());
		ll.add(EndpointMusicbrainz());
		ll.add(EndpointMyOpenlink());
		ll.add(EndpointRevyu());
		ll.add(EndpointSWConference());
		ll.add(EndpointUSCensus());
		ll.add(EndpointWorldFactBook());
		ll.add(EndpointRiese());
		ll.add(EndpointTalisBlogs());
		ll.add(EndpointSWSchool());
		ll.add(EndpointSparqlette());
		ll.add(EndpointLOCALDBpedia());
		return ll;
	}
	
	public static SparqlEndpoint EndpointDBpedia() {
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
	
	public static SparqlEndpoint EndpointLOCALDBpedia() {
		URL u = null;
		try { 
			u = new URL("http://139.18.2.37:8890/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}

	public static SparqlEndpoint EndpointlocalJoseki() {
		URL u = null;
		try { 
			u = new URL("http://localhost:2020/books");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint EndpointWorldFactBook() {
		URL u = null;
		try { 
			u = new URL("http://www4.wiwiss.fu-berlin.de/factbook/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	

	public static SparqlEndpoint EndpointGovTrack() {
		URL u = null;
		try { 
			u = new URL("http://www.rdfabout.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint EndpointRevyu() {
		URL u = null;
		try { 
			u = new URL("http://revyu.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint EndpointMyOpenlink() {
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
	
	public static SparqlEndpoint EndpointDOAPspace() {
		URL u = null;
		try { 
			u = new URL("http://doapspace.org/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new SparqlEndpoint(u);

		}
	
	public static SparqlEndpoint EndpointJohnPeel() {
		URL u = null;
		try { 
			u = new URL("http://dbtune.org:3030/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new SparqlEndpoint(u);

		}
	
	
	
		public static SparqlEndpoint EndpointSWConference() {
		URL u = null;
		try { 
			u = new URL("http://data.semanticweb.org:8080/openrdf-sesame/repositories/SWC");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new SparqlEndpoint(u);

		}
	// returns strange xml
	/*
	public static SpecificSparqlEndpoint dbtune() {
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
	
	 public static SparqlEndpoint EndpointJamendo() {
			URL u = null;
			try { 
				u = new URL("http://dbtune.org:2105/sparql/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new SparqlEndpoint(u);
		}
	
	 
		 
	 public static SparqlEndpoint EndpointMagnaTune() {
		URL u = null;
		try { 
			u = new URL("http://dbtune.org:2020/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u);
	}
	
	 
	 public static SparqlEndpoint EndpointMusicbrainz() {
		 URL u = null;
		 try { 
			 u = new URL("http://dbtune.org/musicbrainz/sparql");
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return new SparqlEndpoint(u);
	 }
	 
	 public static SparqlEndpoint EndpointRiese() {
		 URL u = null;
		 try { 
			 u = new URL("http://riese.joanneum.at:3020/");
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return new SparqlEndpoint(u);
	 }
	
	 
	 public static SparqlEndpoint EndpointUSCensus() {
		 URL u = null;
		 try { 
			 u = new URL("http://www.rdfabout.com/sparql");
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 LinkedList<String> defaultGraphURIs=new LinkedList<String>();
			defaultGraphURIs.add("http://www.rdfabout.com/rdf/schema/census/");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	 }
	 
	 
	
	 
	/*
	 * it only has 4 classes
	 */
	 public static SparqlEndpoint EndpointDBLP() {
		URL u = null;
		try { 
			u = new URL("http://www4.wiwiss.fu-berlin.de/dblp/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u);
	}
	 
	 
	 public static SparqlEndpoint EndpointTalisBlogs() {
			URL u = null;
			try { 
				u = new URL("http://api.talis.com/stores/talisians/services/sparql");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new SparqlEndpoint(u);
		}
	 
	 public static SparqlEndpoint EndpointSparqlette() {
			URL u = null;
			try { 
				u = new URL("http://www.wasab.dk/morten/2005/04/sparqlette/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new SparqlEndpoint(u);
		}
	 
	 
	 
	 public static SparqlEndpoint EndpointSWSchool() {
			URL u = null;
			try { 
				u = new URL("http://sparql.semantic-web.at/snorql/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new SparqlEndpoint(u);
		}
	 
	
	
	

}
