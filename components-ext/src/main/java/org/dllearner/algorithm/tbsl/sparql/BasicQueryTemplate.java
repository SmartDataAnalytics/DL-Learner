package org.dllearner.algorithm.tbsl.sparql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicQueryTemplate
{

	Set<SPARQL_Term> selTerms; // SELECT ?x ?y
	Set<SPARQL_Prefix> prefixes;
	Set<Path> conditions;
	Set<SPARQL_Term> orderBy;
	Set<SPARQL_Filter> filter;
	SPARQL_QueryType qt = SPARQL_QueryType.SELECT;
	List<Slot> slots;

	int limit;
	int offset;

	public BasicQueryTemplate()
	{
		super();
		selTerms   = new HashSet<SPARQL_Term>();
		prefixes   = new HashSet<SPARQL_Prefix>();
		conditions = new HashSet<Path>();
		orderBy    = new HashSet<SPARQL_Term>();
		filter     = new HashSet<SPARQL_Filter>();
		slots      = new ArrayList<Slot>();
	}
	
	public void addSlot(Slot s) {
		slots.add(s);
	}
	
	public void addConditions(Path p) {
		conditions.add(p);
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
		
		for (Path p : conditions) {
			retVal += "\t" + p.toString() + "\n";
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

		retVal += "\n";

		for (Slot s : slots) {
			retVal += s.toString() + "\n";
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
	
	public Set<SPARQL_Filter> getFilters(){
		return filter;
	}
	
	public Set<Path> getConditions() {
		return conditions;
	}

	public void setPrefixes(Set<SPARQL_Prefix> prefixes)
	{
		this.prefixes = prefixes;
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

	public List<Slot> getSlots(){
		return slots;
	}

	public SPARQL_QueryType getQt()
	{
		return qt;
	}

	public void setQt(SPARQL_QueryType qt)
	{
		this.qt = qt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + limit;
		result = prime * result + offset;
		result = prime * result + ((orderBy == null) ? 0 : orderBy.hashCode());
		result = prime * result
				+ ((prefixes == null) ? 0 : prefixes.hashCode());
		result = prime * result + ((qt == null) ? 0 : qt.hashCode());
		result = prime * result
				+ ((selTerms == null) ? 0 : selTerms.hashCode());
		result = prime * result + ((slots == null) ? 0 : slots.hashCode());
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
		BasicQueryTemplate other = (BasicQueryTemplate) obj;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		if (limit != other.limit)
			return false;
		if (offset != other.offset)
			return false;
		if (orderBy == null) {
			if (other.orderBy != null)
				return false;
		} else if (!orderBy.equals(other.orderBy))
			return false;
		if (prefixes == null) {
			if (other.prefixes != null)
				return false;
		} else if (!prefixes.equals(other.prefixes))
			return false;
		if (qt == null) {
			if (other.qt != null)
				return false;
		} else if (!qt.equals(other.qt))
			return false;
		if (selTerms == null) {
			if (other.selTerms != null)
				return false;
		} else if (!selTerms.equals(other.selTerms))
			return false;
		if (slots == null) {
			if (other.slots != null)
				return false;
		} else if (!slots.equals(other.slots))
			return false;
		return true;
	}

}
