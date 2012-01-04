package org.dllearner.algorithm.tbsl.exploration.Sparql;

public class queryInformation {
	// <question id="32" type="boolean" fusion="false" aggregation="false" yago="false">
	public final String query;
	 public final String type;
	 public final boolean fusion;
	 public final boolean aggregation;
	 public final boolean yago;
	 public final String id;
	 public final String XMLtype;
	 public final boolean hint;
	 
	 public boolean isHint() {
		return hint;
	}


	public String getXMLtype() {
		return XMLtype;
	}

	 
	 public String getId() {
		return id;
	}
	public String getQuery() {
		return query;
	}
	public String getType() {
		return type;
	}
	public boolean isFusion() {
		return fusion;
	}
	public boolean isAggregation() {
		return aggregation;
	}
	public boolean isYago() {
		return yago;
	}
	
	public queryInformation(String query1, String id1, String type1, boolean fusion1, boolean aggregation1, boolean yago1, String XMLtype1, boolean hint1){
		this.query=query1;
		 this.type=type1;
		 this.fusion=fusion1;
		 this.aggregation=aggregation1;
		 this.yago=yago1;
		 this.id=id1;
		 this.XMLtype=XMLtype1;
		 this.hint=hint1;
	}
}
