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
import java.util.HashMap;

// one sparql endpoint configuration
public class SpecificSparqlEndpoint {

	String host;
	String hasQueryParameter;
	URL url;
	public HashMap<String, String> parameters = new HashMap<String, String>();

	public SpecificSparqlEndpoint(URL url, String host, HashMap<String, String> parameters) {
		super();
		this.host = host;
		this.url = url;
		this.hasQueryParameter = "query";
		this.parameters = parameters;
	}

	public String getHasQueryParameter() {
		return hasQueryParameter;
	}

	public void setHasQueryParameter(String hasQueryParameter) {
		this.hasQueryParameter = hasQueryParameter;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	public URL getURL() {
		return this.url;
	}

}
