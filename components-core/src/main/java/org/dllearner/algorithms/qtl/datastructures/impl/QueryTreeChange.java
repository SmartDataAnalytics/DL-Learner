package org.dllearner.algorithms.qtl.datastructures.impl;



public class QueryTreeChange {
	
	public enum ChangeType{
		REPLACE_LABEL,
		REMOVE_NODE;
	}
	
	private int nodeId;
	
	private ChangeType type;
	
	private String object;
	private String edge;
	
	public QueryTreeChange(int nodeId, ChangeType type){
		this.nodeId = nodeId;
		this.type = type;
	}
	
	public int getNodeId() {
		return nodeId;
	}

	public ChangeType getType() {
		return type;
	}
	
	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getEdge() {
		return edge;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}

	@Override
	public String toString() {
//		return "nodeId" + (type==ChangeType.REPLACE_LABEL ? "Replace" : "Remove");
		return nodeId + (type==ChangeType.REPLACE_LABEL ? "a" : "b");
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true;
		}
		if(obj == null || !(obj instanceof QueryTreeChange)){
			return false;
		}
		QueryTreeChange other = (QueryTreeChange)obj;
		return nodeId == other.getNodeId() && type == other.getType();
	}
	
	@Override
	public int hashCode() {
		return nodeId + type.hashCode() + 37;
	}


}
