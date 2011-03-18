package org.dllearner.algorithm.tbsl.sem.drs;

import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sem.util.Label;

public interface DRS_Condition {

	// A DRS Condition can be: 
	// i) a predicate
	// ii) a negated DRS
	// iii) a complex condition
	
	public String toString();
	
	public void replaceReferent(String ref1, String ref2);
	public void replaceEqualRef(DiscourseReferent dr1, DiscourseReferent dr2, boolean isInUpperUniverse);
	
	public void replaceLabel(Label label1, Label label2);
	
	public Set<String> collectVariables();
	
	public Set<DRS_Condition> getEqualConditions();
	
	public boolean isComplexCondition();
	public boolean isNegatedCondition();
	
	public List<Label> getAllLabels();
	
	public DRS_Condition clone();
}
