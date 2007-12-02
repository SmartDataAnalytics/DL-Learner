package org.dllearner.kb.sparql;

import java.net.URL;
import java.util.HashMap;

public class SpecificSparqlEndpoint {

	
	
	String host;
	String hasQueryParameter;
	URL url;
	public HashMap<String, String> parameters = new HashMap<String, String>();

		
	public SpecificSparqlEndpoint(URL url,String host,  HashMap<String, String> parameters) {
		super();
		this.host=host;
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
