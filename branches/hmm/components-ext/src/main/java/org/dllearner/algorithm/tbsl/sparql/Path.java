package org.dllearner.algorithm.tbsl.sparql;

public class Path {

	String start;
	String via;
	String target;
	
	public Path() {
		start = "";
		via = "";
		target = "";
	}
	public Path(String s,String v,String t) {
		start = s;
		via = v;
		target = t;
	}
	
	public void setStart(String s) {
		start = s;
	}
	public void setVia(String v) {
		via = v;
	}
	public void setTarget(String t) {
		target = t;
	}
	
	public String toString() {
		String v;
		if (via.equals("isA")) v = via; else v = "?"+via;
		
		if (via.isEmpty()) {
			return "?" + start + " -- " + "?" + target;
		}
		else {
			return "?" + start + " -- " + v + " -- ?" + target;
		}
	}
}
