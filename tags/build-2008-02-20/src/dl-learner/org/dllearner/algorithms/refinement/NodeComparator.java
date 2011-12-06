package org.dllearner.algorithms.refinement;

import org.dllearner.utilities.ConceptComparator;

public class NodeComparator implements Heuristic {

	// Vergleich von Konzepten, falls alle anderen Kriterien fehlschlagen
	ConceptComparator conceptComparator = new ConceptComparator();
	
	// implementiert einfach die Definition in der Diplomarbeit
	public int compare(Node n1, Node n2) {
		
		// sicherstellen, dass Qualität ausgewertet wurde
		if(n1.isQualityEvaluated() && n2.isQualityEvaluated() && !n1.isTooWeak() && !n2.isTooWeak()) {
			if(n1.getCoveredNegativeExamples()<n2.getCoveredNegativeExamples()) 
				return 1;
			else if(n1.getCoveredNegativeExamples()>n2.getCoveredNegativeExamples())
				return -1;
			else {
				//TODO: es wäre geringfügig effizienter die Länge nicht mehrfach zu berechnen
				// Besser: Länge wird einfach ignoriert und stattdessen horizontal expansion
				// genommen => das ist günstiger, da so verhindert wird, dass das gleiche
				// Konzept lange ausgeschlachtet wird, obwohl es ein gleich gutes Element gibt,
				// was vielleicht nur um 1 länger ist;
				// => damit trotzdem die jeweils besten gefundenen Elemente ermittelt werden
				// können (da fällt die horizontal expansion dann natürlich als Kriterium
				// weg, da die nur während des Algorithmus eine Rolle spielt) wird zusätzlich
				// ein separator NodeComparator genutzt
				// if(n1.getConcept().getLength()<n2.getConcept().getLength())
				// 	return 1;
				// else if(n1.getConcept().getLength()>n2.getConcept().getLength())
				//	return -1;
				// else {
					if(n1.getHorizontalExpansion()<n2.getHorizontalExpansion())
						return 1;
					else if(n1.getHorizontalExpansion()>n2.getHorizontalExpansion())
						return -1;
					//else
					//	return 0;
					
					// Vorsicht: es darf nur 0 zurückgegeben werden, wenn die Konzepte identisch sind,
					// in dem Fall werden sie vom Algorithmus ignoriert
					// TODO: hier müsste also gleich eine Vergleichsfunktion für Konzepte in
					// ordered negation normal form einbringen und hätte damit sofort den redundancy
					// check erledigt (??) => horizontalExpansion könnte allerdings bei gleichen
					// Konzepten unterschiedlich sein, also ev. doch keine so gute Idee
					
					else {
						
						//if(n1.getConcept().hashCode()<n2.getConcept().hashCode())
						//	return 1;
						//else if(n1.getConcept().hashCode()>n2.getConcept().hashCode())
						//	return -1;
						
						// throw new RuntimeException("Current implementation cannot cope with candidate concepts with equal hash codes");
						
						// TODO: es ist nicht sehr gut nur Strings zu vergleichen 
						// int test = n1.getConcept().toString().compareTo(n2.getConcept().toString());
						return conceptComparator.compare(n1.getConcept(), n2.getConcept());
						//if(test>0)
						//	return 1;
						//else if(test<0)
						//	return -1;
						
						// dieser Fall sollte eigentlich nicht eintreten, außer wenn
						// node entfernt wird => das ist notwendig, wenn horiz. exp.
						// eines Knotens geändert wird; der muss dann gelöscht und
						// wieder eingefügt werden
						
						// System.out.println("equal nodes:");
						// System.out.println(n1 + " chain: " + n1.getRefinementChainString());
						// System.out.println(n2 + " chain: " + n2.getRefinementChainString());
						
						// throw new RuntimeException("same node twice");
						// return 0;
						// Absicherung das keine gleichen Konzepte vorkommen
						// throw new RuntimeException("Current implementation cannot cope with concepts with equal string representations.");						
					}
					
						
				//}
			}
		}
		
		throw new RuntimeException("Cannot compare nodes, which have no evaluated quality or are too weak.");
	}


	// alle NodeComparators führen zur gleichen Ordnung
	@Override	
	public boolean equals(Object o) {
		return (o instanceof NodeComparator);
	}
	
}