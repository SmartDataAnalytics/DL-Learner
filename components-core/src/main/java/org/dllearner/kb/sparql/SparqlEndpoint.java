/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import com.google.common.collect.Lists;

import java.net.MalformedURLException;
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
		this.defaultGraphURIs= new LinkedList<>();
		this.namedGraphURIs= new LinkedList<>();
	}

	public SparqlEndpoint(URL u, List<String> defaultGraphURIs, List<String> namedGraphURIs) {
		this.url = u;
		this.defaultGraphURIs=defaultGraphURIs;
		this.namedGraphURIs=namedGraphURIs;
	}

	public SparqlEndpoint(URL url, String defaultGraphURI) {
		this(url, Collections.singletonList(defaultGraphURI), Collections.emptyList());
	}

	public URL getURL() {
		return this.url;
	}

	public static SparqlEndpoint create(String url, String defaultGraphURI) throws MalformedURLException {
		return create(url, defaultGraphURI == null ? Collections.emptyList() : Lists.newArrayList(defaultGraphURI));
	}

	public static SparqlEndpoint create(String url, List<String> defaultGraphURIs) throws MalformedURLException {
		return create(url, defaultGraphURIs, Collections.emptyList());
	}

	public static SparqlEndpoint create(String url, List<String> defaultGraphURIs, List<String> namedGraphURIs) throws MalformedURLException {
		return new SparqlEndpoint(new URL(url), defaultGraphURIs, namedGraphURIs);
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

		switch (name) {
			case "DBPEDIA":
				return getEndpointDBpedia();
			case "GOVTRACK":
				return getEndpointGovTrack();
			case "SPARQLETTE":
				return getEndpointSparqlette();
			case "SWCONFERENCE":
				return getEndpointSWConference();
			case "REVYU":
				return getEndpointRevyu();
			case "MYOPENLINK":
				return getEndpointMyOpenlink();
			case "MUSICBRAINZ":
				return getEndpointMusicbrainz();
			default:
				return null;
		}
	}


	public static List<SparqlEndpoint> listEndpoints() {
		LinkedList<SparqlEndpoint> ll = new LinkedList<>();
		ll.add(getEndpointDBpedia());
		ll.add(getEndpointDOAPspace());
		ll.add(getEndpointGovTrack());
		ll.add(getEndpointMusicbrainz());
		ll.add(getEndpointMyOpenlink());
		ll.add(getEndpointRevyu());
		ll.add(getEndpointSWConference());
		ll.add(getEndpointUSCensus());
		ll.add(getEndpointSparqlette());
		return ll;
	}

	public static SparqlEndpoint getEndpointDBpedia() {
		URL u = null;
		try {
			u = new URL("http://dbpedia.org/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointDBpediaLiveAKSW() {
		URL u = null;
		try {
//			u = new URL("http://dbpedia.aksw.org:8899/sparql");
			u = new URL("http://live.dbpedia.org/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointDBpediaHanne() {
		URL u = null;
		try {
			u = new URL("http://hanne.aksw.org:8892/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointDBpediaLiveOpenLink() {
		URL u = null;
		try {
			u = new URL("http://dbpedia-live.openlinksw.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointLOD2Cloud() {
		URL u = null;
		try {
			u = new URL("http://lod.openlinksw.com/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
//		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointDBpediaLOD2Cloud() {
		URL u = null;
		try {
			u = new URL("http://lod.openlinksw.com/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
		defaultGraphURIs.add("http://dbpedia.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointLinkedGeoData() {
		URL u = null;
		try {
			u = new URL("http://linkedgeodata.org/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
		//TODO defaultGraphURIs.add("http://geonames.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointLinkedMDB() {
		URL u = null;
		try {
			u = new URL("http://www.linkedmdb.org/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
		//TODO defaultGraphURIs.add("http://geonames.org");
		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointGovTrack() {
		URL u = null;
		try {
			u = new URL("http://www.rdfabout.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<>(), new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointRevyu() {
		URL u = null;
		try {
			u = new URL("http://revyu.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SparqlEndpoint(u, new LinkedList<>(), new LinkedList<>());
	}

	public static SparqlEndpoint getEndpointMyOpenlink() {
		URL u = null;
		try {
			u = new URL("http://myopenlink.net:8890/sparql/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
		defaultGraphURIs.add("http://myopenlink.net/dataspace");

		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
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

	public static SparqlEndpoint getEndpointSWConference() {
		URL u = null;
		try {
			u = new URL("http://data.semanticweb.org:8080/openrdf-sesame/repositories/SWC");
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

	public static SparqlEndpoint getEndpointUSCensus() {
		URL u = null;
		try {
			u = new URL("http://www.rdfabout.com/sparql");
		} catch (Exception e) {
			e.printStackTrace();
		}
		LinkedList<String> defaultGraphURIs= new LinkedList<>();
		defaultGraphURIs.add("http://www.rdfabout.com/rdf/schema/census/");

		return new SparqlEndpoint(u, defaultGraphURIs, new LinkedList<>());
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
