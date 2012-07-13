package org.dllearner.algorithm.tbsl.sem.drs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sem.util.Label;
import org.dllearner.algorithm.tbsl.sem.util.SemanticRepresentation;



public class DRS implements SemanticRepresentation {

	// A DRS has a set(!) of discourse referents
	Set<DiscourseReferent> m_DiscourseReferents;
	
	// A DRS has a set of conditions
	Set<DRS_Condition> m_DRS_Conditions;
	
	Label m_Label;

	
	public DRS()
	{
		m_DiscourseReferents = new HashSet<DiscourseReferent>();
		m_DRS_Conditions = new HashSet<DRS_Condition>();
	}
	
	public DRS(Label l, Set<DiscourseReferent> rs, Set<DRS_Condition> cs) {
		m_Label = l;
		m_DiscourseReferents = rs;
		m_DRS_Conditions = cs;
	}

	public void addDR(DiscourseReferent ref) {
		m_DiscourseReferents.add(ref);
	}

	public void addCondition(DRS_Condition condition) {
		m_DRS_Conditions.add(condition);
	}

	public void setLabel(Label label)
	{
		m_Label = label;
	}
	public void setLabel(String string)
	{
		m_Label = new Label(string);
	}
	
	public void setDiscourseReferents(Set<DiscourseReferent> dr_set)
	{
		m_DiscourseReferents = dr_set;
	}
	
	public void setDRSConditions(Set<DRS_Condition> conditions)
	{
		m_DRS_Conditions = conditions;
	}
	
	public DRS getComponent(Label label) {
		if (m_Label.equals(label)) {
			return this;
		}
		if (complexConditionOnly()) {
			DRS_Condition c = getConditionList().get(0);
			DRS scope = ((Complex_DRS_Condition) c).getScope().getComponent(label);
			DRS restr = ((Complex_DRS_Condition) c).getRestrictor().getComponent(label);
			if (scope != null) {
				return scope;
			}
			if (restr != null) {
				return restr;
			}
		}
		return null;
	}
	
	public String toString()
	{
		String string ="";
		
		if (m_Label != null) string = string += m_Label +":";
		
		string += "[";
		
		DiscourseReferent dr;
		DRS_Condition condition;
		
		for (Iterator<DiscourseReferent> i = m_DiscourseReferents.iterator(); i.hasNext();)
		{
			dr = (DiscourseReferent) i.next();
			 string = string + dr.toString();
			 if (i.hasNext()) string += ",";
		}
		
		string+= " | ";
		
		for (Iterator<DRS_Condition> i = m_DRS_Conditions.iterator(); i.hasNext();)
		{
			condition = (DRS_Condition) i.next();
			string = string + condition;
			if (i.hasNext()) string += ",";
		}
		
		string+= "]";
		
		return string;
	}
        public String toTex() {
            
                String string ="";
		if (m_Label != null) string = string += m_Label.toTex() + ":";
		string += "\\parbox{5cm}{\\Drs{";
		
		for (Iterator<DiscourseReferent> i = m_DiscourseReferents.iterator(); i.hasNext();) {
                    string += i.next().toString();
                    if (i.hasNext()) string += ",";
		}
		string += "}{";
                
		for (DRS_Condition cond : m_DRS_Conditions) {
                    string += cond.toTex() + " \\\\ ";
		}
		string+= "}}";
		
		return string;
        }

	public Label getLabel()
	{
		return m_Label;
	}
	
	public Label getScopeLabel() {
		
		Label result = m_Label;
		if (complexConditionOnly()) {
			for (DRS_Condition c : m_DRS_Conditions) {
				result = ((Complex_DRS_Condition) c).m_Scope.m_Label;
			}
		}
		return result;
	}
	
	public Label getResLabel() {
		
		Label result = m_Label;
		if (complexConditionOnly()) {
			for (DRS_Condition c : m_DRS_Conditions) {
				result = ((Complex_DRS_Condition) c).m_Restrictor.m_Label;
			}
		}
		return result;
	}
	
	public List<Label> getAllLabels() 
	{
		List<Label> result = new ArrayList<Label>();
	
		result.add(getLabel());
		for (DRS_Condition condition : m_DRS_Conditions) {
			result.addAll(condition.getAllLabels());
		}
		
		return result;
	}


	public Set<DiscourseReferent> getDRs() { 		
		return m_DiscourseReferents;
	}
	
	public Set<DiscourseReferent> getQmarkedDRs() {
		
		Set<DiscourseReferent> result = new HashSet<DiscourseReferent>();
		
		for (DiscourseReferent referent : m_DiscourseReferents) {
			if (referent.isMarked()) {
				result.add(referent);
			}
		}
		return result;
	}
	
	public Set<DiscourseReferent> collectDRs() {
		Set<DiscourseReferent> result = new HashSet<DiscourseReferent>();
		result.addAll(m_DiscourseReferents);
		for (DRS_Condition c : m_DRS_Conditions) {
			if (c.isComplexCondition()) {
				result.addAll(((Complex_DRS_Condition) c).m_Restrictor.collectDRs());
				result.addAll(((Complex_DRS_Condition) c).m_Scope.collectDRs());
			}
		}
		return result;
	}


