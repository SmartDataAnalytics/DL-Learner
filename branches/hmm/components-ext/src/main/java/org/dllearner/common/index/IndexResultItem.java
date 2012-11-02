package org.dllearner.common.index;

import java.util.Collections;
import java.util.Map;

/** Natural ordering is negated natural order of scores (highest to lowest score) if score is different, else lexical order of urls if urls are equal else lexical ordering of labels.
 * This keeps the ordering consistent with equals and hashCode because those ignore the fields too. **/
public class IndexResultItem implements Comparable<IndexResultItem>
{
	private final String uri;
	private final String label;
	private final float score;
	private final Map<String,? extends Object> fields;
	
	public IndexResultItem(String uri, String label, float score)
	{this(uri,label,score,Collections.<String,Object>emptyMap());}
	
	public IndexResultItem(String uri, String label, float score,Map<String,? extends Object> fields)
	{
		this.uri = uri;
		this.label = label;
		this.score = score;
		if(fields==null) throw new AssertionError("fields null");
		this.fields = fields; 		
	}
	
	public String getUri() {return uri;}	
	public String getLabel() {return label;	}	
	public float getScore() {return score;}
	public Map<String,? extends Object> getFields() {return fields;}
	
	@Override public String toString() {return "label:" + label + "--uri:" + uri + "--fields:" + fields;}
	
	@Override public int compareTo(IndexResultItem item)
	{
		int i; 
		i = -Float.compare(this.score, item.score);
		if(i!=0) return i;
		i = this.uri.compareTo(item.uri);
		if(i!=0) return i;
		i = this.label.compareTo(item.label);		
		return i;		
	}

	@Override public int hashCode()
	{
		final int prime = 31;
		int result = 1;
//		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + Float.floatToIntBits(score);
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		IndexResultItem other = (IndexResultItem) obj;
//		if (fields == null)
//		{
//			if (other.fields != null) return false;
//		}
//		else if (!fields.equals(other.fields)) return false;
		if (label == null)
		{
			if (other.label != null) return false;
		}
		else if (!label.equals(other.label)) return false;
		if (Float.floatToIntBits(score) != Float.floatToIntBits(other.score)) return false;
		if (uri == null)
		{
			if (other.uri != null) return false;
		}
		else if (!uri.equals(other.uri)) return false;
		return true;
	}	
}