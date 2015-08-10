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

package org.dllearner.algorithms.distributed;

import java.io.Serializable;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;

/**
 * Search algorithm heuristic for the ontology engineering algorithm. The
 * heuristic has a strong bias towards short descriptions (i.e. the algorithm
 * is likely to be less suitable for learning complex descriptions).
 *
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "DistOEHeuristicRuntime", shortName = "dist_celoe_heuristic", version = 0.5)
public class DistOEHeuristicRuntime extends AbstractDistHeuristic implements Serializable{

	private static final long serialVersionUID = -1528462675604702485L;

	// strong penalty for long descriptions
	private double expansionPenaltyFactor = 0.1;
	// bonus for being better than parent node
	private double gainBonusFactor = 0.3;
	// penalty if a node OWLClassExpression has very many refinements since exploring
	// such a node is computationally very expensive
	private double nodeRefinementPenalty = 0.0001;

	@ConfigOption(name = "startNodeBonus", defaultValue="0.1")
	private double startNodeBonus = 0.1;

	public DistOEHeuristicRuntime() {

	}

	@Override
	public void init() throws ComponentInitException {

	}

	@Override
	public double getNodeScore(DistOENode node) {
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

	@Override
	public double getExpansionPenaltyFactor() {
		return expansionPenaltyFactor;
	}

	@Override
	public double getGainBonusFactor() {
		return gainBonusFactor;
	}

	@Override
	public void setGainBonusFactor(double gainBonusFactor) {
		this.gainBonusFactor = gainBonusFactor;
	}

	@Override
	public double getNodeRefinementPenalty() {
		return nodeRefinementPenalty;
	}

	@Override
	public void setNodeRefinementPenalty(double nodeRefinementPenalty) {
		this.nodeRefinementPenalty = nodeRefinementPenalty;
	}

	@Override
	public void setExpansionPenaltyFactor(double expansionPenaltyFactor) {
		this.expansionPenaltyFactor = expansionPenaltyFactor;
	}

	@Override
	public double getStartNodeBonus() {
		return startNodeBonus;
	}

	@Override
	public void setStartNodeBonus(double startNodeBonus) {
		this.startNodeBonus = startNodeBonus;
	}
}