	public Set<DRS_Condition> getConditions() {
		return m_DRS_Conditions;
	}
	public List<DRS_Condition> getConditionList() {		
		List<DRS_Condition> conditionList = new ArrayList<DRS_Condition>();
		for (DRS_Condition condition : m_DRS_Conditions) {
			conditionList.add(condition);
		}
		return conditionList;
	}
	public List<Simple_DRS_Condition> getAllSimpleConditions() {
		List<Simple_DRS_Condition> conditionList = new ArrayList<Simple_DRS_Condition>();
		for (DRS_Condition c : m_DRS_Conditions) {
			if (c.isComplexCondition()) {
				conditionList.addAll(((Complex_DRS_Condition) c).getScope().getAllSimpleConditions());
				conditionList.addAll(((Complex_DRS_Condition) c).getRestrictor().getAllSimpleConditions());
			}
			else if (c.isNegatedCondition()) {
				conditionList.addAll(((Negated_DRS) c).getDRS().getAllSimpleConditions());
			}
			else {
				conditionList.add(((Simple_DRS_Condition) c));
			}
		}
		return conditionList;
	}
	
	public Set<Simple_DRS_Condition> collectPredicates() {
		
		Set<Simple_DRS_Condition> predicates = new HashSet<Simple_DRS_Condition>();
		
		for (DRS_Condition condition : m_DRS_Conditions) {
			if (condition.isNegatedCondition()) {
				predicates.addAll(((Negated_DRS) condition).getDRS().collectPredicates());
			}
			else if (condition.isComplexCondition()) {
				predicates.addAll(((Complex_DRS_Condition) condition).getRestrictor().collectPredicates());
				predicates.addAll(((Complex_DRS_Condition) condition).getScope().collectPredicates());
			}
			else {
				predicates.add((Simple_DRS_Condition) condition);
			}
		}
		
		return predicates;
	}

	public void removeCondition(DRS_Condition condition) {
	
		m_DRS_Conditions.remove(condition);
		Set<DRS_Condition> donotkeep = new HashSet<DRS_Condition>();
		for (DRS_Condition c : m_DRS_Conditions) {
			if (c.equals(condition)) {
				donotkeep.add(c);
			}
			if (c.isNegatedCondition()) {
				((Negated_DRS) c).getDRS().removeCondition(condition);
			}
			else if (c.isComplexCondition()) {
				((Complex_DRS_Condition) c).getRestrictor().removeCondition(condition);
				((Complex_DRS_Condition) c).getScope().removeCondition(condition);
			}
		}
		
		// only because remove sometimes fails (for whatever mysterious reason)
		Set<DRS_Condition> newconditions = new HashSet<DRS_Condition>();
		for (DRS_Condition c : m_DRS_Conditions) {
			if (!c.equals(condition)) {
				newconditions.add(c);
			}
		}		
		m_DRS_Conditions = newconditions;
	}
	

	public void replaceReferent(String ref1, String ref2) { 
	
		DiscourseReferent found = null;
		for (DiscourseReferent dr : m_DiscourseReferents) {
			if (dr.m_Referent.equals(ref1)) {
				found = dr;
				break;
			}
		}
		if (found != null) {
			m_DiscourseReferents.remove(found);
			m_DiscourseReferents.add(new DiscourseReferent(ref2,found.marked,found.nonexistential));
		}

		for ( DRS_Condition condition : m_DRS_Conditions ) {
			condition.replaceReferent(ref1,ref2);
		}
	}
	
	public void replaceEqualRef(DiscourseReferent dr1, DiscourseReferent dr2, boolean isInUpperUniverse) { 
		
		boolean next = isInUpperUniverse;
		boolean marked = false; // TODO test!
		boolean nonex = false;
		DiscourseReferent found = null;
		for (DiscourseReferent dr : m_DiscourseReferents) {
			if (dr.m_Referent.equals(dr1.m_Referent)) { 
				found = dr;
				marked = found.marked;
				nonex = found.nonexistential;
				break;
			}
		}
		if (found != null) {
			m_DiscourseReferents.remove(found);
			next = true;
		}
		m_DiscourseReferents.remove(dr2);
		if (!isInUpperUniverse && !dr2.m_Referent.matches("[0-9]+")) {
			m_DiscourseReferents.add(new DiscourseReferent(dr2.m_Referent,marked,nonex));
		}

		for ( DRS_Condition condition : m_DRS_Conditions ) {
			condition.replaceEqualRef(dr1,dr2,next);
		}
	}

	public void replaceLabel(Label label1, Label label2) {
		if ( m_Label.equals(label1) ) {
			setLabel(label2);
		}
		for (DRS_Condition condition : m_DRS_Conditions) {
			condition.replaceLabel(label1,label2);
		}
	}

	public DRS clone()
	{
		DRS output = new DRS();
		
		output.setLabel(m_Label);
		
		for (DiscourseReferent ref : m_DiscourseReferents) {
			output.addDR(ref.clone());
		}
		for (DRS_Condition condition : m_DRS_Conditions) {
			output.addCondition(condition.clone());
		}
		
		return output;
	}

