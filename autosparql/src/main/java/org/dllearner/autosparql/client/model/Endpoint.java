package org.dllearner.autosparql.client.model;


import com.extjs.gxt.ui.client.data.BaseModel;

public class Endpoint extends BaseModel{
	
	
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
		return obj == this || ((Endpoint)obj).getLabel().equals(this.getLabel());
	}
	
	@Override
	public int hashCode() {
		return 37+getLabel().hashCode();
	}
	
}
