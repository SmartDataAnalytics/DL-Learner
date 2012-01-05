package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Having {

	public String filter;
	
	public SPARQL_Having(String s) {
		filter = s;
	}
	
	public String toString() {
		return "HAVING (" + filter + ")";
	}
}
