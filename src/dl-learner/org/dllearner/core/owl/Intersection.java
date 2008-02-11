package org.dllearner.core.owl;

import java.util.List;
import java.util.Map;

public class Intersection extends Description {

	public Intersection() {
		
	}
	
	public Intersection(Description... children) {
		for(Description child : children) {
			addChild(child);
		}
	}
	
	public Intersection(List<Description> children) {
		for(Description child : children) {
			addChild(child);
		}
	}
	
	@Override
	public int getArity() {
		return children.size();
	}

	public int getLength() {
		int length = 0;
		for(Description child : children) {
			length += child.getLength();
		}
		return length + children.size() - 1;
	}

	public String toString(String baseURI, Map<String,String> prefixes) {
		if(children.size()==0)
			return "EMPTY_AND";
		
		String ret = "(";
		for(int i=0; i<children.size()-1; i++) {
			ret += children.get(i).toString(baseURI, prefixes) + " AND "; 
		}
		ret += children.get(children.size()-1).toString(baseURI, prefixes) + ")";
		return ret;
	}
	
	public String toStringOld() {
		String ret = "MULTI_AND [";
		for(Description child : children) {
			ret += child.toString() + ",";
		}
		ret += "]";
		return ret;
	}	
	
}
