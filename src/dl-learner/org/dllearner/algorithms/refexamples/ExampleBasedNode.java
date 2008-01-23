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

import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.utilities.ConceptComparator;

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
public class ExampleBasedNode {

	// TODO: add example based variables here
	@SuppressWarnings({"unused"})
	private Set<Individual> coveredPositives;
	
	// TOP ist einfach das TOP-Konzept, also das einzige welches nicht evaluiert wird
	public enum QualityEvaluationMethod { TOP, REASONER, TOO_WEAK_LIST, OVERLY_GENERAL_LIST };
	
	private QualityEvaluationMethod qualityEvaluationMethod = QualityEvaluationMethod.TOP;
	
	// alle Eigenschaften eines Knotens im Suchbaum
	private Concept concept;
	private int horizontalExpansion;
	private int coveredNegativeExamples;
	private boolean isTooWeak;
	private boolean isQualityEvaluated;
	private boolean isRedundant;
	
	private static ConceptComparator conceptComparator = new ConceptComparator();
	private static NodeComparatorStable nodeComparator = new NodeComparatorStable();
	
	// Einbettung in Suchbaum
	private ExampleBasedNode parent = null;
	// private Set<Node> children = new HashSet<Node>();
	private Set<ExampleBasedNode> children = new TreeSet<ExampleBasedNode>(nodeComparator);
	// es wird auch eine Liste von Kindern gehalten
	private Set<Concept> childConcepts = new TreeSet<Concept>(conceptComparator);
	
	// verwendeter Operator für Expansion des Knotens
	// private RefinementOperator operator;
	
	public ExampleBasedNode(Concept concept) {
		this.concept = concept;
		horizontalExpansion = 0;
		isQualityEvaluated = false;
	}
	
	public void setCoveredNegativeExamples(int coveredNegativeExamples) {
		if(isQualityEvaluated)
			throw new RuntimeException("Cannot set quality of a node more than once.");
		this.coveredNegativeExamples = coveredNegativeExamples;
		isQualityEvaluated = true;
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
	
	public Concept getConcept() {
		return concept;
	}
	public int getCoveredNegativeExamples() {
		return coveredNegativeExamples;
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
	
	@Override		
	public String toString() {
		String ret = concept.toString() + " [q:";
		if(isTooWeak)
			ret += "tw";
		else
			ret += coveredNegativeExamples;
		ret += ", he:" + horizontalExpansion + ", children:" + children.size() + "]";
		return ret;
	}
	
	// gibt die Refinement-Chain zurück, die zu dem Knoten geführt hat
	public String getRefinementChainString() {
		if(parent!=null) {
			String ret = parent.getRefinementChainString();
			ret += " => " + concept.toString();
			return ret;
		} else {
			return concept.toString();
		}
	}

	public String getTreeString() {
		return getTreeString(0).toString();
	}
	
	private StringBuilder getTreeString(int depth) {
		StringBuilder treeString = new StringBuilder();
		for(int i=0; i<depth-1; i++)
			treeString.append("  ");
		if(depth!=0)
			// treeString.append("|-→ ");
			treeString.append("|--> ");
		treeString.append(getShortDescription()+"\n");
		for(ExampleBasedNode child : children) {
			treeString.append(child.getTreeString(depth+1));
		}
		return treeString;
	}
	
	private String getShortDescription() {
		String ret = concept.toString() + " [q:";
		
		if(isTooWeak)
			ret += "tw";
		else
			ret += coveredNegativeExamples;
		
		ret += " ("+qualityEvaluationMethod+"), he:" + horizontalExpansion + "]";
		return ret;
	}
	
	public Set<ExampleBasedNode> getChildren() {
		return children;
	}

	public Set<Concept> getChildConcepts() {
		return childConcepts;
	}

	public QualityEvaluationMethod getQualityEvaluationMethod() {
		return qualityEvaluationMethod;
	}

	public void setQualityEvaluationMethod(QualityEvaluationMethod qualityEvaluationMethod) {
		this.qualityEvaluationMethod = qualityEvaluationMethod;
	}
	
}
