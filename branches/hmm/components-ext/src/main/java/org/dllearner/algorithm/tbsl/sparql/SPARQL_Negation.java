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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((term == null) ? 0 : term.hashCode());
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
		SPARQL_Negation other = (SPARQL_Negation) obj;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}
	
	

}
