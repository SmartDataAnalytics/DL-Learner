package org.dllearner.test.rules;

import org.dllearner.utilities.datastructures.StringTuple;

public abstract class FilterRule {

	
	public abstract boolean keepTuple(String subject, StringTuple tuple);
	
}
