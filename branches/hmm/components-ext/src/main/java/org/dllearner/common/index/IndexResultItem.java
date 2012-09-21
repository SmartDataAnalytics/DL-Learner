package org.dllearner.common.index;

import java.util.Collections;
import java.util.Map;

public class IndexResultItem {
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
	
	@Override public String toString()
	{
		// TODO Auto-generated method stub
		return "label:" + label + "--uri:" + uri + "--fields:" + fields;
	}
}
