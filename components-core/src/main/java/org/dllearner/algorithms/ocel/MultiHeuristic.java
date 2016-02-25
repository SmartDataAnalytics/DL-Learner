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

import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.config.ConfigOption;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;

import java.util.Set;

/**
 * This heuristic combines the following criteria to assign a
 * double score value to a node:
 * <ul>
 * <li>quality/accuracy of a concept (based on the full training set, not
 *   the negative example coverage as the flexible heuristic)</li>
 * <li>horizontal expansion</li>
 * <li>accuracy gain: The heuristic takes into account the accuracy
 *   difference between a node and its parent. If there is no gain (even
 *   though we know that the refinement is proper) it is unlikely (although
 *   not excluded) that the refinement is a necessary path to take towards a
 *   solution.</li>
 * </ul> 
 *
 * The heuristic has two parameters:
 * <ul>
 * <li>expansion penalty factor: describes how much accuracy gain is worth
 *   an increase of horizontal expansion by one (typical value: 0.01)</li>
 * <li>gain bonus factor: describes how accuracy gain should be weighted
 *   versus accuracy itself (typical value: 1.00)</li>
 * </ul>
 *   
 * The value of a node is calculated as follows:
 * 
 * <p><code>value = accuracy + gain bonus factor * accuracy gain - expansion penalty
 * factor * horizontal expansion - node children penalty factor * number of children of node</code></p>
 * 
 * <p><code>accuracy = (TP + TN)/(P + N)</code></p>
 * 
 * <p><code>
 * TP = number of true positives (= covered positives)<br />
 * TN = number of true negatives (= nr of negatives examples - covered negatives)<br />
 * P = number of positive examples<br />
 * N = number of negative examples<br />
 * </code></p>
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "multiple criteria heuristic", shortName = "multiheuristic", version = 0.7)
public class MultiHeuristic implements ExampleBasedHeuristic, Component {
	
//	private OCELConfigurator configurator;
	
	// heuristic parameters
	
	@ConfigOption(description = "how much accuracy gain is worth an increase of horizontal expansion by one (typical value: 0.01)", defaultValue="0.02")
	private double expansionPenaltyFactor = 0.02;
	
	@ConfigOption(description = "how accuracy gain should be weighted versus accuracy itself (typical value: 1.00)", defaultValue="0.5")
	private double gainBonusFactor = 0.5;
	
	@ConfigOption(description = "penalty factor for the search tree node child count (use higher values for simple learning problems)", defaultValue="0.0001")
	private double nodeChildPenalty = 0.0001;
	
	@ConfigOption(description = "the score value for the start node", defaultValue="0.1")
	private double startNodeBonus = 0.1; //was 2.0
	
	// penalise errors on positive examples harder than on negative examples
	// (positive weight = 1)
	@ConfigOption(description = "weighting factor on the number of true negatives (true positives are weigthed with 1)", defaultValue="1.0")
	private double negativeWeight = 1.0; // was 0.8;
	
	@ConfigOption(description = "penalty value to deduce for using a negated class expression (complementOf)", defaultValue="0")
	private int negationPenalty = 0;

	// examples
	@NoConfigOption
	private int nrOfNegativeExamples;
	@NoConfigOption
	private int nrOfExamples;
	
	@Deprecated
	public MultiHeuristic(int nrOfPositiveExamples, int nrOfNegativeExamples) {
		this.nrOfNegativeExamples = nrOfNegativeExamples;
		nrOfExamples = nrOfPositiveExamples + nrOfNegativeExamples;
//		this(nrOfPositiveExamples, nrOfNegativeExamples, 0.02, 0.5);
	}
	
	public MultiHeuristic(int nrOfPositiveExamples, int nrOfNegativeExamples, double negativeWeight, double startNodeBonus, double expansionPenaltyFactor, int negationPenalty) {
		this.nrOfNegativeExamples = nrOfNegativeExamples;
		nrOfExamples = nrOfPositiveExamples + nrOfNegativeExamples;
//		this.configurator = configurator;
		this.negativeWeight = negativeWeight;
		this.startNodeBonus = startNodeBonus;
		this.expansionPenaltyFactor = expansionPenaltyFactor;
	}

    public MultiHeuristic(){

    }

//	public MultiHeuristic(int nrOfPositiveExamples, int nrOfNegativeExamples, double expansionPenaltyFactor, double gainBonusFactor) {
//		this.nrOfNegativeExamples = nrOfNegativeExamples;
//		nrOfExamples = nrOfPositiveExamples + nrOfNegativeExamples;
//		this.expansionPenaltyFactor = expansionPenaltyFactor;
//		this.gainBonusFactor = gainBonusFactor;
//	}

	@Override
	public void init() throws ComponentInitException {
		// nothing to do here
	}	
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ExampleBasedNode node1, ExampleBasedNode node2) {
		double score1 = getNodeScore(node1);
		double score2 = getNodeScore(node2);
		double diff = score1 - score2;
		if(diff>0)
			return 1;
		else if(diff<0)
			return -1;
		else
			// we cannot return 0 here otherwise different nodes/concepts with the
			// same score may be ignored (not added to a set because an equal element exists)
			return node1.getConcept().compareTo(node2.getConcept());
	}

	public double getNodeScore(ExampleBasedNode node) {
		double accuracy = getWeightedAccuracy(node.getCoveredPositives().size(),node.getCoveredNegatives().size());
		ExampleBasedNode parent = node.getParent();
		double gain = 0;
		if(parent != null) {
			double parentAccuracy =  getWeightedAccuracy(parent.getCoveredPositives().size(),parent.getCoveredNegatives().size());
			gain = accuracy - parentAccuracy;
		} else {
			accuracy += startNodeBonus;
		}
		int he = node.getHorizontalExpansion() - getHeuristicLengthBonus(node.getConcept());
		return accuracy + gainBonusFactor * gain - expansionPenaltyFactor * he - nodeChildPenalty * node.getChildren().size();
	}
	
	private double getWeightedAccuracy(int coveredPositives, int coveredNegatives) {
		return (coveredPositives + negativeWeight * (nrOfNegativeExamples - coveredNegatives))/(double)nrOfExamples;
	}
	
	public static double getNodeScore(ExampleBasedNode node, int nrOfPositiveExamples, int nrOfNegativeExamples, double negativeWeight, double startNodeBonus, double expansionPenaltyFactor, int negationPenalty) {
		MultiHeuristic multi = new MultiHeuristic(nrOfPositiveExamples, nrOfNegativeExamples, negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
		return multi.getNodeScore(node);
	}
	
	// this function can be used to give some constructs a length bonus
	// compared to their syntactic length
	private int getHeuristicLengthBonus(OWLClassExpression description) {
		
		
		int bonus = 0;
		
		Set<OWLClassExpression> nestedClassExpressions = description.getNestedClassExpressions();
		for (OWLClassExpression expression : nestedClassExpressions) {
			// do not count TOP symbols (in particular in ALL r.TOP and EXISTS r.TOP)
			// as they provide no extra information
			if(expression.isOWLThing())
				bonus = 1; //2;
			
			// we put a penalty on negations, because they often overfit
			// (TODO: make configurable)
			else if(expression instanceof OWLObjectComplementOf) {
				bonus = -negationPenalty;
			}
			
//			if(OWLClassExpression instanceof BooleanValueRestriction)
//				bonus = -1;
			
			// some bonus for doubles because they are already penalised by length 3
			else if(expression instanceof OWLDataSomeValuesFrom) {
//				System.out.println(description);
				bonus = 3; //2;
			}
		}
		
		return bonus;
	}

    public double getExpansionPenaltyFactor() {
        return expansionPenaltyFactor;
    }

    public void setExpansionPenaltyFactor(double expansionPenaltyFactor) {
        this.expansionPenaltyFactor = expansionPenaltyFactor;
    }

	public int getNrOfNegativeExamples() {
		return nrOfNegativeExamples;
	}

	public void setNrOfNegativeExamples(int nrOfNegativeExamples) {
		this.nrOfNegativeExamples = nrOfNegativeExamples;
	}

	public int getNrOfExamples() {
		return nrOfExamples;
	}

	public void setNrOfExamples(int nrOfExamples) {
		this.nrOfExamples = nrOfExamples;
	}

	public double getGainBonusFactor() {
		return gainBonusFactor;
	}

	public void setGainBonusFactor(double gainBonusFactor) {
		this.gainBonusFactor = gainBonusFactor;
	}

	public double getNodeChildPenalty() {
		return nodeChildPenalty;
	}

	public void setNodeChildPenalty(double nodeChildPenalty) {
		this.nodeChildPenalty = nodeChildPenalty;
	}

	public double getStartNodeBonus() {
		return startNodeBonus;
	}

	public void setStartNodeBonus(double startNodeBonus) {
		this.startNodeBonus = startNodeBonus;
	}

	public double getNegativeWeight() {
		return negativeWeight;
	}

	public void setNegativeWeight(double negativeWeight) {
		this.negativeWeight = negativeWeight;
	}

	public int getNegationPenalty() {
		return negationPenalty;
	}

	public void setNegationPenalty(int negationPenalty) {
		this.negationPenalty = negationPenalty;
	}
}
