package org.dllearner.autosparql.server.util;

import java.net.URL;
import java.util.List;

import org.dllearner.kb.sparql.SparqlEndpoint;

public class SPARQLEndpointEx extends SparqlEndpoint {
	private String label;
	private List<String> predicateFilters;
	private String prefix;
	
	public SPARQLEndpointEx(URL u, List<String> defaultGraphURIs,
			List<String> namedGraphURIs, String label, String prefix, List<String> predicateFilters) {
		super(u, defaultGraphURIs, namedGraphURIs);
		
		this.label = label;
		this.prefix = prefix;
		this.predicateFilters = predicateFilters;
	}
	
	public String getLabel(){
		return label;
	}
	
	public String getPrefix(){
		return prefix;
	}
	
	public List<String> getPredicateFilters(){
		return predicateFilters;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ENDPOINT\n");
		sb.append("Label: ").append(getLabel()).append("\n");
		sb.append("URL: ").append(getURL()).append("\n");
		sb.append("Default Graph URI: ").append(getDefaultGraphURIs()).append("\n");
		sb.append("Named Graph URI: ").append(getNamedGraphURIs()).append("\n");
		sb.append("Predicate Filters: ").append(getPredicateFilters()).append("\n");
		
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true;
		}
		if(!(obj instanceof SPARQLEndpointEx) || obj == null){
			return false;
		}
		SPARQLEndpointEx other = (SPARQLEndpointEx)obj;
		return this.getURL().equals(other.getURL());
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
