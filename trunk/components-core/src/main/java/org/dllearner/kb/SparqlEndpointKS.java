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
 *
 */
package org.dllearner.kb;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.sparql.SparqlEndpoint;

/**
 * SPARQL endpoint knowledge source (without fragment extraction),
 * in particular for those algorithms which work directly on an endpoint
 * without requiring an OWL reasoner.
 * 
 * @author Jens Lehmann
 *
 */
public class SparqlEndpointKS implements KnowledgeSource {

	private SparqlEndpoint endpoint;

	// TODO: turn those into config options
	private URL url;
	private List<String> defaultGraphURIs = new LinkedList<String>();
	private List<String> namedGraphURIs = new LinkedList<String>();
	
	public SparqlEndpointKS() {
		
	}
	
	public SparqlEndpointKS(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	@Override
	public void init() throws ComponentInitException {
		if(endpoint == null) {
			endpoint = new SparqlEndpoint(url, defaultGraphURIs, namedGraphURIs);
		}
	}
	
	public SparqlEndpoint getEndpoint() {
		return endpoint;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public List<String> getDefaultGraphURIs() {
		return defaultGraphURIs;
	}

	public void setDefaultGraphURIs(List<String> defaultGraphURIs) {
		this.defaultGraphURIs = defaultGraphURIs;
	}

	public List<String> getNamedGraphURIs() {
		return namedGraphURIs;
	}

	public void setNamedGraphURIs(List<String> namedGraphURIs) {
		this.namedGraphURIs = namedGraphURIs;
	}	
	
}
