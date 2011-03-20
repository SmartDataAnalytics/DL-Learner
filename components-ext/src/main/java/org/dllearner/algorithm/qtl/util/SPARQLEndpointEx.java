package org.dllearner.algorithm.qtl.util;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.kb.sparql.SparqlEndpoint;

public class SPARQLEndpointEx extends SparqlEndpoint {
	private String label;
	private Set<String> predicateFilters;
	private String prefix;
	private String baseURI;
	private Map<String, String> prefixes;
	
	public SPARQLEndpointEx(URL u, List<String> defaultGraphURIs,
			List<String> namedGraphURIs, String label, String prefix, Set<String> predicateFilters) {
		super(u, defaultGraphURIs, namedGraphURIs);
		
		this.label = label;
		this.prefix = prefix;
		this.predicateFilters = predicateFilters;
	}
	
	public SPARQLEndpointEx(URL u, List<String> defaultGraphURIs,
			List<String> namedGraphURIs, String label, String baseURI, Map<String, String> prefixes, Set<String> predicateFilters) {
		super(u, defaultGraphURIs, namedGraphURIs);
		
		this.label = label;
		this.baseURI = baseURI;
		this.prefixes = prefixes;
		this.predicateFilters = predicateFilters;
	}
	
	public SPARQLEndpointEx(SparqlEndpoint endpoint, String label, String prefix, Set<String> predicateFilters) {
		super(endpoint.getURL(), endpoint.getDefaultGraphURIs(), endpoint.getNamedGraphURIs());
		
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
	
	public String getBaseURI(){
		return baseURI;
	}
	
	public Map<String, String> getPrefixes(){
		return prefixes;
	}
	
	public Set<String> getPredicateFilters(){
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
