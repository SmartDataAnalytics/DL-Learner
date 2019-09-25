/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import com.google.common.collect.ComparisonChain;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;

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
@ComponentAnn(name = "Flexible Heuristic", shortName = "flexheuristic", version = 0.1)
public class FlexibleHeuristic implements ExampleBasedHeuristic {

	@ConfigOption(description = "the number of negative examples")
	private int nrOfNegativeExamples;
	@ConfigOption(description = "score percent to deduct per expression length", required = true)
	private double percentPerLengthUnit;
	// 5% sind eine Verlängerung um 1 wert
	// double percentPerLengthUnit = 0.05;

	public int getNrOfNegativeExamples() {
		return nrOfNegativeExamples;
	}

	public void setNrOfNegativeExamples(int nrOfNegativeExamples) {
		this.nrOfNegativeExamples = nrOfNegativeExamples;
	}

	public double getPercentPerLengthUnit() {
		return percentPerLengthUnit;
	}

	public void setPercentPerLengthUnit(double percentPerLengthUnit) {
		this.percentPerLengthUnit = percentPerLengthUnit;
	}

	
	public FlexibleHeuristic(int nrOfNegativeExamples, double percentPerLengthUnit) {
		this.nrOfNegativeExamples = nrOfNegativeExamples;
		this.percentPerLengthUnit = percentPerLengthUnit;
	}

	public FlexibleHeuristic() {
	}
	
	// implementiert einfach die Definition in der Diplomarbeit
	@Override
	public int compare(ExampleBasedNode n1, ExampleBasedNode n2) {
		
		// sicherstellen, dass Qualität ausgewertet wurde
		if(n1.isQualityEvaluated() && n2.isQualityEvaluated() && !n1.isTooWeak() && !n2.isTooWeak()) {
			
			// alle scores sind negativ, größere scores sind besser
			double score1 = -n1.getCoveredNegatives().size()/(double)nrOfNegativeExamples;
			score1 -= percentPerLengthUnit * OWLClassExpressionUtils.getLength(n1.getConcept());
			
			double score2 = -n2.getCoveredNegatives().size()/(double)nrOfNegativeExamples;
			score2 -= percentPerLengthUnit * OWLClassExpressionUtils.getLength(n2.getConcept());

			return ComparisonChain.start()
					.compare(score1, score2)
					.compare(n1.getConcept(), n2.getConcept())
					.result();
		}
		
		throw new RuntimeException("Cannot compare nodes, which have no evaluated quality or are too weak.");
	}

	@Override		
	public boolean equals(Object o) {
		return (o instanceof FlexibleHeuristic);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
	}

	@Override
	public double getNodeScore(ExampleBasedNode n1) {
		double score1 = -n1.getCoveredNegatives().size()/(double)nrOfNegativeExamples;
		score1 -= percentPerLengthUnit * OWLClassExpressionUtils.getLength(n1.getConcept());
		return score1;
	}
}
