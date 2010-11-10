package org.dllearner.autosparql.client.model;


import com.extjs.gxt.ui.client.data.BaseModel;

public class Endpoint extends BaseModel{
	
	
	private static final long serialVersionUID = -3347290446256124889L;
	
	public Endpoint(){
	}
	
	public Endpoint(int id, String label){
		set("id", id);
		set("label", label);
	}
	
	public String getLabel(){
		return get("label");
	}
	
	public int getID(){
		return (Integer)get("id");
	}

}
