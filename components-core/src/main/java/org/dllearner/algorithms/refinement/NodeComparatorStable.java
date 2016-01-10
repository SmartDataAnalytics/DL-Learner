package org.dllearner.algorithms.refinement;

import java.util.Comparator;

import org.dllearner.utilities.owl.OWLClassExpressionUtils;

/**
 * Der Comparator ist stable, weil er nur nach covered negatives,
 * Konzeptlänge und Konzeptstring vergleicht, die sich während des Algorithmus nicht
 * ändern können.
 * 
 * @author jl
 *
 */
public class NodeComparatorStable implements Comparator<Node> {

	// implementiert 
	public int compare(Node n1, Node n2) {
		
		// sicherstellen, dass Qualität ausgewertet wurde
		if(n1.isQualityEvaluated() && n2.isQualityEvaluated()) {
			if(!n1.isTooWeak() && !n2.isTooWeak()) {
				if(n1.getCoveredNegativeExamples()<n2.getCoveredNegativeExamples()) 
					return 1;
				else if(n1.getCoveredNegativeExamples()>n2.getCoveredNegativeExamples())
					return -1;
				else {
					//prefer nodes with shorted concepts
					int diff = OWLClassExpressionUtils.getLength(n2.getConcept()) - OWLClassExpressionUtils.getLength(n1.getConcept());
					
					if(diff == 0){
						diff = n1.getConcept().compareTo(n2.getConcept());
					}
						
					return diff;
				}
			} else
				return n1.getConcept().compareTo(n2.getConcept());
		}
		
		throw new RuntimeException("Cannot compare nodes, which have no evaluated quality or are too weak.");
	}

	// alle NodeComparators führen zur gleichen Ordnung
	@Override		
	public boolean equals(Object o) {
		return (o instanceof NodeComparatorStable);
	}

}
