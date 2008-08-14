package org.dllearner.kb.manipulator;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

public class TypeFilterRule extends Rule{
	
	String predicateFilter;
	String objectFilter;
	String canonicalClassName;


	public TypeFilterRule(Months month, String predicateFilter, String objectFilter, String canonicalClassName) {
		super(month);
		this.predicateFilter = predicateFilter;
		this.objectFilter = objectFilter;
		this.canonicalClassName = canonicalClassName;
	}
	

	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<RDFNodeTuple>();
		for (RDFNodeTuple tuple : tuples) {
			boolean remove = (tuple.aPartContains(predicateFilter) &&
					tuple.bPartContains(objectFilter) &&
					subject.getClass().getCanonicalName().equals(canonicalClassName));
			if(!remove){
				keep.add(tuple);
			}
		}
		return  keep;
	}
	

	/*
	if (t.a.equals(type) && t.b.equals(classns)
			&& node instanceof ClassNode) {
		toRemove.add(t);
	}

	// all with type class
	if (t.b.equals(classns) && node instanceof ClassNode) {
		toRemove.add(t);
	}

	// remove all instances with owl:type thing
	if (t.a.equals(type) && t.b.equals(thing)
			&& node instanceof InstanceNode) {
		toRemove.add(t);
	}
	*/
	
}
