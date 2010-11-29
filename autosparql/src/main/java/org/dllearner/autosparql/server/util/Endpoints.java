package org.dllearner.autosparql.server.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

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
			String defaultGraphURI = endpointConf.getString("defaultGraphURI");
			List<String> namedGraphURIs = endpointConf.getList("namedGraphURI");
			List<String> predicateFilters = endpointConf.getList("predicateFilters.predicate");
			
			return new SPARQLEndpointEx(url, Collections.singletonList(defaultGraphURI), namedGraphURIs, label, predicateFilters);
		} catch (MalformedURLException e) {
			System.err.println("Could not parse URL from SPARQL endpoint.");
			e.printStackTrace();
		}
		return null;
	
	}
	
	public List<SPARQLEndpointEx> getEndpoints(){
		return endpoints;
	}
	
	public static void main(String[] args){
		Endpoints endpoints = new Endpoints("endpoints.xml");
		for(SPARQLEndpointEx endpoint : endpoints.getEndpoints()){
			System.out.println(endpoint);
		}
	}
}

