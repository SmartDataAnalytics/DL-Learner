/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.algorithms.refexamples;

import java.util.List;

import org.dllearner.core.configurators.ExampleBasedROLComponentConfigurator;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Thing;
import org.dllearner.utilities.owl.ConceptComparator;

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
public class MultiHeuristic implements ExampleBasedHeuristic {
	
	private ConceptComparator conceptComparator = new ConceptComparator();
	private ExampleBasedROLComponentConfigurator configurator;
	
	// heuristic parameters
	private double expansionPenaltyFactor = 0.02;
	private double gainBonusFactor = 0.5;
	private double nodeChildPenalty = 0.0001; // (use higher values than 0.0001 for simple learning problems);
	private double startNodeBonus = 0.1; //was 2.0
	// penalise errors on positive examples harder than on negative examples
	// (positive weight = 1)
	private double negativeWeight = 1.0; // was 0.8;
	
	// examples
	private int nrOfNegativeExamples;
	private int nrOfExamples;
	
	@Deprecated
	public MultiHeuristic(int nrOfPositiveExamples, int nrOfNegativeExamples) {
		this.nrOfNegativeExamples = nrOfNegativeExamples;
		nrOfExamples = nrOfPositiveExamples + nrOfNegativeExamples;
//		this(nrOfPositiveExamples, nrOfNegativeExamples, 0.02, 0.5);
	}
	
	public MultiHeuristic(int nrOfPositiveExamples, int nrOfNegativeExamples, ExampleBasedROLComponentConfigurator configurator) {
		this.nrOfNegativeExamples = nrOfNegativeExamples;
		nrOfExamples = nrOfPositiveExamples + nrOfNegativeExamples;
		this.configurator = configurator;
		negativeWeight = configurator.getNegativeWeight();
		startNodeBonus = configurator.getStartNodeBonus();
	}
	
//	public MultiHeuristic(int nrOfPositiveExamples, int nrOfNegativeExamples, double expansionPenaltyFactor, double gainBonusFactor) {
//		this.nrOfNegativeExamples = nrOfNegativeExamples;
//		nrOfExamples = nrOfPositiveExamples + nrOfNegativeExamples;
//		this.expansionPenaltyFactor = expansionPenaltyFactor;
//		this.gainBonusFactor = gainBonusFactor;
//	}

	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(ExampleBasedNode node1, ExampleBasedNode node2) {
		double score1 = getNodeScore(node1);
		double score2 = getNodeScore(node2);
		double diff = score1 - score2;
		if(diff>0)
			return 1;
		else if(diff<0)
			return -1;
		else
			// TODO: would it be OK to simply return 0 here (?)
			// could improve performance a bit
			return conceptComparator.compare(node1.getConcept(), node2.getConcept());
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
	
	public static double getNodeScore(ExampleBasedNode node, int nrOfPositiveExamples, int nrOfNegativeExamples, ExampleBasedROLComponentConfigurator configurator) {
		MultiHeuristic multi = new MultiHeuristic(nrOfPositiveExamples, nrOfNegativeExamples, configurator);
		return multi.getNodeScore(node);
	}
	
	// this function can be used to give some constructs a length bonus
	// compared to their syntactic length
	private int getHeuristicLengthBonus(Description description) {
		int bonus = 0;
		
		// do not count TOP symbols (in particular in ALL r.TOP and EXISTS r.TOP)
		// as they provide no extra information
		if(description instanceof Thing)
			bonus = 1; //2;
		
		// we put a penalty on negations, because they often overfit
		// (TODO: make configurable)
		else if(description instanceof Negation) {
			bonus = -configurator.getNegationPenalty();
		}
		
//		if(description instanceof BooleanValueRestriction)
//			bonus = -1;
		
		// some bonus for doubles because they are already penalised by length 3
		else if(description instanceof DatatypeSomeRestriction) {
//			System.out.println(description);
			bonus = 3; //2;
		}
		
		List<Description> children = description.getChildren();
		for(Description child : children) {
			bonus += getHeuristicLengthBonus(child);
		}
		return bonus;
	}
}
