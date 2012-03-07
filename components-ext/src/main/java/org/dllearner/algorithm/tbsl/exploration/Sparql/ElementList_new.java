package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.util.HashMap;

/**
 * 
 * @author swalter
 *
 */
public class ElementList_new {
	
	/**
	 * Name of the variable, e.g. ?y0
	 */
	private String variablename;
	
	/**
	 * URI of the Resource or Class, which was used for getting the depending elements with the uri
	 */
	private String resourceURI;
	/**
	 * HashMap with name -uri pairs.
	 */
	private HashMap<String,String> hm = new HashMap<String,String>();
	public String getVariablename() {
		return variablename;
	}
	public void setVariablename(String variablename) {
		this.variablename = variablename;
	}
	public HashMap<String,String> getHm() {
		return hm;
	}
	public void setHm(HashMap<String,String> hm) {
		this.hm = hm;
	}
	public String getResourceURI() {
		return resourceURI;
	}
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}
	
	public ElementList_new(String variable, String resource, HashMap<String,String> hm){
		this.setHm(hm);
		this.setResourceURI(resource);
		this.setVariablename(variable);
	}
}
