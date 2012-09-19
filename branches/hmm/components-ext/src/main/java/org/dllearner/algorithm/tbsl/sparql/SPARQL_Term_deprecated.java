package org.dllearner.algorithm.tbsl.sparql;

public class SPARQL_Term_deprecated extends SPARQL_Value {
	
	SPARQL_OrderBy orderBy = SPARQL_OrderBy.NONE;
	SPARQL_Aggregate aggregate = SPARQL_Aggregate.NONE;
	SPARQL_Term_deprecated as = null;
	
	public SPARQL_Term_deprecated(String name) {
		super(name);
		this.name = name.replace("?","").replace("!","");
	}
	public SPARQL_Term_deprecated(String name,boolean b) {
		super(name);
		this.name = name.replace("?","").replace("!","");
		setIsVariable(b);
	}
	
	public SPARQL_Term_deprecated(String name, SPARQL_Aggregate aggregate) {
		super(name);
		this.aggregate = aggregate;
	}
	public SPARQL_Term_deprecated(String name, SPARQL_Aggregate aggregate,boolean b,SPARQL_Term_deprecated t) {
		super(name);
		this.aggregate = aggregate;
		setIsVariable(b);
		as = t;
	}
	
	public SPARQL_Term_deprecated(String name, SPARQL_OrderBy orderBy) {
		super(name);
		this.orderBy = orderBy;
	}
	public SPARQL_Term_deprecated(String name, SPARQL_OrderBy orderBy,boolean b,SPARQL_Term_deprecated t) {
		super(name);
		this.orderBy = orderBy;
		setIsVariable(b);
		as = t;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SPARQL_Term_deprecated)) return false;
		
		SPARQL_Term_deprecated f = (SPARQL_Term_deprecated) obj;
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
			String n;
			if (as != null) { n = as.name; } else { n = name; }
			if (orderBy == SPARQL_OrderBy.ASC)
				return "ASC(?"+n.toLowerCase()+")";
			else
				return "DESC(?"+n.toLowerCase()+")";
		}
		if (isVariable() && !isString()) {
			return "?"+name.toLowerCase();
		}
		else {
			return name;
		}
	}
	
	

}
