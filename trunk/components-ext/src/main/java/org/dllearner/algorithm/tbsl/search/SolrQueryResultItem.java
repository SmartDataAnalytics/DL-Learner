package org.dllearner.algorithm.tbsl.search;

public class SolrQueryResultItem {
	
	private String label;
	private String uri;
	private float score;
	
	public SolrQueryResultItem(String label, String uri) {
		this(label, uri, -1);
	}
	
	public SolrQueryResultItem(String label, String uri, float score) {
		super();
		this.label = label;
		this.uri = uri;
		this.score = score;
	}

	public String getLabel() {
		return label;
	}

	public String getUri() {
		return uri;
	}

	public float getScore() {
		return score;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + Float.floatToIntBits(score);
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SolrQueryResultItem other = (SolrQueryResultItem) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (Float.floatToIntBits(score) != Float.floatToIntBits(other.score))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return uri + "(label: " + label + ", score: " + score + ")";
	}


}
