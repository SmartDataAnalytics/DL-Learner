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

package org.dllearner.algorithms.celoe;

import java.util.Comparator;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DoubleEditor;
import org.dllearner.utilities.owl.ConceptComparator;

/**
 * Search algorithm heuristic for the ontology engineering algorithm. The heuristic
 * has a strong bias towards short descriptions (i.e. the algorithm is likely to be
 * less suitable for learning complex descriptions).
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "OEHeuristicRuntime", shortName = "celoe_heuristic", version = 0.5)
public class OEHeuristicRuntime implements Component, Comparator<OENode>{
	
	// strong penalty for long descriptions
	private double expansionPenaltyFactor = 0.1;
	// bonus for being better than parent node
	private double gainBonusFactor = 0.3;
	// penalty if a node description has very many refinements since exploring 
	// such a node is computationally very expensive
	private double nodeRefinementPenalty = 0.0001;
	// syntactic comparison as final comparison criterion
	private ConceptComparator conceptComparator = new ConceptComparator();
	
	@ConfigOption(name = "startNodeBonus", defaultValue="0.1")
	private double startNodeBonus = 0.1;
	
	public OEHeuristicRuntime() {

	}
	
	@Override
	public void init() throws ComponentInitException {

	}		
	
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
		// the root node also gets a bonus to possibly spawn useful disjunctions
		} else {
			score += startNodeBonus;
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

	public double getGainBonusFactor() {
		return gainBonusFactor;
	}

	public void setGainBonusFactor(double gainBonusFactor) {
		this.gainBonusFactor = gainBonusFactor;
	}

	public double getNodeRefinementPenalty() {
		return nodeRefinementPenalty;
	}

	public void setNodeRefinementPenalty(double nodeRefinementPenalty) {
		this.nodeRefinementPenalty = nodeRefinementPenalty;
	}

	public void setExpansionPenaltyFactor(double expansionPenaltyFactor) {
		this.expansionPenaltyFactor = expansionPenaltyFactor;
	}

	public double getStartNodeBonus() {
		return startNodeBonus;
	}

	public void setStartNodeBonus(double startNodeBonus) {
		this.startNodeBonus = startNodeBonus;
	}


}
