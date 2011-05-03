package org.dllearner.algorithm.tbsl.sparql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Slot {

	String anchor;
	SlotType type;
	List<String> words;
	
	public Slot(String a,List<String> ws) {
		anchor = a;
		type = SlotType.UNSPEC; 
		words = ws;
		replaceUnderscores();
	}
	public Slot(String a,SlotType t,List<String> ws) {
		anchor = a;
		type = t;
		words = ws;
		replaceUnderscores();
	}
	
	public void setSlotType(SlotType st) {
		type = st;
	}
	
	public SlotType getSlotType(){
		return type;
	}
	
	public String getAnchor() {
		return anchor;
	}
	public void setAnchor(String s) {
		anchor = s;
	}
	
	public List<String> getWords() {
		return words;
	}
	public void setWords(List<String> ws) {
		words = ws;
	}
	
	public void replaceReferent(String ref1,String ref2) {
		if (anchor.equals(ref1)) {
			anchor = ref2;
		}
	}
	
	public void replaceUnderscores() {
		ArrayList<String> newWords = new ArrayList<String>();
		for (String w : words) {
			newWords.add(w.replaceAll("_"," "));
		}
		words = newWords;
	}
	
	public String toString() {
		
		String out = anchor + ": " + type + " {";
		
		for (Iterator<String> i = words.iterator(); i.hasNext();) {
			out += i.next();
			if (i.hasNext()) {
				out += ",";
			}
		}
		
		out += "}";
		
		return out;
	}
	
	public Slot clone() {
		
		List<String> newWords = new ArrayList<String>();
		for (String word : words) {
			newWords.add(word);
		}
		
		return new Slot(anchor,type,newWords);
	}
	
}
