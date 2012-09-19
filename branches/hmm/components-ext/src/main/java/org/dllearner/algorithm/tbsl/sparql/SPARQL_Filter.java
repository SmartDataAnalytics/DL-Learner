package org.dllearner.algorithm.tbsl.sparql;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SPARQL_Filter implements Serializable
{
	private static final long serialVersionUID = -6243542586884540703L;
	
	private Set<SPARQL_Pair> terms = new HashSet<SPARQL_Pair>();
	private boolean and = true;
	
	//uses && if set true, otherwise ||
	public void setAnd(boolean and)
	{
		this.and = and;
	}

	public SPARQL_Filter(SPARQL_Pair pair)
	{
		super();
		this.terms.add(pair);
	}

	public SPARQL_Filter()
	{
	}

	public void addBound(SPARQL_Term term)
	{
		terms.add(new SPARQL_Pair(term, SPARQL_PairType.B));
	}
	
	public void addNotBound(SPARQL_Term term)
	{
		terms.add(new SPARQL_Pair(term, SPARQL_PairType.NB));
	}

	public void addPair(SPARQL_Term term, Object o, SPARQL_PairType t)
	{
		terms.add(new SPARQL_Pair(term, o, t));
	}
	
	public Set<SPARQL_Pair> getTerms(){
		return terms;
	}
	
	public boolean isAnd(){
		return and;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof SPARQL_Filter)) return false;

		SPARQL_Filter f = (SPARQL_Filter) obj;
		return f.toString().equals(toString());
	}

	@Override
	public String toString()
	{
		String retVal = "FILTER(";
		boolean first = true;
		for (SPARQL_Pair pair : terms)
		{
			if (!first)
				if (and)
					retVal += " && ";
				else 
					retVal += " || ";
			retVal += pair.toString();
		}
		retVal += ")";
		return retVal;
	}

}
