package org.dllearner.test.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.utilities.datastructures.StringTuple;

public class RuleExecutor {

	List<ReplacementRule> replacementRules = new ArrayList<ReplacementRule>();
	List<FilterRule> filterRule = new ArrayList<FilterRule>();
	
	public RuleExecutor() {
		super();
	}
	
	private boolean keepTuple(String subject, StringTuple tuple) {
		
		for (int i = 0; i < filterRule.size(); i++) {
			FilterRule fr = filterRule.get(i);
			if (!(fr.keepTuple(subject, tuple))) {
				return false;
			}
			
		}
		return true;
	}
	
	public SortedSet<StringTuple> filterTuples(String subject, SortedSet<StringTuple> tuples){
		SortedSet<StringTuple> returnSet = new TreeSet<StringTuple>();
		for (StringTuple tuple : tuples) {
			if(keepTuple(subject, tuple)) {
				returnSet.add(tuple);
			}
		}
		return returnSet;
	}
	
	public void addFilterRule(FilterRule fr){
		filterRule.add(fr);
	}
	
}
