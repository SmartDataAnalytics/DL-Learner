package org.dllearner.kb.manipulator;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

public class SimpleObjectFilterRule extends Rule{
	
	String objectFilter;

	public SimpleObjectFilterRule(Months month, String objectFilter) {
		super(month);
		this.objectFilter = objectFilter;
	}
	
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<RDFNodeTuple>();
		for (RDFNodeTuple tuple : tuples) {
			if(!tuple.bPartContains(objectFilter)){
				keep.add(tuple);
			}
		}
		return  keep;
	}

	/*
	private boolean keepTuple(Node subject, RDFNodeTuple tuple) {
		
		for (int i = 0; i < filterRules.size(); i++) {
			Rule fr = filterRules.get(i);
			if (!(fr.keepTuple(subject, tuple))) {
				return false;
			}
		}
		return true;
	}*/
	
	
	
}
