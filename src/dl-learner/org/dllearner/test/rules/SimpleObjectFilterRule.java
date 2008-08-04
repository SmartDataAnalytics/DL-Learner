package org.dllearner.test.rules;

import org.dllearner.utilities.datastructures.StringTuple;

public class SimpleObjectFilterRule extends FilterRule{
	
	String objectFilter;

	public SimpleObjectFilterRule(String objectFilter) {
		super();
		this.objectFilter = objectFilter;
	}
	
	
	@Override
	public boolean keepTuple(String subject, StringTuple tuple) {
		return !(tuple.b.contains(objectFilter));
	}

	
	
	
}
