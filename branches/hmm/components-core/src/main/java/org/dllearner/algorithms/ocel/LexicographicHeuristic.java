/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.algorithms.ocel;

import org.dllearner.utilities.owl.ConceptComparator;

public class LexicographicHeuristic implements ExampleBasedHeuristic {

	// Vergleich von Konzepten, falls alle anderen Kriterien fehlschlagen
	ConceptComparator conceptComparator = new ConceptComparator();
	
	// implementiert einfach die Definition in der Diplomarbeit
	public int compare(ExampleBasedNode n1, ExampleBasedNode n2) {
		
		// sicherstellen, dass Qualität ausgewertet wurde
		if(n1.isQualityEvaluated() && n2.isQualityEvaluated() && !n1.isTooWeak() && !n2.isTooWeak()) {
			if(n1.getCoveredNegatives().size()<n2.getCoveredNegatives().size()) 
				return 1;
			else if(n1.getCoveredNegatives().size()>n2.getCoveredNegatives().size())
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
		return (o instanceof LexicographicHeuristic);
	}
	
}
