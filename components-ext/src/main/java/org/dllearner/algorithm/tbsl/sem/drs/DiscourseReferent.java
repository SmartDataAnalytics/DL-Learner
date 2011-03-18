package org.dllearner.algorithm.tbsl.sem.drs;

public class DiscourseReferent {

	String m_Referent;
	boolean marked;
	boolean nonexistential;
	
	public DiscourseReferent(String referent)
	{
		m_Referent = referent;
		marked = false;
		nonexistential = false;
	}
	public DiscourseReferent(String s, boolean m, boolean e) {
		m_Referent = s;
		marked = m;
		nonexistential = e;
	}
	
	
	// get the value
	public String getValue()
	{
		return m_Referent;
	}
	// set the value
	public void setValue(String referent)
	{
		m_Referent = referent;
	}
	
	public boolean isMarked() {
		return marked;
	}
	public boolean isNonexistential() {
		return nonexistential;
	}
	
	public DiscourseReferent clone() {
		return new DiscourseReferent(m_Referent,marked,nonexistential);
	}
	
	// printing methods
	public String toString()
	{
		if (marked) {
			return "?" + m_Referent;
		}
		if (nonexistential) {
			return "!" + m_Referent;
		}
		return m_Referent;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_Referent == null) ? 0 : m_Referent.hashCode());
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
		DiscourseReferent other = (DiscourseReferent) obj;
		if (m_Referent == null) {
			if (other.m_Referent != null)
				return false;
		} else if (!m_Referent.equals(other.m_Referent))
			return false;
		return true;
	}

	

}
