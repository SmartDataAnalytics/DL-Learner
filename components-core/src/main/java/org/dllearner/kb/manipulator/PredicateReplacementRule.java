package org.dllearner.kb.manipulator;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class PredicateReplacementRule extends Rule{
	
	String oldPredicate;
	String newPredicate;


	public PredicateReplacementRule(Months month, String oldPredicate, String newPredicate) {
		super(month);
		this.oldPredicate = oldPredicate;
		this.newPredicate = newPredicate;
	}
	
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<>();
		for (RDFNodeTuple tuple : tuples) {
			if(tuple.aPartContains(oldPredicate)){
				tuple.a = new ResourceImpl(newPredicate);
				logJamon();
			}
			keep.add(tuple);
		}
		return  keep;
	}

	@Override
	public void logJamon(){
		JamonMonitorLogger.increaseCount(PredicateReplacementRule.class, "replacedPredicates");
	}

	
	
}
