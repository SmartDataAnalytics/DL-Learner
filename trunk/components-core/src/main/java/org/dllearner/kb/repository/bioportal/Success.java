package org.dllearner.kb.repository.bioportal;

public class Success {
	private String accessedResource;
	private String accessDate;
	private Data data;
	
	public String getAccessedResource() {
		return accessedResource;
	}
	public void setAccessedResource(String accessedResource) {
		this.accessedResource = accessedResource;
	}
	public String getAccessDate() {
		return accessDate;
	}
	public void setAccessDate(String accessDate) {
		this.accessDate = accessDate;
	}
	public Data getData() {
		return data;
	}
	public void setData(Data data) {
		this.data = data;
	}	
}
