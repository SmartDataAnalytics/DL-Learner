package org.dllearner.distributed.amqp;

import java.util.Comparator;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.Heuristic;
import org.dllearner.core.config.ConfigOption;

import com.google.common.collect.ComparisonChain;

public class OEHeuristicRuntime extends AbstractComponent implements Heuristic, Comparator<OENode> {
	@ConfigOption(description="penalty for long descriptions (horizontal " +
			"expansion) (strong by default)", defaultValue="0.1")
	private double expansionPenaltyFactor = 0.1;

	@ConfigOption(description="bonus for being better than parent node",
			defaultValue="0.3")
	private double gainBonusFactor = 0.3;

	@ConfigOption(description="penalty if a node OWLClassExpression has " +
			"very many refinements since exploring such a node is " +
			"computationally very expensive", defaultValue="0.0001")
	private double nodeRefinementPenalty = 0.0001;

	@ConfigOption(name="startNodeBonus", defaultValue="0.1")
	private double startNodeBonus = 0.1;

	public OEHeuristicRuntime() { }

	@Override
	public void init() throws ComponentInitException { }

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

	@Override
	public int compare(OENode node1, OENode node2) {
		return ComparisonChain.start()
				.compare(getNodeScore(node1), getNodeScore(node2))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
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
