package org.dllearner.kb.manipulator;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.old.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

public class SimplePredicateFilterRule extends Rule{
	
	String predicateFilter;


	public SimplePredicateFilterRule(Months month, String predicateFilter) {
		super(month);
		this.predicateFilter = predicateFilter;
	}
	
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<RDFNodeTuple>();
		for (RDFNodeTuple tuple : tuples) {
			if(!tuple.aPartContains(predicateFilter)){
				keep.add(tuple);
			}
		}
		return  keep;
	}

	
	
	
}
