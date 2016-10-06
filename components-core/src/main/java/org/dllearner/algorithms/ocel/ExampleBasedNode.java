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

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractSearchTreeNode;
import org.dllearner.core.StringRenderer;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.datastructures.SearchTreeNode;
import org.dllearner.utilities.datastructures.WeakSearchTreeNode;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * Represents a node in the search tree. A node consists of
 * the following parts:
 * 
 * ... (see paper) ...
 * 
 * @author Jens Lehmann
 *
 */
public class ExampleBasedNode extends AbstractSearchTreeNode<ExampleBasedNode> implements SearchTreeNode, WeakSearchTreeNode {

	private static DecimalFormat df = new DecimalFormat();
	
	// example based variables
	private Set<OWLIndividual> coveredPositives;
	private Set<OWLIndividual> coveredNegatives;

	// the method by which quality was evaluated in this node
	public enum QualityEvaluationMethod { START, REASONER, TOO_WEAK_LIST, OVERLY_GENERAL_LIST }

	private QualityEvaluationMethod qualityEvaluationMethod = QualityEvaluationMethod.START;
	
	// all properties of a node in the search tree
	private OWLClassExpression concept;
	private int horizontalExpansion;
	// specifies whether the node is too weak (exceeds the max. nr allowed
	// misclassifications of positive examples)
	private boolean isTooWeak;
	private boolean isQualityEvaluated;
	private boolean isRedundant;

	// apart from the child nodes, we also keep child concepts
	private SortedSet<OWLClassExpression> childConcepts = new TreeSet<>();
	
	// a flag whether this could be a solution for a posonly learning problem
	private boolean isPosOnlyCandidate = true;

	private OCEL learningAlgorithm;
	
	public ExampleBasedNode(OWLClassExpression concept, AbstractCELA learningAlgorithm) {
		this.concept = concept;
		horizontalExpansion = 0;
		isQualityEvaluated = false;
		this.learningAlgorithm = (OCEL) learningAlgorithm;
	}

	public void setHorizontalExpansion(int horizontalExpansion) {
		this.horizontalExpansion = horizontalExpansion;
	}

	public void setRedundant(boolean isRedundant) {
		this.isRedundant = isRedundant;
	}

	public void setTooWeak(boolean isTooWeak) {
		if(isQualityEvaluated)
			throw new RuntimeException("Cannot set quality of a node more than once.");
		this.isTooWeak = isTooWeak;
		isQualityEvaluated = true;
	}

    @Override
	public void addChild(ExampleBasedNode child) {
    	super.addChild(child);
        childConcepts.add(child.concept);
    }
	
	public void setQualityEvaluationMethod(QualityEvaluationMethod qualityEvaluationMethod) {
		this.qualityEvaluationMethod = qualityEvaluationMethod;
	}

	public void setCoveredExamples(Set<OWLIndividual> coveredPositives, Set<OWLIndividual> coveredNegatives) {
		this.coveredPositives = coveredPositives;
		this.coveredNegatives = coveredNegatives;
		isQualityEvaluated = true;
	}

	public int getQuality() {
		return getCovPosMinusCovNeg();
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	public String toSimpleString() {
		String ret = concept.toString() + " [q:";
		if(isTooWeak)
			ret += "tw";
		else
			ret += coveredNegatives.size();
		ret += ", he:" + horizontalExpansion + ", children:" + children.size() + "]";
		return ret;
	}

	public String getShortDescription() {
		return StringRenderer.getRenderer().render(concept) + getStats();
	}

	public String getStats() {
		String ret = " [";
		
		if(isTooWeak)
			ret += "q:tw";
		else {
			double accuracy = 100 * getAccuracy();
			ret += "acc:" + df.format(accuracy) + "% ";
			
			// comment this out to display the heuristic score with default parameters
			//  learningAlgorithm.getHeuristic()
			int nrOfPositiveExamples = ((PosNegLP) learningAlgorithm.getLearningProblem()).getPositiveExamples().size();
			int nrOfNegativeExamples = ((PosNegLP) learningAlgorithm.getLearningProblem()).getNegativeExamples().size();
			double heuristicScore = MultiHeuristic.getNodeScore(this, nrOfPositiveExamples, nrOfNegativeExamples, learningAlgorithm.getNegativeWeight(), learningAlgorithm.getStartNodeBonus(), learningAlgorithm.getExpansionPenaltyFactor(), learningAlgorithm.getNegationPenalty());
			ret += "h:" +df.format(heuristicScore) + " ";
			
			int wrongPositives = nrOfPositiveExamples - coveredPositives.size();
			ret += "q:" + wrongPositives + "p-" + coveredNegatives.size() + "n";
		}
		
		ret += " ("+qualityEvaluationMethod+"), he:" + horizontalExpansion;
		ret += " c:" + children.size() + "]";
		
		return ret;
	}
	
	public double getAccuracy() {
		int tp = coveredPositives.size();
		int fp = coveredNegatives.size();
		int tn = ((PosNegLP)learningAlgorithm.getLearningProblem()).getNegativeExamples().size() - fp;
		int fn = ((PosNegLP)learningAlgorithm.getLearningProblem()).getPositiveExamples().size() - tp;

		double accuracy = ((PosNegLP)learningAlgorithm.getLearningProblem()).getAccuracyMethod().getAccOrTooWeak2(tp, fn, fp, tn, 1);
		if (accuracy == -1 && !isTooWeak)
			throw new RuntimeException("Accuracy says weak but node is not marked as such.");
		return accuracy;
	}
	
	/**
	 * Used to detect whether one node is more accurate than another one
	 * with calculating accuracy itself.
	 * @return Number of covered positives minus number of covered negatives.
	 */
	public int getCovPosMinusCovNeg() {
		return coveredPositives.size() - coveredNegatives.size();
	}
	
	public Set<OWLIndividual> getCoveredPositives() {
		return coveredPositives;
	}
	
	public Set<OWLIndividual> getCoveredNegatives() {
		return coveredNegatives;
	}

	public SortedSet<OWLClassExpression> getChildConcepts() {
		return childConcepts;
	}

	public OWLClassExpression getConcept() {
		return concept;
	}
	
	@Override
	public OWLClassExpression getExpression() {
		return getConcept();
	}
	
	public QualityEvaluationMethod getQualityEvaluationMethod() {
		return qualityEvaluationMethod;
	}
	
	public int getHorizontalExpansion() {
		return horizontalExpansion;
	}
	public boolean isQualityEvaluated() {
		return isQualityEvaluated;
	}
	public boolean isRedundant() {
		return isRedundant;
	}
	@Override
	public boolean isTooWeak() {
		return isTooWeak;
	}

}