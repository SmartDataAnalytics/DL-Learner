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

package org.dllearner.algorithms.refinement;

import java.util.Comparator;

import org.dllearner.utilities.owl.ConceptComparator;

/**
 * Der Comparator ist stable, weil er nur nach covered negatives,
 * Konzeptlänge und Konzeptstring vergleicht, die sich während des Algorithmus nicht
 * ändern können.
 * 
 * @author jl
 *
 */
public class NodeComparatorStable implements Comparator<Node> {

	ConceptComparator conceptComparator = new ConceptComparator();
	
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
					//TODO: es wäre geringfügig effizienter die Länge nicht mehrfach zu berechnen
					if(n1.getConcept().getLength()<n2.getConcept().getLength())
						return 1;
					else if(n1.getConcept().getLength()>n2.getConcept().getLength())
						return -1;
					else
						return conceptComparator.compare(n1.getConcept(), n2.getConcept());
				}
			} else
				return conceptComparator.compare(n1.getConcept(), n2.getConcept());
		}
		
		throw new RuntimeException("Cannot compare nodes, which have no evaluated quality or are too weak.");
	}

	// alle NodeComparators führen zur gleichen Ordnung
	@Override		
	public boolean equals(Object o) {
		return (o instanceof NodeComparatorStable);
	}

}
