package org.dllearner.algorithms.ocel;

import java.util.Comparator;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.utilities.owl.ConceptComparator;

public class SubsumptionComparator implements Comparator<Description> {

	private AbstractReasonerComponent rs;
	private ConceptComparator cc = new ConceptComparator();
	
	public SubsumptionComparator(AbstractReasonerComponent rs) {
		this.rs = rs;
	}
	
	public int compare(ExampleBasedNode arg0, ExampleBasedNode arg1) {
		Description concept1 = arg0.getConcept();
		Description concept2 = arg1.getConcept();
		return compare(concept1, concept2);
	}

	public int compare(Description concept1, Description concept2) {
		// return true if concept1 is a super concept of concept2
		boolean value1 = rs.isSuperClassOf(concept1, concept2);
		if(value1)
			return 1;
		
		boolean value2 = rs.isSuperClassOf(concept2, concept1);
		if(value2)
			return -1;
		
//		System.out.println("Incomparable: " + concept1 + " " + concept2);
		
		// both concepts are incomparable => order them syntactically
		return cc.compare(concept1, concept2);
	}

}
