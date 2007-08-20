package org.dllearner.dl;

import java.util.List;

public class MultiDisjunction extends Concept {

	public MultiDisjunction() {
		
	}
	
	public MultiDisjunction(Concept... children) {
		for(Concept child : children) {
			addChild(child);
		}
	}
	
	// Kinder müssen in einer Liste sein, sonst ist nicht garantiert,
	// dass die Ordnung der Kinder die gleiche wie im Argument ist
	public MultiDisjunction(List<Concept> children) {
		for(Concept child : children) {
			addChild(child);
		}
	}
	
	@Override
	public int getArity() {
		return children.size();		
	}

	public int getLength() {
		int length = 0;
		for(Concept child : children) {
			length += child.getLength();
		}
		return length + children.size() - 1;		
	}

	public String toString() {
		if(children.size()==0)
			return "EMPTY_OR";
		
		String ret = "(";
		for(int i=0; i<children.size()-1; i++) {
			ret += children.get(i).toString() + " OR "; 
		}
		ret += children.get(children.size()-1).toString() + ")";
		return ret;
	}	
	
	public String toStringOld() {
		String ret = "MULTI_OR [";
		for(Concept child : children) {
			ret += child.toString() + ",";
		}
		ret += "]";
		return ret;
	}
	
}
