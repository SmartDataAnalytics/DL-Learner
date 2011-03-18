package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Term extends SPARQL_Value {
	
	SPARQL_OrderBy orderBy;
	SPARQL_Aggregate aggregate;
	SPARQL_Term as = null;
	private boolean isVariable = false;
	
	public SPARQL_Term(String name) {
		super(name);
		this.name = name.replace("?","").replace("!","");
		orderBy = SPARQL_OrderBy.NONE;
		aggregate = SPARQL_Aggregate.NONE;
	}
	public SPARQL_Term(String name,boolean b) {
		super(name);
		this.name = name.replace("?","").replace("!","");
		orderBy = SPARQL_OrderBy.NONE;
		aggregate = SPARQL_Aggregate.NONE;
		isVariable = b;
	}
	
	public SPARQL_Term(String name, SPARQL_Aggregate aggregate) {
		super(name);
		this.aggregate = aggregate;
	}
	public SPARQL_Term(String name, SPARQL_Aggregate aggregate,boolean b,SPARQL_Term t) {
		super(name);
		this.aggregate = aggregate;
		isVariable = b;
		as = t;
	}
	
	public SPARQL_Term(String name, SPARQL_OrderBy orderBy) {
		super(name);
		this.orderBy = orderBy;
	}
	
	public void setIsVariable(boolean b) {
		isVariable = b;
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

	public void setOrderBy(SPARQL_OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public SPARQL_Aggregate getAggregate() {
		return aggregate;
	}

	public void setAggregate(SPARQL_Aggregate aggregate) {
		this.aggregate = aggregate;
	}
	
	public boolean isString()
	{
		return name.startsWith("'");
	}

	@Override
	public String toString() {
		if (aggregate != SPARQL_Aggregate.NONE) {
			if (as != null) {
				return aggregate+"(?"+name.toLowerCase()+") AS " + as.toString();
			}
			else {
				return aggregate+"(?"+name.toLowerCase()+")";
			}
		}
		if (orderBy != SPARQL_OrderBy.NONE) {
			if (orderBy == SPARQL_OrderBy.ASC)
				return "ASC(?"+name.toLowerCase()+")";
			else
				return "DESC(?"+name.toLowerCase()+")";
		}
		if (isVariable) {
			return "?"+name.toLowerCase();
		}
		else {
			return name;
		}
	}
	
	

}
