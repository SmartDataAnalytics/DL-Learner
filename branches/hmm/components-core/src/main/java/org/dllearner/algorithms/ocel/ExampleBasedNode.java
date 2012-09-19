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

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.SearchTreeNode;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.utilities.owl.ConceptComparator;

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
public class ExampleBasedNode implements SearchTreeNode {

//	public static long exampleMemoryCounter = 0;
	
//	private OCELConfigurator configurator;
	
	private static DecimalFormat df = new DecimalFormat();
	
	// example based variables
	private Set<Individual> coveredPositives;
	private Set<Individual> coveredNegatives;
//	private int coveredPositiveSize;
//	private int coveredNegativeSize;
	
	// the method by which quality was evaluated in this node
	public enum QualityEvaluationMethod { START, REASONER, TOO_WEAK_LIST, OVERLY_GENERAL_LIST };
	private QualityEvaluationMethod qualityEvaluationMethod = QualityEvaluationMethod.START;
	
	// all properties of a node in the search tree
	private Description concept;
	private int horizontalExpansion;
	// specifies whether the node is too weak (exceeds the max. nr allowed
	// misclassifications of positive examples)
	private boolean isTooWeak;
	private boolean isQualityEvaluated;
	private boolean isRedundant;
	
	private double negativeWeight;
	private double startNodeBonus;
	private double expansionPenaltyFactor;
	private int negationPenalty;
	
	private static ConceptComparator conceptComparator = new ConceptComparator();
	private static NodeComparatorStable nodeComparator = new NodeComparatorStable();
	
	// link to parent in search tree
	private ExampleBasedNode parent = null;
	private SortedSet<ExampleBasedNode> children = new TreeSet<ExampleBasedNode>(nodeComparator);
	// apart from the child nodes, we also keep child concepts
	private SortedSet<Description> childConcepts = new TreeSet<Description>(conceptComparator);
	
	// a flag whether this could be a solution for a posonly learning problem
	private boolean isPosOnlyCandidate = true;
	
