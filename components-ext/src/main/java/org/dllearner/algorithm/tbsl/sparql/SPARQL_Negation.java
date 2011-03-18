package org.dllearner.algorithm.tbsl.sparql;


public class SPARQL_Negation {
	
	SPARQL_Term term;

	public SPARQL_Negation(SPARQL_Term term) {
		super();
		this.term = term;
	}
	
	public String toString() {
		String retVal = "";
		SPARQL_Filter filter = new SPARQL_Filter();
		filter.addNotBound(term);
		return retVal;
	}

}
