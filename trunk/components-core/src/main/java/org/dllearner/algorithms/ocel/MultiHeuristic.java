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

import java.util.List;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DoubleEditor;
import org.dllearner.core.config.IntegerEditor;
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
@ComponentAnn(name = "multiple criteria heuristic", shortName = "multiheuristic", version = 0.7)
public class MultiHeuristic implements ExampleBasedHeuristic, Component {
	
	private ConceptComparator conceptComparator = new ConceptComparator();
//	private OCELConfigurator configurator;
	
	// heuristic parameters
	
	@ConfigOption(name = "expansionPenaltyFactor", defaultValue="0.02", propertyEditorClass = DoubleEditor.class)
	private double expansionPenaltyFactor = 0.02;
	
	@ConfigOption(name = "gainBonusFactor", defaultValue="0.5", propertyEditorClass = DoubleEditor.class)
	private double gainBonusFactor = 0.5;
	
	@ConfigOption(name = "nodeChildPenalty", defaultValue="0.0001", propertyEditorClass = DoubleEditor.class)
	private double nodeChildPenalty = 0.0001; // (use higher values than 0.0001 for simple learning problems);
	
	@ConfigOption(name = "startNodeBonus", defaultValue="0.1", propertyEditorClass = DoubleEditor.class)
	private double startNodeBonus = 0.1; //was 2.0
	
	// penalise errors on positive examples harder than on negative examples
	// (positive weight = 1)
	@ConfigOption(name = "negativeWeight", defaultValue="1.0", propertyEditorClass = DoubleEditor.class)
	private double negativeWeight = 1.0; // was 0.8;
	
	@ConfigOption(name = "negationPenalty", defaultValue="0", propertyEditorClass = IntegerEditor.class)
	private int negationPenalty = 0;
	
	// examples
	private int nrOfNegativeExamples;
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
	
	public static double getNodeScore(ExampleBasedNode node, int nrOfPositiveExamples, int nrOfNegativeExamples, double negativeWeight, double startNodeBonus, double expansionPenaltyFactor, int negationPenalty) {
		MultiHeuristic multi = new MultiHeuristic(nrOfPositiveExamples, nrOfNegativeExamples, negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
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
			bonus = -negationPenalty;
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
}
