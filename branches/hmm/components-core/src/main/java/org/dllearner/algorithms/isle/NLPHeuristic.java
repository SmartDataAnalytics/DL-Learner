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

package org.dllearner.algorithms.isle;

import java.util.Comparator;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.utilities.owl.ConceptComparator;

/**
 * 
 * TODO: NLP-Heuristiken in Statistik integrieren
 * 
 * @author Jens Lehmann
 *
 */
public class NLPHeuristic implements Comparator<OENode> {
	// strong penalty for long descriptions
	private double expansionPenaltyFactor = 0.1;
	// bonus for being better than parent node
	private double gainBonusFactor = 0.3;
	// penalty if a node description has very many refinements since exploring 
	// such a node is computationally very expensive
	private double nodeRefinementPenalty = 0.0001;
	// syntactic comparison as final comparison criterion
	private ConceptComparator conceptComparator = new ConceptComparator();
	
	@Override
	public int compare(OENode node1, OENode node2) {
//		System.out.println("node1 " + node1);
//		System.out.println("score: " + getNodeScore(node1));
//		System.out.println("node2 " + node2);
//		System.out.println("score: " + getNodeScore(node2));
		
		double diff = getNodeScore(node1) - getNodeScore(node2);
		
		if(diff>0) {		
			return 1;
		} else if(diff<0) {
			return -1;
		} else {
			return conceptComparator.compare(node1.getDescription(), node2.getDescription());
		}
	}

	public double getNodeScore(OENode node) {
		// accuracy as baseline
		double score = node.getAccuracy();
		// being better than the parent gives a bonus;
		if(!node.isRoot()) {
			double parentAccuracy = node.getParent().getAccuracy();
			score += (parentAccuracy - score) * gainBonusFactor;
		}
		// penalty for horizontal expansion
		score -= node.getHorizontalExpansion() * expansionPenaltyFactor;
		// penalty for having many child nodes (stuck prevention)
		score -= node.getRefinementCount() * nodeRefinementPenalty;
		return score;
	}

	public double getExpansionPenaltyFactor() {
		return expansionPenaltyFactor;
	}	

}
