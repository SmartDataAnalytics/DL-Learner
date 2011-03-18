package org.dllearner.algorithm.tbsl.sem.dudes.data;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sem.util.Label;
import org.dllearner.algorithm.tbsl.sem.util.Type;

public class Argument {

	String anchor;
	String referent;
	Type type;
	Label label;
	
	public Argument() {
	}
	public Argument(String a,String r,Type t,Label l) {
		label = l;
		referent = r;
		anchor = a;
		type = t;
	}
	
	public void setLabel(Label l) {
		label = l;
	}
	public void setAnchor(String s) {
		anchor = s;	
	}
	public void setType(Type t) {
		type = t;	
	}
	public void setReferent(String s) {
		referent = s;	
	}
	
	public String toString() {
		return "(" + anchor + "," + referent + "," + type + "," + label + ")";
	}
	
	public Argument clone() {
		return new Argument(anchor,referent,type,label);
	}
	
	public void replaceReferent(String ref1, String ref2) {
		if (referent.equals(ref1)) {
			referent = ref2;
		}
	}
	
	public Set<String> collectVariables() {
		
		Set<String> variables = new HashSet<String>();
		variables.add(referent);
		return variables;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anchor == null) ? 0 : anchor.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result
				+ ((referent == null) ? 0 : referent.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Argument other = (Argument) obj;
		if (anchor == null) {
			if (other.anchor != null)
				return false;
		} else if (!anchor.equals(other.anchor))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (referent == null) {
			if (other.referent != null)
				return false;
		} else if (!referent.equals(other.referent))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
