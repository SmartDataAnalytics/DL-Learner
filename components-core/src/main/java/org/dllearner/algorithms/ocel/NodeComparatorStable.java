package org.dllearner.algorithms.ocel;

import java.util.Comparator;

import org.dllearner.utilities.owl.OWLClassExpressionUtils;

/**
 * This comparator is stable, because it only takes covered examples, concept
 * length and the concepts itself (using again a stable comparator) into
 * account, which do not change during the run of the algorithm.
 * 
 * @author Jens Lehmann
 *
 */
public class NodeComparatorStable implements Comparator<ExampleBasedNode> {

	public int compare(ExampleBasedNode n1, ExampleBasedNode n2) {
		
		// make sure quality has been evaluated
		if(n1.isQualityEvaluated() && n2.isQualityEvaluated()) {
			if(!n1.isTooWeak() && !n2.isTooWeak()) {
				int classificationPointsN1 =  n1.getCoveredPositives().size() - n1.getCoveredNegatives().size();
				int classificationPointsN2 =  n2.getCoveredPositives().size() - n2.getCoveredNegatives().size();				
				
				if(classificationPointsN1>classificationPointsN2) 
					return 1;
				else if(classificationPointsN1<classificationPointsN2)
					return -1;
				else {
					int lengthN1 = OWLClassExpressionUtils.getLength(n1.getConcept());
					int lengthN2 = OWLClassExpressionUtils.getLength(n2.getConcept());
					
					if(lengthN1<lengthN2)
						return 1;
					else if(lengthN1>lengthN2)
						return -1;
					else
						return n1.getConcept().compareTo(n2.getConcept());
				}
			} else {
				if(n1.isTooWeak() && !n2.isTooWeak())
					return -1;
				else if(!n1.isTooWeak() && n2.isTooWeak())
					return 1;
				else
					return n1.getConcept().compareTo(n2.getConcept());
			}
		}
		
		throw new RuntimeException("Cannot compare nodes, which have no evaluated quality.");
	}

	// all stable node comparators lead to the same order
	@Override		
	public boolean equals(Object o) {
		return (o instanceof NodeComparatorStable);
	}

}
