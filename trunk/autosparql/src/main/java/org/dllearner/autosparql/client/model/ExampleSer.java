package org.dllearner.autosparql.client.model;

import java.io.Serializable;


public class ExampleSer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6955538657940009581L;
	
	private String uri;
	private String label;
	private String imageURL;
	private String comment;
	
	public ExampleSer() {
	}
	
	public ExampleSer(String uri, String label, String imageURL, String comment) {
		super();
		this.uri = uri;
		this.label = label;
		this.imageURL = imageURL;
		this.comment = comment;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Example toExample(){
		return new Example(uri, label, imageURL, comment);
	}

}
