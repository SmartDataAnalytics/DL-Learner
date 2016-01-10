package org.dllearner.kb.manipulator;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class ObjectReplacementRule extends Rule{
	
	String oldObject;
	String newObject;


	public ObjectReplacementRule(Months month, String oldObject, String newObject) {
		super(month);
		this.oldObject = oldObject;
		this.newObject = newObject;
	}
	
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<>();
		for (RDFNodeTuple tuple : tuples) {
			if(tuple.bPartContains(oldObject)){
				String tmp = tuple.b.toString().replace(oldObject, newObject);
				tuple.b = new ResourceImpl(tmp);
				JamonMonitorLogger.increaseCount(ObjectReplacementRule.class, "replacedObjects");
			}
			keep.add(tuple);
		}
		return  keep;
	}

	@Override
	public void logJamon(){
		JamonMonitorLogger.increaseCount(ObjectReplacementRule.class, "replacedObjects");
	}
	
	
	
}
