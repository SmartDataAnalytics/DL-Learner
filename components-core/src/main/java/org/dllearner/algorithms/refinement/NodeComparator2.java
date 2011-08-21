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

import org.dllearner.utilities.owl.ConceptComparator;

/**
 * This heuristic compares two nodes by computing a score
 * using the number of covered negatives and the horizontal
 * expansion factor of a node as input. Using this score
 * it decides which one of the nodes seems to be more promising.
 * The heuristic is flexible, because it offers a tradeoff
 * between accurary and horizontal expansion (concept length).
 * In contrast to the lexicographic heuristic this means that
 * it sometimes prefers worse classifiers with low horizontal
 * expansion over a better classifier with high horizontal
 * expansion.
 * 
 * It can be configured by using the "percentPerLenghtUnit" 
 * constructor argument. A higher
 * value means that the algorithm is more likely to search in
 * unexplored areas (= low horizontal expansion) of the search 
 * space vs. looking in promising but already explored (= high
 * horizontal expansion) areas of the search space.
 * 
 * @author Jens Lehmann
 *
 */
public class NodeComparator2 implements Heuristic {

	// Vergleich von Konzepten, falls alle anderen Kriterien fehlschlagen
	private ConceptComparator conceptComparator = new ConceptComparator();
	private int nrOfNegativeExamples;
	private double percentPerLengthUnit;
	
	// 5% sind eine Verlängerung um 1 wert
	// double percentPerLengthUnit = 0.05;
	
	public NodeComparator2(int nrOfNegativeExamples, double percentPerLengthUnit) {
		this.nrOfNegativeExamples = nrOfNegativeExamples;
		this.percentPerLengthUnit = percentPerLengthUnit;
	}
	
	// implementiert einfach die Definition in der Diplomarbeit
	public int compare(Node n1, Node n2) {
		
		// sicherstellen, dass Qualität ausgewertet wurde
		if(n1.isQualityEvaluated() && n2.isQualityEvaluated() && !n1.isTooWeak() && !n2.isTooWeak()) {
			
			// alle scores sind negativ, größere scores sind besser
			double score1 = -n1.getCoveredNegativeExamples()/(double)nrOfNegativeExamples;
			score1 -= percentPerLengthUnit * n1.getConcept().getLength();
			
			double score2 = -n2.getCoveredNegativeExamples()/(double)nrOfNegativeExamples;
			score2 -= percentPerLengthUnit * n2.getConcept().getLength();
			
			double diff = score1 - score2;
			
			if(diff>0)
				return 1;
			else if(diff<0)
				return -1;
			else
				return conceptComparator.compare(n1.getConcept(), n2.getConcept());
		}
		
		throw new RuntimeException("Cannot compare nodes, which have no evaluated quality or are too weak.");
	}

	@Override		
	public boolean equals(Object o) {
		return (o instanceof NodeComparator2);
	}
	
}