	public ExampleBasedNode(Description concept, double negativeWeight, double startNodeBonus, double expansionPenaltyFactor, int negationPenalty) {
//		this.configurator = configurator;
		this.concept = concept;
		horizontalExpansion = 0;
		isQualityEvaluated = false;
		this.negativeWeight = negativeWeight;
		this.startNodeBonus = startNodeBonus;
		this.expansionPenaltyFactor = expansionPenaltyFactor;
		this.negationPenalty = negationPenalty;
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

    public boolean addChild(ExampleBasedNode child) {
        // child.setParent(this);
        child.parent = this;
        childConcepts.add(child.concept);
        return children.add(child);
    }
	
	public void setQualityEvaluationMethod(QualityEvaluationMethod qualityEvaluationMethod) {
		this.qualityEvaluationMethod = qualityEvaluationMethod;
	}

	public void setCoveredExamples(Set<Individual> coveredPositives, Set<Individual> coveredNegatives) {
		this.coveredPositives = coveredPositives;
		this.coveredNegatives = coveredNegatives;
		isQualityEvaluated = true;
//		exampleMemoryCounter += coveredPositives.size() * 4;
//		exampleMemoryCounter += coveredNegatives.size() * 4;
	}

	@Override		
	public String toString() {
//		System.out.println(concept);
		String ret = concept.toString() + " [q:";
		if(isTooWeak)
			ret += "tw";
		else
			ret += coveredNegatives.size();
		ret += ", he:" + horizontalExpansion + ", children:" + children.size() + "]";
		return ret;
	}
	
	// returns the refinement chain leading to this node as string
	public String getRefinementChainString() {
		if(parent!=null) {
			String ret = parent.getRefinementChainString();
			ret += " => " + concept.toString();
			return ret;
		} else {
			return concept.toString();
		}
	}	
	
	public String getTreeString(int nrOfPositiveExamples, int nrOfNegativeExamples) {
		return getTreeString(nrOfPositiveExamples, nrOfNegativeExamples, 0,null, null).toString();
	}
	
	public String getTreeString(int nrOfPositiveExamples, int nrOfNegativeExamples, String baseURI) {
		return getTreeString(nrOfPositiveExamples, nrOfNegativeExamples, 0,baseURI, null).toString();
	}	
	
	public String getTreeString(int nrOfPositiveExamples, int nrOfNegativeExamples, String baseURI, Map<String,String> prefixes) {
		return getTreeString(nrOfPositiveExamples, nrOfNegativeExamples, 0,baseURI, prefixes).toString();
	}	
	
	private StringBuilder getTreeString(int nrOfPositiveExamples, int nrOfNegativeExamples, int depth, String baseURI, Map<String,String> prefixes) {
		StringBuilder treeString = new StringBuilder();
		for(int i=0; i<depth-1; i++)
			treeString.append("  ");
		if(depth!=0)
			// treeString.append("|-→ ");
			treeString.append("|--> ");
		treeString.append(getShortDescription(nrOfPositiveExamples, nrOfNegativeExamples, baseURI, prefixes)+"\n");
		for(ExampleBasedNode child : children) {
			treeString.append(child.getTreeString(nrOfPositiveExamples, nrOfNegativeExamples, depth+1,baseURI, prefixes));
		}
		return treeString;
	}
	
	public String getShortDescription(int nrOfPositiveExamples, int nrOfNegativeExamples, String baseURI, Map<String, String> prefixes) {
		String ret = concept.toString(baseURI, prefixes) + " [";
		
		if(isTooWeak)
			ret += "q:tw";
		else {
			double accuracy = 100 * (coveredPositives.size() + nrOfNegativeExamples - coveredNegatives.size())/(double)(nrOfPositiveExamples+nrOfNegativeExamples);
			ret += "acc:" + df.format(accuracy) + "% ";			
			
			// comment this out to display the heuristic score with default parameters
			double heuristicScore = MultiHeuristic.getNodeScore(this, nrOfPositiveExamples, nrOfNegativeExamples, negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
			ret += "h:" +df.format(heuristicScore) + " ";
			
			int wrongPositives = nrOfPositiveExamples - coveredPositives.size();
			ret += "q:" + wrongPositives + "p-" + coveredNegatives.size() + "n";
		}
		
		ret += " ("+qualityEvaluationMethod+"), he:" + horizontalExpansion;
		ret += " c:" + children.size() + "]";
		
		return ret;
	}
	
	public String getShortDescriptionHTML(int nrOfPositiveExamples, int nrOfNegativeExamples, String baseURI) {
		String ret = "<html><nobr> " + concept.toManchesterSyntaxString(baseURI,null) + " <i>[";
		
		if(isTooWeak)
			ret += "q:tw";
		else {
			double accuracy = 100 * (coveredPositives.size() + nrOfNegativeExamples - coveredNegatives.size())/(double)(nrOfPositiveExamples+nrOfNegativeExamples);
			ret += "<b>acc: " + df.format(accuracy) + "% </b>";			
			
			// comment this out to display the heuristic score with default parameters
			double heuristicScore = MultiHeuristic.getNodeScore(this, nrOfPositiveExamples, nrOfNegativeExamples, negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
			ret += "h:" +df.format(heuristicScore) + " ";
			
			int wrongPositives = nrOfPositiveExamples - coveredPositives.size();
			ret += "q:" + wrongPositives + "p-" + coveredNegatives.size() + "n";
		}
		
		ret += " ("+qualityEvaluationMethod+"), he:" + horizontalExpansion;
		ret += " c:" + children.size() + "]";
		
		return ret + "</i></nobr></html>";
	}	
	
	//TODO integrate this method with the one above
	public String getStats(int nrOfPositiveExamples, int nrOfNegativeExamples) {
		String ret = " [";
		
		if(isTooWeak)
			ret += "q:tw";
		else {
			double accuracy = 100 * (coveredPositives.size() + nrOfNegativeExamples - coveredNegatives.size())/(double)(nrOfPositiveExamples+nrOfNegativeExamples);
			ret += "acc:" + df.format(accuracy) + "% ";			
			
			// comment this out to display the heuristic score with default parameters
			double heuristicScore = MultiHeuristic.getNodeScore(this, nrOfPositiveExamples, nrOfNegativeExamples, negativeWeight, startNodeBonus, expansionPenaltyFactor, negationPenalty);
			ret += "h:" +df.format(heuristicScore) + " ";
			
			int wrongPositives = nrOfPositiveExamples - coveredPositives.size();
			ret += "q:" + wrongPositives + "p-" + coveredNegatives.size() + "n";
		}
		
		ret += " ("+qualityEvaluationMethod+"), he:" + horizontalExpansion;
		ret += " c:" + children.size() + "]";
		
		return ret;
	}
	
	public double getAccuracy(int nrOfPositiveExamples, int nrOfNegativeExamples) {
		return (coveredPositives.size() + nrOfNegativeExamples - coveredNegatives.size())/(double)(nrOfPositiveExamples+nrOfNegativeExamples);
	}
	
	/**
	 * Used to detect whether one node is more accurate than another one
	 * with calculating accuracy itself.
	 * @return Number of covered positives minus number of covered negatives.
	 */
	public int getCovPosMinusCovNeg() {
		return coveredPositives.size() - coveredNegatives.size();
	}
	
	public Set<Individual> getCoveredPositives() {
		return coveredPositives;
	}	
	
	public Set<Individual> getCoveredNegatives() {
		return coveredNegatives;
	}
	
	public SortedSet<ExampleBasedNode> getChildren() {
		return children;
	}

	public SortedSet<Description> getChildConcepts() {
		return childConcepts;
	}

	public Description getConcept() {
		return concept;
	}	
	
	public Description getExpression() {
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
	public boolean isTooWeak() {
		return isTooWeak;
	}

	/**
	 * @return the parent
	 */
	public ExampleBasedNode getParent() {
		return parent;
	}

	public boolean isPosOnlyCandidate() {
		return isPosOnlyCandidate;
	}

	public void setPosOnlyCandidate(boolean isPosOnlyCandidate) {
		this.isPosOnlyCandidate = isPosOnlyCandidate;
	}

}