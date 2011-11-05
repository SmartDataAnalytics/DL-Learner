package org.dllearner.algorithm.tbsl.sparql;

import org.dllearner.algorithm.tbsl.sparql.SPARQL_Aggregate;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_OrderBy;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Term;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Value;

public class SPARQL_Term extends SPARQL_Value {
	
	SPARQL_OrderBy orderBy = SPARQL_OrderBy.NONE;
	SPARQL_Aggregate aggregate = SPARQL_Aggregate.NONE;
	boolean isURI = false; 
	String alias;
	
	public SPARQL_Term(String name) {
		super(name);
		this.name = name.replace("?","").replace("!","");
		alias = name;
	}
	public SPARQL_Term(String name, boolean uri) {
		super(name);
		this.name = name.replace("?","").replace("!","");
		isURI = uri;
		alias = name;
	}
	
	public SPARQL_Term(String name, SPARQL_Aggregate aggregate) {
		super(name);
		this.aggregate = aggregate;
		alias = name;
	}
	public SPARQL_Term(String name, SPARQL_Aggregate aggregate, String as) {
		super(name);
		this.aggregate = aggregate;
		alias = as;
	}
	
	public SPARQL_Term(String name, SPARQL_OrderBy ob) {
		super(name);
		orderBy = ob;
		alias = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SPARQL_Term)) return false;
		
		SPARQL_Term f = (SPARQL_Term) obj;
		return f.getName().toLowerCase().equals(this.getName().toLowerCase()) && f.getAggregate() == aggregate && f.getOrderBy() == orderBy;
	}

	public SPARQL_OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(SPARQL_OrderBy ob) {
		orderBy = ob;
	}

	public SPARQL_Aggregate getAggregate() {
		return aggregate;
	}

	public void setAggregate(SPARQL_Aggregate aggregate) {
		this.aggregate = aggregate;
	}
	
	public boolean isString()
	{
		return name.startsWith("'") || name.matches("\\d+");
	}
	
	public void setIsURI(boolean isURI){
		this.isURI = isURI;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
//		System.err.println("SPARQL_Term: name="+name+",alias="+alias+",agg="+aggregate+",orderBy="+orderBy); // DEBUG
		if (aggregate != SPARQL_Aggregate.NONE) {
			if (alias != null && !alias.equals(name))
				return "(" + aggregate+"(?"+name.toLowerCase()+") AS ?" + alias + ")";
			else 
				return aggregate+"(?"+name.toLowerCase()+")";
		}
		if (orderBy != SPARQL_OrderBy.NONE) {
			if (orderBy == SPARQL_OrderBy.ASC)
				return "ASC(?"+alias.toLowerCase()+")"; 
			else 
				return "DESC(?"+alias.toLowerCase()+")";
		}
		if (isString()) {
			return name.replaceAll("_"," ");
		}
		else if (isURI) {
			return name;
		}
		else return "?"+name.toLowerCase();
	}
	

}
