package org.dllearner.algorithm.tbsl.sem.drs;

import java.util.*;

import org.dllearner.algorithm.tbsl.sem.util.Label;

public class Simple_DRS_Condition implements DRS_Condition{


	// A simple DRS condition consists of a predicate
	String m_Predicate;
	
	// as well as of a list of arguments
	List<DiscourseReferent> m_Arguments;
	
	public Simple_DRS_Condition() {
		m_Arguments = new ArrayList<DiscourseReferent>();	
	}
	
	public Simple_DRS_Condition(String predicate, List<DiscourseReferent> referents) {
		m_Predicate = predicate;
		m_Arguments = referents;	
	}
	
	public String toString()
	{
		String string ="(";
		DiscourseReferent dr;
		
		for (int i=0; i < m_Arguments.size(); i++)
		{
			dr = (DiscourseReferent) m_Arguments.get(i);
			if (i < m_Arguments.size()-1){
				string = string + dr.getValue() +",";
			}
			else
				string = string +dr.getValue();
		}
		
		string = string +")";
		
		return m_Predicate+string;	
	}
        public String toTex() {
            String out = "\\text{"+m_Predicate.replaceAll("\\_","\\\\_")+"}(";
            for (Iterator<DiscourseReferent> i = m_Arguments.iterator(); i.hasNext();) {
                out += i.next().toString();
                if (i.hasNext()) out += ",";
            }
            out += ")";
            return out;
        }

	// get methods
	
	// get the predicate
	public String getPredicate()
	{
		return m_Predicate;
	}
	
	public void setPredicate(String pred)
	{
		m_Predicate = pred;
	}
	
	// get the arguments
	public List<DiscourseReferent> getArguments() {
		return m_Arguments;
	}

	
	public boolean hasAsArgument(DiscourseReferent dr) {
		if (m_Arguments.contains(dr)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// add arguments
	public void addArgument(DiscourseReferent dr)
	{
		m_Arguments.add(dr);
	}
	
	// set arguments
	public void setArguments(List<DiscourseReferent> dr_set)
	{
		m_Arguments = dr_set;
	}


	public void replaceLabel(Label label1, Label label2) {
		// do nothing
	}

	public void replaceReferent(String ref1, String ref2) {
		Collections.replaceAll(m_Arguments,new DiscourseReferent(ref1),new DiscourseReferent(ref2));
	}

	public void replaceEqualRef(DiscourseReferent dr1, DiscourseReferent dr2, boolean isInUpperUniverse) {
		Collections.replaceAll(m_Arguments,dr1,dr2);
	}

	
	public Set<String> collectVariables() {
		
		Set<String> variables = new HashSet<String>();
		
		for ( DiscourseReferent ref : m_Arguments ) {
			variables.add(ref.m_Referent);
		}
		
		return variables;
	}
	
	public boolean isComplexCondition() { return false; }
	public boolean isNegatedCondition() { return false; }
	
	public List<Label> getAllLabels() { return(new ArrayList<Label>()); }

	public Set<DRS_Condition> getEqualConditions() {
	
		Set<DRS_Condition> out = new HashSet<DRS_Condition>();
		if (m_Predicate.equals("equal")) {
			out.add(this);
		}
		return out;
	}
	
	public DRS_Condition clone() {
		
		List<DiscourseReferent> newArgs = new ArrayList<DiscourseReferent>();
		for (DiscourseReferent dr : m_Arguments) {
			newArgs.add(dr);
		}
		return new Simple_DRS_Condition(m_Predicate,newArgs);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_Arguments == null) ? 0 : m_Arguments.hashCode());
		result = prime * result
				+ ((m_Predicate == null) ? 0 : m_Predicate.hashCode());
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
		Simple_DRS_Condition other = (Simple_DRS_Condition) obj;
		if (m_Arguments == null) {
			if (other.m_Arguments != null)
				return false;
		} else if (!m_Arguments.equals(other.m_Arguments))
			return false;
		if (m_Predicate == null) {
			if (other.m_Predicate != null)
				return false;
		} else if (!m_Predicate.equals(other.m_Predicate))
			return false;
		return true;
	}
	
	
}
