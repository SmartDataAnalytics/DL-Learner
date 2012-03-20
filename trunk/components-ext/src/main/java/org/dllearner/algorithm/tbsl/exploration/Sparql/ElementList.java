package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.util.HashMap;

import org.dllearner.algorithm.tbsl.exploration.Utils.QueryPair;


/**
 * 
 * @author swalter
 *
 */
public class ElementList {
	
	/**
	 * Name of the variable, e.g. ?y0
	 */
	private String variablename;
	
	/**
	 * URI of the Resource or Class, which was used for getting the depending elements with the uri
	 */
	private String uri;
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
	public String getURI() {
		return uri;
	}
	public void setURI(String resourceURI) {
		this.uri = resourceURI;
	}
	
	public ElementList(String name_new, String resource, HashMap<String,String> hm){
		this.setHm(hm);
		this.setURI(resource);
		this.setVariablename(name_new);
	}
	
	public void printAll(){
		System.out.println("Name: "+this.getVariablename());
		System.out.println("URI: "+this.getURI());
		System.out.println("List of Elements:");
		for(String key : this.hm.keySet()){
			System.out.println( key + ": "+this.hm.get(key));
		}
	}
	
	public String printToString(){
		String result="";
		result+="Name: "+this.getVariablename()+"\n";
		result+="URI: "+this.getURI()+"\n";
		result+="List of Elements:"+"\n";
		for(String key : this.hm.keySet()){
			result+=key + ": "+this.hm.get(key)+"\n";
		}
		
		return result;
	}
}