	public Set<String> collectVariables() {
		
		Set<String> variables = new HashSet<String>();
		
		for ( DiscourseReferent ref : m_DiscourseReferents ) {
			variables.add(ref.m_Referent);
		}
		for ( DRS_Condition condition : m_DRS_Conditions ) {
			variables.addAll(condition.collectVariables());
		}
		
		return variables;
	}


	public DRS merge(DRS drs) { // label of this projects
		
		DRS input = drs.clone();
		DRS output = clone();
		
		output.getDRs().addAll(input.getDRs());
		output.getConditions().addAll(input.getConditions());
		
		return output;
	}
	public DRS merge(DRS drs,Label label) { // label projects
		
		DRS input = drs.clone();
		DRS output = clone();
		
		output.getDRs().addAll(input.getDRs());
		output.getConditions().addAll(input.getConditions());
		
		output.setLabel(label);
		
		return output;
	}
	
	public DRS mergeIn(DRS drs,Label loserLabel,Label winnerLabel) {
		
		Label topLabel = getLabel();
		DRS result;
		
		if ( loserLabel.equals(topLabel) ) { 
			result = merge(drs,winnerLabel); 
		}
		else { // TODO: first all proper names to top
			for (DRS_Condition condition : getConditions()) {
				if ( condition.isNegatedCondition() ) { 
					DRS subDRS = ((Negated_DRS) condition).getDRS();
					((Negated_DRS) condition).setDRS(subDRS.mergeIn(drs,loserLabel,winnerLabel));
				}
				else if ( condition.isComplexCondition() ) {
					DRS restrictor = ((Complex_DRS_Condition) condition).getRestrictor();
					DRS scope = ((Complex_DRS_Condition) condition).getScope();
					((Complex_DRS_Condition) condition).setRestrictor(restrictor.mergeIn(drs,loserLabel,winnerLabel));
					((Complex_DRS_Condition) condition).setScope(scope.mergeIn(drs,loserLabel,winnerLabel));
					}
				}
			result = this;
			}

		return result;
	}
	
	public boolean complexConditionOnly() {
		
		List<DRS_Condition> conditionList = getConditionList();
		
		if ( conditionList.size()==1 && conditionList.get(0).isComplexCondition() ) {
			return true;
		}
		else { 
			return false; 
		}
	}

 

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((m_DRS_Conditions == null) ? 0 : m_DRS_Conditions.hashCode());
		result = prime
				* result
				+ ((m_DiscourseReferents == null) ? 0 : m_DiscourseReferents
						.hashCode());
		result = prime * result + ((m_Label == null) ? 0 : m_Label.hashCode());
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
		DRS other = (DRS) obj;
		if (m_DRS_Conditions == null) {
			if (other.m_DRS_Conditions != null)
				return false;
		} else if (!m_DRS_Conditions.equals(other.m_DRS_Conditions))
			return false;
		if (m_DiscourseReferents == null) {
			if (other.m_DiscourseReferents != null)
				return false;
		} else if (!m_DiscourseReferents.equals(other.m_DiscourseReferents))
			return false;
		if (m_Label == null) {
			if (other.m_Label != null)
				return false;
		} else if (!m_Label.equals(other.m_Label))
			return false;
		return true;
	}
	
	
	public boolean equalsModuloLabel(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DRS other = (DRS) obj;
		if (m_DRS_Conditions == null) {
			if (other.m_DRS_Conditions != null)
				return false;
		} else if (!m_DRS_Conditions.equals(other.m_DRS_Conditions))
			return false;
		if (m_DiscourseReferents == null) {
			if (other.m_DiscourseReferents != null)
				return false;
		} else if (!m_DiscourseReferents.equals(other.m_DiscourseReferents))
			return false;
		return true;
	}

	public boolean equalsModuloRenaming(DRS drs) {
			
		DRS drs1 = this.clone();
		DRS drs2 = drs.clone();
		
		drs1.setLabel("l1"); drs2.setLabel("l1");		
		if (drs1.equals(drs2)) { return true; }
		
		Set<String> thisVars = drs1.collectVariables();
		Set<String> drsVars = drs2.collectVariables();
		
		if (thisVars.size() != drsVars.size()) {
			return false;
		}

		List<String> thisVarsR = new ArrayList<String>();
		List<String> drsVarsR = new ArrayList<String>();
		for (String s : thisVars) {
			if (!drsVars.contains(s)) {
				thisVarsR.add(s);
			}
		}
		for (String s : drsVars) {
			if (!thisVars.contains(s)) {
				drsVarsR.add(s);
			}
		}
		
		String oldV; String newV;
		for (int i=0; i < thisVarsR.size(); i++) {
			oldV = thisVarsR.get(i);
			newV = drsVarsR.get(i);
			drs1.replaceReferent(oldV,newV);
		}
		
		// If it looks the same, it is the same.
		
		DRS_Constructor dc = new DRS_Constructor();
		if (dc.construct(drs1.toString()).equals(dc.construct(drs2.toString()))) {
			return true;
		} 
		else {
			return false;
		}
		
	}

}
