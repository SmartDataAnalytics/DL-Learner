package org.dllearner.kb.sparql;

import java.util.HashMap;

public class SparqlEndpoint {

	String host;
	int port;
	String hasAfterGET;
	String hasQueryParameter;
	String hasURL;
	public HashMap<String, String> parameters = new HashMap<String, String>();

	public SparqlEndpoint(String host, String port, String hasAfterGET, String hasQueryParameter,
			HashMap<String, String> parameters) {
		super();
		this.host = host;
		this.port = Integer.parseInt(port);
		this.hasAfterGET = hasAfterGET;
		this.hasQueryParameter = hasQueryParameter;
		this.parameters = parameters;
	}

	public SparqlEndpoint(String host, int port, String hasURL, HashMap<String, String> parameters) {
		super();
		this.port = port;
		this.host = host;
		this.hasURL = hasURL;
		this.hasQueryParameter = "query";
		this.parameters = parameters;
	}

	public String getHasAfterGET() {
		return hasAfterGET;
	}

	public void setHasAfterGET(String hasAfterGET) {
		this.hasAfterGET = hasAfterGET;
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/*
	 * sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=" +
	 * //"SELECT%20%2A%20WHERE%20%7B%20%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FAristotle%3E%20%3Fa%20%3Fb%20%7D%20" +
	 * URLEncoder.encode(query, "UTF-8")+ //query+// URLencode
	 * "&format=application%2Fsparql-results%2Bxml
	 */
}
