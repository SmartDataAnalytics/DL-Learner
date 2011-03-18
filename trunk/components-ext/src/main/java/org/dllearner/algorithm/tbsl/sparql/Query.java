package org.dllearner.algorithm.tbsl.sparql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Query
{

	Set<SPARQL_Term> selTerms; // SELECT ?x ?y
	Set<SPARQL_Prefix> prefixes;
	Set<SPARQL_Triple> conditions;
	Set<SPARQL_Term> orderBy;
	Set<SPARQL_Filter> filter;
	SPARQL_QueryType qt = SPARQL_QueryType.SELECT;

	int limit;
	int offset;

	public Query()
	{
		super();
		selTerms = new HashSet<SPARQL_Term>();
		prefixes = new HashSet<SPARQL_Prefix>();
		conditions = new HashSet<SPARQL_Triple>();
		orderBy = new HashSet<SPARQL_Term>();
		filter = new HashSet<SPARQL_Filter>();
	}

	public Query(Set<SPARQL_Term> selTerms, Set<SPARQL_Prefix> prefixes, Set<SPARQL_Triple> conditions)
	{
		super();
		this.selTerms = selTerms;
		this.prefixes = prefixes;
		this.conditions = conditions;
	}

	public Query(Set<SPARQL_Term> selTerms, Set<SPARQL_Prefix> prefixes, Set<SPARQL_Triple> conditions, Set<SPARQL_Term> orderBy, int limit, int offset)
	{
		super();
		this.selTerms = selTerms;
		this.prefixes = prefixes;
		this.conditions = conditions;
		this.orderBy = orderBy;
		this.limit = limit;
		this.offset = offset;
	}

	public Set<Integer> getSlotInts() {
		
		Set<Integer> result = new HashSet<Integer>(); 
		
		String name;
		int i;
		
		for (SPARQL_Triple triple : conditions) {
			
			name = triple.variable.getName();
			if (name.matches("s[0-9]+")) {
				i = Integer.parseInt(name.substring(name.indexOf("s") + 1));
				result.add(i);
			}
			
			name = triple.value.getName();
			if (name.matches("s[0-9]+")) {
				i = Integer.parseInt(name.substring(name.indexOf("s") + 1));
				result.add(i);
			}
			
			name = triple.property.getName();
			if (name.matches("s[0-9]+")) {
				i = Integer.parseInt(name.substring(name.indexOf("s") + 1));
				result.add(i);
			}			
		}
		
		
		
		return result;
	}
	
	@Override
	public String toString()
	{

		String retVal = "";
		for (SPARQL_Prefix prefix : prefixes)
		{
			retVal += prefix.toString() + "\n";
		}

		if (qt == SPARQL_QueryType.SELECT)
		{
			retVal += "\nSELECT ";

			for (SPARQL_Term term : selTerms)
			{
				retVal += term.toString() + " ";
			}
		}
		else retVal += "\nASK ";

		retVal += "WHERE {\n";

		if (conditions == null || conditions.size() == 0) return "ERROR";
		for (SPARQL_Triple condition : conditions)
		{
			if (condition == null) continue;
			retVal += "\t" + condition.toString() + " .\n";
		}

		for (SPARQL_Filter f : filter)
		{
			retVal += "\t" + f.toString() + " .\n";
		}

		retVal += "}\n";

		if (orderBy != null && !orderBy.isEmpty())
		{
			retVal += "ORDER BY ";
			for (SPARQL_Term term : orderBy)
			{
				retVal += term.toString() + " ";
			}
			retVal += "\n";
		}

		if (limit != 0 || offset != 0)
		{
			retVal += "LIMIT " + limit + " OFFSET " + offset + "\n";
		}

		return retVal;

	}

	public List<String> getVariablesAsStringList()
	{
		List<String> result = new ArrayList<String>();
		for (SPARQL_Term term : selTerms)
		{
			result.add(term.toString());
		}
		return result;
	}

	public Set<SPARQL_Term> getSelTerms()
	{
		return selTerms;
	}

	public void setSelTerms(Set<SPARQL_Term> selTerms)
	{
		this.selTerms = selTerms;
	}

	public Set<SPARQL_Prefix> getPrefixes()
	{
		return prefixes;
	}

	public void setPrefixes(Set<SPARQL_Prefix> prefixes)
	{
		this.prefixes = prefixes;
	}

	public Set<SPARQL_Triple> getConditions()
	{
		return conditions;
	}

	public void setConditions(Set<SPARQL_Triple> conditions)
	{
		this.conditions = conditions;
	}

	public void addCondition(SPARQL_Triple triple)
	{
		conditions.add(triple);
	}

	public void addFilter(SPARQL_Filter f)
	{
		for (int i = 0; i < filter.size(); ++i)
			if (f.equals(filter.toArray()[i])) return;

		this.filter.add(f);
	}

	public Set<SPARQL_Term> getOrderBy()
	{
		return orderBy;
	}

	public void addOrderBy(SPARQL_Term term)
	{
		if (term.orderBy == SPARQL_OrderBy.NONE)
			term.orderBy = SPARQL_OrderBy.ASC;
		
		orderBy.add(term);
	}

	public void addPrefix(SPARQL_Prefix prefix)
	{
		prefixes.add(prefix);
	}

	public void addSelTerm(SPARQL_Term term)
	{
		for (int i = 0; i < selTerms.size(); ++i)
			if (term.equals(selTerms.toArray()[i])) return;

		selTerms.add(term);
	}

	public boolean isSelTerm(SPARQL_Term term)
	{
		for (int i = 0; i < selTerms.size(); ++i) // TODO: have to figure out
													// while .remove doesn't
													// call .equals
		{
			if (term.equals(selTerms.toArray()[i])) return true;
		}
		return false;
	}

	public void removeSelTerm(SPARQL_Term term)
	{
		Set<SPARQL_Term> newSelTerms = new HashSet<SPARQL_Term>();
		for (int i = 0; i < selTerms.size(); ++i) // TODO: have to figure out
													// while .remove doesn't
													// call .equals
		{
			if (!term.equals(selTerms.toArray()[i])) newSelTerms.add((SPARQL_Term) selTerms.toArray()[i]);
		}
		selTerms = newSelTerms;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public SPARQL_QueryType getQt()
	{
		return qt;
	}

	public void setQt(SPARQL_QueryType qt)
	{
		this.qt = qt;
	}

}
