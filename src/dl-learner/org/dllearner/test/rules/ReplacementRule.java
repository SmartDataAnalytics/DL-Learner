package org.dllearner.test.rules;

import org.dllearner.utilities.datastructures.StringTuple;

public abstract class ReplacementRule {

	public abstract StringTuple applyRule(String subject, StringTuple tuple);
}
