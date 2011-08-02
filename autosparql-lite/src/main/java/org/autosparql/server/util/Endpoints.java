package org.autosparql.server.util;

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
	
	
	public static List<SPARQLEndpointEx> loadEndpoints(String configPath){
		List<SPARQLEndpointEx> endpoints = new ArrayList<SPARQLEndpointEx>();
		try {
			XMLConfiguration config = new XMLConfiguration(new File(configPath));
			
			List endpointConfigurations = config.configurationsAt("endpoint");
			for(Iterator iter = endpointConfigurations.iterator();iter.hasNext();){
				HierarchicalConfiguration endpointConf = (HierarchicalConfiguration) iter.next();
				endpoints.add(createEndpoint(endpointConf));
			}
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return endpoints;
	}
	
	private static SPARQLEndpointEx createEndpoint(HierarchicalConfiguration endpointConf){
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
	
}

