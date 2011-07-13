package org.autosparql.shared;

import com.extjs.gxt.ui.client.data.BaseModel;

public class Endpoint extends BaseModel {

	private static final long serialVersionUID = -6359375538639325192L;
	
	private String url;
	private String label;
	
	public Endpoint() {
	}

	public Endpoint(String url, String label) {
		super();
		this.url = url;
		this.label = label;
		set("label", label);
	}
	
	public String getURL(){
		return url;
	}
	
	public String getLabel(){
		return label;
	}
	
	

}
