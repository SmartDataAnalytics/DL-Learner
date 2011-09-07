package org.dllearner.algorithm.tbsl.sparql;


public class BasicSlot {

	String anchor;
	String token;
	SlotType type;
	
	public BasicSlot(String a,String t) {
		anchor = a;
		token = t;
		type = SlotType.UNSPEC; 
		replaceUnderscores();
	}
	public BasicSlot(String a,SlotType t,String s) {
		anchor = a;
		token = s;
		type = t;
		replaceUnderscores();
	}
	
	public void setSlotType(SlotType st) {
		type = st;
	}
	
	public SlotType getSlotType(){
		return type;
	}
	
	public String getAnchor() {
		return anchor;
	}
	public void setAnchor(String s) {
		anchor = s;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String t) {
		token = t;
	}
	
	public void replaceReferent(String ref1,String ref2) {
		if (anchor.equals(ref1)) {
			anchor = ref2;
		}
	}
	
	public void replaceUnderscores() {
		token = token.replaceAll("_"," ");
	}
	
	@Override
	public String toString() {	
		return anchor + ": " + type + " {" + token + "}";
	}
	@Override
	public BasicSlot clone() {
		return new BasicSlot(anchor,type,token);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anchor == null) ? 0 : anchor.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		BasicSlot other = (BasicSlot) obj;
		if (anchor == null) {
			if (other.anchor != null)
				return false;
		} else if (!anchor.equals(other.anchor))
			return false;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
	
}
