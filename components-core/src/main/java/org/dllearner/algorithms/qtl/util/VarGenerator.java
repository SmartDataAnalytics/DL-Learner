package org.dllearner.algorithms.qtl.util;

import com.hp.hpl.jena.sparql.core.Var;

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
	
	public Var newVar(){
		return Var.alloc(base + cnt++);
	}
	
	public void reset(){
		cnt = 0;
	}
}
