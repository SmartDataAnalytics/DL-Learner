/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.kb.sparql;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * One sparql endpoint configuration,
 * made to comply with Jena.
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
	
	public SparqlEndpoint(URL u, List<String> defaultGraphURIs, List<String> namedGraphURIs) {
		this.url = u;
		this.defaultGraphURIs=defaultGraphURIs;
		this.namedGraphURIs=namedGraphURIs;
	}
	
	public SparqlEndpoint(URL url, String defaultGraphURI) {
		this(url, Collections.singletonList(defaultGraphURI), Collections.<String>emptyList());
	}
	
	public URL getURL() {
		return this.url;
	}
	
	public String getHTTPRequest() {
		String ret = this.url.toString()+"?";
		ret += (defaultGraphURIs.isEmpty())?"":"default-graph-uri="+defaultGraphURIs.get(0)+"&";
		ret += "query="; 
		return ret;
	}

	public List<String> getDefaultGraphURIs() {
		return defaultGraphURIs;
	}

	public List<String> getNamedGraphURIs() {
		return namedGraphURIs;
	}
	
	@Override
	public String toString(){
		return getHTTPRequest();
	}
	
	
	public static SparqlEndpoint getEndpointByName(String name) {

		name = name.toUpperCase();
		
		if (name.equals("DBPEDIA")) {
			return getEndpointDBpedia();
		} else if (name.equals("LOCALDBPEDIA")) {
			return getEndpointLOCALDBpedia();
		} else if (name.equals("LOCALGEONAMES")) {
			return getEndpointLOCALGeonames();
		} else if (name.equals("LOCALGEODATA")) {
			return getEndpointLOCALGeoData();
		} else if (name.equals("LOCALJOSECKI") || name.equals("LOCALJOSEKI") ) {
			return getEndpointlocalJoseki();
		} else if (name.equals("LOCALJOSEKIBIBLE")||name.equals("LOCALJOSECKIBIBLE")) {
			return getEndpointLocalJosekiBible();
		} else if (name.equals("GOVTRACK")) {
			return getEndpointGovTrack();
		} else if (name.equals("SPARQLETTE")) {
			return getEndpointSparqlette();
		} else if (name.equals("SWCONFERENCE")) {
			return getEndpointSWConference();
		} else if (name.equals("REVYU")) {
			return getEndpointRevyu();
		} else if (name.equals("MYOPENLINK")) {
			return getEndpointMyOpenlink();
		} else if (name.equals("FACTBOOK")) {
			return getEndpointWorldFactBook();
		} else if (name.equals("DBLP")) {
			return getEndpointDBLP();
		} else if (name.equals("MUSICBRAINZ")) {
			return getEndpointMusicbrainz();
		} else {
			return null;
			}
	}
	
	
	public static List<SparqlEndpoint> listEndpoints() {
		LinkedList<SparqlEndpoint> ll =new LinkedList<SparqlEndpoint>();
		ll.add(getEndpointDBLP());
		ll.add(getEndpointDBpedia());
		ll.add(getEndpointDOAPspace());
		ll.add(getEndpointGovTrack());
		ll.add(getEndpointJamendo());
		ll.add(getEndpointJohnPeel());
		ll.add(getEndpointlocalJoseki());
		ll.add(getEndpointMagnaTune());
		ll.add(getEndpointMusicbrainz());
		ll.add(getEndpointMyOpenlink());
		ll.add(getEndpointRevyu());
		ll.add(getEndpointSWConference());
		ll.add(getEndpointUSCensus());
		ll.add(getEndpointWorldFactBook());
		ll.add(getEndpointRiese());
		ll.add(getEndpointTalisBlogs());
		ll.add(getEndpointSWSchool());
		ll.add(getEndpointSparqlette());
		ll.add(getEndpointLOCALDBpedia());
		return ll;
	}
	
	public static SparqlEndpoint getEndpointDBpedia() {
		URL u = null;
		try { 
			u = new URL("http://dbpedia.org/sparql");
//			u = new URL("http://dbpedia.openlinksw.com:8890/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointLOCALDBpedia() {
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
	
	public static SparqlEndpoint getEndpointDBpediaAKSW() {
		URL u = null;
		try { 
			u = new URL("http://dbpedia.aksw.org:8890/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointDBpediaLiveAKSW() {
		URL u = null;
		try { 
//			u = new URL("http://dbpedia.aksw.org:8899/sparql");
			u = new URL("http://live.dbpedia.org/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointDBpediaHanne() {
		URL u = null;
		try { 
			u = new URL("http://hanne.aksw.org:8892/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointDBpediaLiveOpenLink() {
		URL u = null;
		try { 
			u = new URL("http://dbpedia-live.openlinksw.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointLOD2Cloud() {
		URL u = null;
		try { 
			u = new URL("http://lod.openlinksw.com/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
//		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointDBpediaLOD2Cloud() {
		URL u = null;
		try { 
			u = new URL("http://lod.openlinksw.com/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointLinkedGeoData() {
		URL u = null;
		try { 
			u = new URL("http://linkedgeodata.org/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		//TODO defaultGraphURIs.add("http://geonames.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}	

	public static SparqlEndpoint getEndpointLOCALGeonames() {
		URL u = null;
		try { 
			u = new URL("http://139.18.2.37:8890/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://geonames.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}	
	
	public static SparqlEndpoint getEndpointLOCALGeoData() {
		URL u = null;
		try { 
			u = new URL("http://139.18.2.37:8890/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs=new LinkedList<String>();
		defaultGraphURIs.add("http://linkedgeodata.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<String>());
	}		
	
	public static SparqlEndpoint getEndpointlocalJoseki() {
		URL u = null;
		try { 
			u = new URL("http://localhost:2020/books");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointLocalJosekiBible() {
		URL u = null;
		try { 
			u = new URL("http://localhost:2020/bible");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointWorldFactBook() {
		URL u = null;
		try { 
			u = new URL("http://www4.wiwiss.fu-berlin.de/factbook/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	

	public static SparqlEndpoint getEndpointGovTrack() {
		URL u = null;
		try { 
			u = new URL("http://www.rdfabout.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointRevyu() {
		URL u = null;
		try { 
			u = new URL("http://revyu.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<String>(), new LinkedList<String>());
	}
	
	public static SparqlEndpoint getEndpointMyOpenlink() {
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
	
	public static SparqlEndpoint getEndpointDOAPspace() {
		URL u = null;
		try { 
			u = new URL("http://doapspace.org/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new SparqlEndpoint(u);

		}
	
	public static SparqlEndpoint getEndpointJohnPeel() {
		URL u = null;
		try { 
			u = new URL("http://dbtune.org:3030/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new SparqlEndpoint(u);

		}
	
	
	
		public static SparqlEndpoint getEndpointSWConference() {
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
	
	 public static SparqlEndpoint getEndpointJamendo() {
			URL u = null;
			try { 
				u = new URL("http://dbtune.org:2105/sparql/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new SparqlEndpoint(u);
		}
	
	 
		 
	 public static SparqlEndpoint getEndpointMagnaTune() {
		URL u = null;
		try { 
			u = new URL("http://dbtune.org:2020/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u);
	}
	
	 
	 public static SparqlEndpoint getEndpointMusicbrainz() {
		 URL u = null;
		 try { 
			 u = new URL("http://dbtune.org/musicbrainz/sparql");
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return new SparqlEndpoint(u);
	 }
	 
	 public static SparqlEndpoint getEndpointRiese() {
		 URL u = null;
		 try { 
			 u = new URL("http://riese.joanneum.at:3020/");
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return new SparqlEndpoint(u);
	 }
	
	 
	 public static SparqlEndpoint getEndpointUSCensus() {
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
	 public static SparqlEndpoint getEndpointDBLP() {
		URL u = null;
		try { 
			u = new URL("http://www4.wiwiss.fu-berlin.de/dblp/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u);
	}
	 
	 
	 public static SparqlEndpoint getEndpointTalisBlogs() {
			URL u = null;
			try { 
				u = new URL("http://api.talis.com/stores/talisians/services/sparql");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new SparqlEndpoint(u);
		}
	 
	 public static SparqlEndpoint getEndpointSparqlette() {
			URL u = null;
			try { 
				u = new URL("http://www.wasab.dk/morten/2005/04/sparqlette/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new SparqlEndpoint(u);
		}
	 
	 
	 
	 public static SparqlEndpoint getEndpointSWSchool() {
			URL u = null;
			try { 
				u = new URL("http://sparql.semantic-web.at/snorql/");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new SparqlEndpoint(u);
		}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultGraphURIs == null) ? 0 : defaultGraphURIs.hashCode());
		result = prime * result + ((namedGraphURIs == null) ? 0 : namedGraphURIs.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SparqlEndpoint other = (SparqlEndpoint) obj;
		if (defaultGraphURIs == null) {
			if (other.defaultGraphURIs != null)
				return false;
		} else if (!defaultGraphURIs.equals(other.defaultGraphURIs))
			return false;
		if (namedGraphURIs == null) {
			if (other.namedGraphURIs != null)
				return false;
		} else if (!namedGraphURIs.equals(other.namedGraphURIs))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	 
	
	
	

}
