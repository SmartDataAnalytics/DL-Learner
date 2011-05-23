package org.dllearner.autosparql.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class Example extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6955538657940009581L;
	
	public static enum Type{
		POSITIVE,
		NEGATIVE
	}
	
	public Example(){
	}
	
	public Example(String uri, String label, String imageURL, String comment){
		set("uri", uri);
		set("label", label);
		set("imageURL", imageURL);
		set("comment", comment);
	}
	
	public String getURI(){
		return (String)get("uri");
	}
	
	public String getLabel(){
		return (String)get("label");
	}
	
	public String getImageURL(){
		return (String)get("imageURL");
	}
	
	public String getComment(){
		return (String)get("comment");
	}
	
	@Override
	public String toString() {
		return getURI();
	}
	
	public ExampleSer toExampleSer(){
		return new ExampleSer(getURI(), getLabel(), getImageURL(), getComment());
	}

}
