package org.dllearner.autosparql.server.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;

public class Endpoints {
	
	private List<SPARQLEndpointEx> endpoints;

	public Endpoints(String path){
		endpoints = new ArrayList<SPARQLEndpointEx>();
		try {
			XMLConfiguration config = new XMLConfiguration(new File(path));
			
			List endpointConfigurations = config.configurationsAt("endpoint");
			for(Iterator iter = endpointConfigurations.iterator();iter.hasNext();){
				HierarchicalConfiguration endpointConf = (HierarchicalConfiguration) iter.next();
				endpoints.add(createEndpoint(endpointConf));
			}
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private SPARQLEndpointEx createEndpoint(HierarchicalConfiguration endpointConf){
		try {
			URL url = new URL(endpointConf.getString("url"));
			String label = endpointConf.getString("label");
			String prefix = endpointConf.getString("prefix");
			if(prefix == null){
				prefix = label.replaceAll("@", "").replaceAll(" ", "");
			}
			String defaultGraphURI = endpointConf.getString("defaultGraphURI");
			List<String> namedGraphURIs = endpointConf.getList("namedGraphURI");
			List<String> predicateFilters = endpointConf.getList("predicateFilters.predicate");
			
			return new SPARQLEndpointEx(url, Collections.singletonList(defaultGraphURI), namedGraphURIs, label, prefix, new HashSet<String>(predicateFilters));
		} catch (MalformedURLException e) {
			System.err.println("Could not parse URL from SPARQL endpoint.");
			e.printStackTrace();
		}
		return null;
	
	}
	
	public List<SPARQLEndpointEx> getEndpoints(){
		return endpoints;
	}
	public static SPARQLEndpointEx getDBPediaAKSWEndpoint(){
		try {
			URL url = new URL("http://live.dbpedia.org/sparql/");
			List<String> defaultGraphURIs = Collections.singletonList("http://dbpedia.org");
			List<String> namedGraphURIs = Collections.emptyList();
			List<String> predicateFilters = new ArrayList<String>();
			predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
			predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
			SPARQLEndpointEx endpoint = new SPARQLEndpointEx(url, defaultGraphURIs, namedGraphURIs, "label", "prefix", new HashSet<String>(predicateFilters));
			return endpoint;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static SPARQLEndpointEx getDB0Endpoint(){
		try {
			URL url = new URL("http://db0.aksw.org:8999/sparql");
			List<String> defaultGraphURIs = Collections.singletonList("http://dbpedia.org");
			List<String> namedGraphURIs = Collections.emptyList();
			List<String> predicateFilters = new ArrayList<String>();
			predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
			predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
			SPARQLEndpointEx endpoint = new SPARQLEndpointEx(url, defaultGraphURIs, namedGraphURIs, "label", "prefix", new HashSet<String>(predicateFilters));
			return endpoint;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		Endpoints endpoints = new Endpoints("endpoints.xml");
		for(SPARQLEndpointEx endpoint : endpoints.getEndpoints()){
			System.out.println(endpoint);
		}
	}
}

