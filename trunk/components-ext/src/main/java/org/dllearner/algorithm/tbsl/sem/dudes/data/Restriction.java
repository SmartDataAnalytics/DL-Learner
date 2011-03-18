package org.dllearner.algorithm.tbsl.sem.dudes.data;

public class Restriction {
	
	String variable;
	String contextClass;
	
	public Restriction(String v,String c) {
		variable = v;
		contextClass = c;
	}
	
	public String getVariable() {
		return variable;
	}
	
	public void setVariable(String s) {
		variable = s;
	}
	
	public String toString() {
		return variable + "^" + contextClass;
	}
}
