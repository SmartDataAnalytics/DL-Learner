package org.dllearner.autosparql.server;


public class QueryTreeChange {
	
	static enum ChangeType{
		REPLACE_LABEL,
		REMOVE_NODE;
	}
	
	private int nodeId;
	
	private ChangeType type;
	
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
