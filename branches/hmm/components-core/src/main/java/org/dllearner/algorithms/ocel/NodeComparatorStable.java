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

import java.util.Comparator;

import org.dllearner.utilities.owl.ConceptComparator;

/**
 * This comparator is stable, because it only takes covered examples, concept
 * length and the concepts itself (using again a stable comparator) into
 * account, which do not change during the run of the algorithm.
 * 
 * @author Jens Lehmann
 *
 */
public class NodeComparatorStable implements Comparator<ExampleBasedNode> {

	private ConceptComparator conceptComparator = new ConceptComparator();
	
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
					int lengthN1 = n1.getConcept().getLength();
					int lengthN2 = n2.getConcept().getLength();
					
					if(lengthN1<lengthN2)
						return 1;
					else if(lengthN1>lengthN2)
						return -1;
					else
						return conceptComparator.compare(n1.getConcept(), n2.getConcept());
				}
			} else {
				if(n1.isTooWeak() && !n2.isTooWeak())
					return -1;
				else if(!n1.isTooWeak() && n2.isTooWeak())
					return 1;
				else
					return conceptComparator.compare(n1.getConcept(), n2.getConcept());
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
