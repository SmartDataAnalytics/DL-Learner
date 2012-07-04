package org.dllearner.algorithm.tbsl.sem.drs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sem.util.Label;

public class Negated_DRS implements DRS_Condition {

	DRS m_DRS;
	
	public Negated_DRS() {	
	}
	
	Negated_DRS(DRS drs) {
		m_DRS = drs;
	}
	
	// set methods
	public void setDRS(DRS drs) {
		m_DRS = drs;
	}
	
	
	// get methods
	public DRS getDRS()
	{
		return m_DRS;
	}
	
	// printing methods
	
	public String toString()
	{
		return "NOT "+m_DRS;
	}
        public String toTex() {
            return "\\lnot "+m_DRS.toTex();
        }

	public void replaceLabel(Label label1, Label label2) {
		m_DRS.replaceLabel(label1, label2);
	}

	public void replaceReferent(String ref1, String ref2) {
		m_DRS.replaceReferent(ref1,ref2);
	}
	public void replaceEqualRef(DiscourseReferent dr1, DiscourseReferent dr2, boolean isInUpperUniverse) { 
		m_DRS.replaceEqualRef(dr1, dr2, isInUpperUniverse);
	}
	
	public Set<String> collectVariables() {
		return m_DRS.collectVariables();
	}
	
	public Set<DRS_Condition> getEqualConditions() {
		
		Set<DRS_Condition> out = new HashSet<DRS_Condition>();
		
		for (DRS_Condition c : m_DRS.m_DRS_Conditions) {
			out.addAll(c.getEqualConditions());
		}
		return out;
	}
	
	public boolean isComplexCondition() { return false; }
	public boolean isNegatedCondition() { return true; }
	
	public List<Label> getAllLabels() {
		List<Label> result = new ArrayList<Label>();
		result.add(m_DRS.getLabel());
		return result;
	}
	
	public DRS_Condition clone() {
		return (new Negated_DRS(m_DRS.clone()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_DRS == null) ? 0 : m_DRS.hashCode());
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
		Negated_DRS other = (Negated_DRS) obj;
		if (m_DRS == null) {
			if (other.m_DRS != null)
				return false;
		} else if (!m_DRS.equals(other.m_DRS))
			return false;
		return true;
	}
	
	
	
}
