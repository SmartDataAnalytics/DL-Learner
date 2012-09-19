package org.dllearner.algorithm.tbsl.sem.drs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sem.util.Label;

public class Complex_DRS_Condition implements DRS_Condition {

	DRS_Quantifier m_Quantifier;

	DiscourseReferent m_Referent;

	DRS m_Restrictor;

	DRS m_Scope;

	// constructors

	// trivial constructor
	public Complex_DRS_Condition() {

	}

	// full constructors
	public Complex_DRS_Condition(DRS_Quantifier quantifier,DiscourseReferent referent, DRS restrictor, DRS scope) {
		m_Quantifier = quantifier;
		m_Referent = referent;
		m_Restrictor = restrictor;
		m_Scope = scope;
	}

	// set methods

	public void setQuantifier(DRS_Quantifier quantifier) {
		m_Quantifier = quantifier;
	}

	public void setRestrictor(DRS restrictor) {
		m_Restrictor = restrictor;
	}

	public void setScope(DRS scope) {
		m_Scope = scope;
	}

	public void setReferent(DiscourseReferent referent) {
		m_Referent = referent;
	}

	// get methods

	public DRS getRestrictor() {
		return m_Restrictor;
	}

	public DRS getScope() {
		return m_Scope;
	}

	public DRS_Quantifier getQuantifier() {
		return m_Quantifier;
	}

	public DiscourseReferent getReferent() {
		return m_Referent;
	}

	public String toString() {

            String out = m_Restrictor + " " + m_Quantifier + " ";
            if (!m_Referent.m_Referent.equals("null")) out += m_Referent + " ";
            out += m_Scope + "\n";

            return out;
	}
        public String toTex() {
            
            String out = m_Restrictor.toTex() + " \\langle " + m_Quantifier + " ";
            if (!m_Referent.m_Referent.equals("null")) out += m_Referent;
            out += " \\rangle " + m_Scope.toTex(); 
            
            return out;
        }

	public void replaceLabel(Label label1, Label label2) {
		m_Restrictor.replaceLabel(label1, label2);
		m_Scope.replaceLabel(label1, label2);
	}

	public void replaceReferent(String ref1, String ref2) {
		if (m_Referent.toString().equals(ref1)) {
			setReferent(new DiscourseReferent(ref2));
		}
		m_Restrictor.replaceReferent(ref1, ref2);
		m_Scope.replaceReferent(ref1, ref2);
	}
	public void replaceEqualRef(DiscourseReferent dr1, DiscourseReferent dr2, boolean isInUpperUniverse) { 
		if (m_Referent.equals(dr1.m_Referent)) {
			setReferent(dr2);
		}
		m_Restrictor.replaceEqualRef(dr1, dr2, isInUpperUniverse);
		m_Scope.replaceEqualRef(dr1, dr2, isInUpperUniverse);
	}

	public Set<String> collectVariables() {

		Set<String> variables = new HashSet<String>();

		if (!m_Referent.m_Referent.equals("null")) variables.add(m_Referent.m_Referent);
		variables.addAll(m_Restrictor.collectVariables());
		variables.addAll(m_Scope.collectVariables());

		return variables;
	}

	public boolean isComplexCondition() {
		return true;
	}

	public boolean isNegatedCondition() {
		return false;
	}

	public List<Label> getAllLabels() {
		List<Label> result = new ArrayList<Label>();
		result.addAll(m_Restrictor.getAllLabels());
		result.addAll(m_Scope.getAllLabels());
		return result;
	}
	
	public Set<DRS_Condition> getEqualConditions() {
		
		Set<DRS_Condition> out = new HashSet<DRS_Condition>();
		
		for (DRS_Condition c : m_Restrictor.getConditions()) {
			out.addAll(c.getEqualConditions());
		}
		for (DRS_Condition c: m_Scope.getConditions()) {
			out.addAll(c.getEqualConditions());
		}
		
		return out;
	}

	public DRS_Condition clone() {
		return (new Complex_DRS_Condition(m_Quantifier, m_Referent, m_Restrictor.clone(), m_Scope.clone()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_Quantifier == null) ? 0 : m_Quantifier.hashCode());
		result = prime * result
				+ ((m_Referent == null) ? 0 : m_Referent.hashCode());
		result = prime * result
				+ ((m_Restrictor == null) ? 0 : m_Restrictor.hashCode());
		result = prime * result + ((m_Scope == null) ? 0 : m_Scope.hashCode());
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
		Complex_DRS_Condition other = (Complex_DRS_Condition) obj;
		if (m_Quantifier == null) {
			if (other.m_Quantifier != null)
				return false;
		} else if (!m_Quantifier.equals(other.m_Quantifier))
			return false;
		if (m_Referent == null) {
			if (other.m_Referent != null)
				return false;
		} else if (!m_Referent.equals(other.m_Referent))
			return false;
		if (m_Restrictor == null) {
			if (other.m_Restrictor != null)
				return false;
		} else if (!m_Restrictor.equals(other.m_Restrictor))
			return false;
		if (m_Scope == null) {
			if (other.m_Scope != null)
				return false;
		} else if (!m_Scope.equals(other.m_Scope))
			return false;
		return true;
	}

}
