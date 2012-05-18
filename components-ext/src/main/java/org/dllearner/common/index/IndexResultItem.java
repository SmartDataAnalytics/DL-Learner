package org.dllearner.common.index;

public class IndexResultItem {
	
	private String uri;
	private String label;
	private float score;
	
	public IndexResultItem(String uri, String label, float score) {
		this.uri = uri;
		this.label = label;
		this.score = score;
	}
	
	public String getUri() {
		return uri;
	}
	
	public String getLabel() {
		return label;
	}
	
	public float getScore() {
		return score;
	}
}
