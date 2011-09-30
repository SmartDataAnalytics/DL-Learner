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
	
	//copy constructor
	public Query(Query query){
		Set<SPARQL_Term> selTerms = new HashSet<SPARQL_Term>();
		for(SPARQL_Term term : query.getSelTerms()){
			SPARQL_Term newTerm = new SPARQL_Term(term.getName());
			newTerm.setIsVariable(term.isVariable());
			newTerm.setIsURI(newTerm.isURI);
			newTerm.setAggregate(term.getAggregate());
			newTerm.setOrderBy(term.getOrderBy());
			selTerms.add(newTerm);
		}
		this.selTerms = selTerms;
		Set<SPARQL_Prefix> prefixes = new HashSet<SPARQL_Prefix>();
		for(SPARQL_Prefix prefix : query.getPrefixes()){
			SPARQL_Prefix newPrefix = new SPARQL_Prefix(prefix.getName(), prefix.getUrl());
			prefixes.add(newPrefix);
		}
		this.prefixes = prefixes;
		Set<SPARQL_Triple> conditions = new HashSet<SPARQL_Triple>();
		for(SPARQL_Triple condition : query.getConditions()){
			SPARQL_Term variable = new SPARQL_Term(condition.getVariable().getName());
			variable.setIsVariable(condition.getVariable().isVariable());
			variable.setIsURI(condition.getVariable().isURI);
			SPARQL_Property property = new SPARQL_Property(condition.getProperty().getName());
			property.setIsVariable(condition.getProperty().isVariable());
			property.setPrefix(condition.getProperty().getPrefix());
			SPARQL_Term value = new SPARQL_Term(condition.getValue().getName());
			if(condition.getValue() instanceof SPARQL_Term){
				value.setIsURI(((SPARQL_Term)condition.getValue()).isURI);
			}
			value.setIsVariable(condition.getValue().isVariable());
			SPARQL_Triple newCondition = new SPARQL_Triple(variable, property, value);
			conditions.add(newCondition);
		}
		this.conditions = conditions;
		Set<SPARQL_Term> orderBy = new HashSet<SPARQL_Term>();
		for(SPARQL_Term order : query.getOrderBy()){
			SPARQL_Term newTerm = new SPARQL_Term(order.getName());
			newTerm.setIsVariable(order.isVariable());
			newTerm.setAggregate(order.getAggregate());
			newTerm.setOrderBy(order.getOrderBy());
			orderBy.add(newTerm);
		}
		this.orderBy = orderBy;
		//TODO add copy for filters
		Set<SPARQL_Filter> filters = new HashSet<SPARQL_Filter>();
		for(SPARQL_Filter filter : query.getFilters()){
			for(SPARQL_Pair term : filter.getTerms()){
				
			}
		}
		this.filter = filters;
		
		this.limit = query.getLimit();
		this.offset = query.getOffset();
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
	
	public Set<SPARQL_Filter> getFilters(){
		return filter;
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
	
	public void replaceVarWithURI(String var, String uri){
		SPARQL_Term subject;
		SPARQL_Property property;
		SPARQL_Value object;
		uri = "<" + uri + ">";
		
		for(SPARQL_Triple triple : conditions){
			subject = triple.getVariable();
			property = triple.getProperty();
			object = triple.getValue();
			if(subject.isVariable()){
				if(subject.getName().equals(var)){
					subject.setName(uri);
					subject.setIsVariable(false);
					subject.setIsURI(true);
				}
			}
			if(property.isVariable()){
				if(property.getName().equals(var)){
					property.setName(uri);
					property.setIsVariable(false);
				}
			}
			if(object.isVariable()){
				if(object.getName().equals(var)){
					object.setName(uri);
					object.setIsVariable(false);
					if(object instanceof SPARQL_Term){
						((SPARQL_Term) object).setIsURI(true);
					}
				}
			}
			
		}
	}
	
	public void replaceVarWithPrefixedURI(String var, String uri){
		SPARQL_Term subject;
		SPARQL_Property property;
		SPARQL_Value object;
		
		for(SPARQL_Triple triple : conditions){
			subject = triple.getVariable();
			property = triple.getProperty();
			object = triple.getValue();
			if(subject.isVariable()){
				if(subject.getName().equals(var)){
					subject.setName(uri);
					subject.setIsVariable(false);
					subject.setIsURI(true);
				}
			}
			if(property.isVariable()){
				if(property.getName().equals(var)){
					property.setName(uri);
					property.setIsVariable(false);
				}
			}
			if(object.isVariable()){
				if(object.getName().equals(var)){
					object.setName(uri);
					object.setIsVariable(false);
					if(object instanceof SPARQL_Term){
						((SPARQL_Term) object).setIsURI(true);
					}
				}
			}
			
		}
	}
	
	public List<SPARQL_Triple> getTriplesWithVar(String var){
		List<SPARQL_Triple> triples = new ArrayList<SPARQL_Triple>();
		
		SPARQL_Term variable;
		SPARQL_Property property;
		SPARQL_Value value;
		for(SPARQL_Triple triple : conditions){
			variable = triple.getVariable();
			property = triple.getProperty();
			value = triple.getValue();
			
			if(variable.isVariable() && variable.getName().equals(var)){
				triples.add(triple);
			} else if(property.isVariable() && property.getName().equals(var)){
				triples.add(triple);
			} else if(value.isVariable() && value.getName().equals(var)){
				triples.add(triple);
			}
		}
		return triples;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conditions == null) ? 0 : conditions.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + limit;
		result = prime * result + offset;
		result = prime * result + ((orderBy == null) ? 0 : orderBy.hashCode());
		result = prime * result
				+ ((prefixes == null) ? 0 : prefixes.hashCode());
		result = prime * result + ((qt == null) ? 0 : qt.hashCode());
		result = prime * result
				+ ((selTerms == null) ? 0 : selTerms.hashCode());
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
		Query other = (Query) obj;
		if (conditions == null) {
			if (other.conditions != null)
				return false;
		} else if (!conditions.equals(other.conditions))
			return false;
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
		return true;
	}
	
	/**
	 * Returns the variable in the SPARQL query, which determines the type of the answer
	 * by an rdf:type property.
	 * @return
	 */
	public String getAnswerTypeVariable(){
		SPARQL_Term selection = selTerms.iterator().next();
		for(SPARQL_Triple t : conditions){
			if(t.getVariable().equals(selection) && t.getProperty().getName().equals("type")){
				return t.getValue().getName();
			}
		}
		return null;
	}

}
