package org.dllearner.autosparql.client.model;


import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseModel;

public class Endpoint extends BaseModel implements Serializable{
	
	
	private static final long serialVersionUID = -3347290446256124889L;
	
	public Endpoint(){
	}
	
	public Endpoint(String label){
		set("label", label);
	}
	
	public String getLabel(){
		return get("label");
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Endpoint) || obj == null){
			return false;
		}
		if(obj == this){
			return true;
		}
		Endpoint other = (Endpoint)obj;
		return other.getLabel().equals(this.getLabel());
	}
	
	@Override
	public int hashCode() {
		return getLabel().hashCode();
	}
	
}
