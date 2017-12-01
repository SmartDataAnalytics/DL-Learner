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
package org.dllearner.algorithms.qtl.util;

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
		String sb = "ENDPOINT\n" +
				"Label: " + getLabel() + "\n" +
				"URL: " + getURL() + "\n" +
				"Default Graph URI: " + getDefaultGraphURIs() + "\n" +
				"Named Graph URI: " + getNamedGraphURIs() + "\n" +
				"Predicate Filters: " + getPredicateFilters() + "\n";

		return sb;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		SPARQLEndpointEx that = (SPARQLEndpointEx) o;

		return getURL() != null ? getURL().equals(that.getURL()) : that.getURL() == null;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
