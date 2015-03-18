package org.dllearner.algorithms.qtl.util;

/**
 * @author Lorenz Buehmann
 *
 */
public class VarGenerator {
	
	private final String header = "?";
	private final String base;
	private int cnt = 0;
	
	public VarGenerator(String base) {
		this.base = base;
	}
	
	public VarGenerator() {
		this("s");
	}
	
	public String newVar(){
		return header + base + cnt++;
	}
	
	public void reset(){
		cnt = 0;
	}
}
