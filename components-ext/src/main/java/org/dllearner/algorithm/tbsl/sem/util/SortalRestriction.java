package org.dllearner.algorithm.tbsl.sem.util;

public class SortalRestriction {

	String m_Referent;
	String m_Sort;
	
	public SortalRestriction() {	
	}
	public SortalRestriction(String ref,String sort) {
		m_Referent = ref;
		m_Sort = sort;
	}
	
	public String getReferent() {
		return m_Referent;
	}
	public String getSort() {
		return m_Sort;
	}
	
	public String toString() {
		return "(" + m_Referent + "," + m_Sort + ")";
	}
	
	public SortalRestriction clone() {
		return new SortalRestriction(m_Referent,m_Sort);
	}
	
	public void replaceReferent(String ref1,String ref2) {
		if (m_Referent.equals(ref1)) {
			m_Referent = ref2;
		}
	}
	
}
