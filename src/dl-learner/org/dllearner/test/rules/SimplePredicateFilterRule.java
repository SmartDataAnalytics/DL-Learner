package org.dllearner.test.rules;

import org.dllearner.utilities.datastructures.StringTuple;

public class SimplePredicateFilterRule extends FilterRule{
	
	String predicateFilter;


	public SimplePredicateFilterRule(String predicateFilter) {
		super();
		this.predicateFilter = predicateFilter;
	}
	
	@Override
	public boolean keepTuple(String subject, StringTuple tuple) {
		return !(tuple.a.contains(predicateFilter));
	}

	
	
	
}
