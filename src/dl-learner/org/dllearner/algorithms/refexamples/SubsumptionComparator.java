package org.dllearner.algorithms.refexamples;

import java.util.Comparator;

import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;

public class SubsumptionComparator implements Comparator<ExampleBasedNode> {

	public ReasoningService rs;
	
	public SubsumptionComparator(ReasoningService rs) {
		this.rs = rs;
	}
	
	public int compare(ExampleBasedNode arg0, ExampleBasedNode arg1) {
		Description concept1 = arg0.getConcept();
		Description concept2 = arg1.getConcept();
		// return true if concept1 is a super concept of concept2
		boolean value1 = rs.subsumes(concept1, concept2);
		if(value1)
			return 1;
		
		boolean value2 = rs.subsumes(concept2, concept1);
		if(value2)
			return -1;
		
		// both concepts are equal
		return 0;
	}

}
