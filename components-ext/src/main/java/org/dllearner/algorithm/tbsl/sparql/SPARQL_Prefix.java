package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Prefix {
	
	private String name;
	private String url;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "PREFIX "+name+": <"+url+">";
	}
	
	public SPARQL_Prefix(String name, String url) {
		super();
		this.name = name;
		this.url = url;
	}

}
