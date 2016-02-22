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

package org.dllearner.algorithms.isle.metrics;

import java.util.Arrays;
import java.util.List;

import org.dllearner.algorithms.el.ELDescriptionTreeComparator;
import org.dllearner.algorithms.el.ELHeuristic;
import org.dllearner.algorithms.el.SearchTreeNode;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A stable comparator for search tree nodes. Stable means that the order
 * of nodes will not change during the run of the learning algorithm. In
 * this implementation, this is ensured by using only covered examples
 * and tree size as criteria.
 * 
 * @author Jens Lehmann
 *
 */
public class RelevanceWeightedStableHeuristic implements ELHeuristic {

	private ELDescriptionTreeComparator cmp = new ELDescriptionTreeComparator();
	private RelevanceWeightings weightings;
	private List<RelevanceMetric> relevanceMetrics;
	private OWLClass classToDescribe;
	
	
	public RelevanceWeightedStableHeuristic(OWLClass classToDescribe, RelevanceWeightings weightings, RelevanceMetric... relevanceMetrics) {
		this.classToDescribe = classToDescribe;
		this.weightings = weightings;
		this.relevanceMetrics = Arrays.asList(relevanceMetrics);
	}
	
	public RelevanceWeightedStableHeuristic(OWLClass classToDescribe, RelevanceWeightings weightings, List<RelevanceMetric> relevanceMetrics) {
		this.classToDescribe = classToDescribe;
		this.weightings = weightings;
		this.relevanceMetrics = relevanceMetrics;
	}
	
	public RelevanceWeightedStableHeuristic(OWLClass classToDescribe, RelevanceMetric... relevanceMetrics) {
		this(classToDescribe, new DefaultRelevanceWeightings(), relevanceMetrics);
	}
	
	public RelevanceWeightedStableHeuristic(OWLClass classToDescribe, List<RelevanceMetric> relevanceMetrics) {
		this(classToDescribe, new DefaultRelevanceWeightings(), relevanceMetrics);
	}
	
	/**
	 * @param weightings the weightings to set
	 */
	public void setWeightings(RelevanceWeightings weightings) {
		this.weightings = weightings;
	}
	
	/**
	 * @param relevanceMetrics the relevanceMetrics to set
	 */
	public void setRelevanceMetrics(List<RelevanceMetric> relevanceMetrics) {
		this.relevanceMetrics = relevanceMetrics;
	}
	
	/**
	 * @param classToDescribe the classToDescribe to set
	 */
	public void setClassToDescribe(OWLClass classToDescribe) {
		this.classToDescribe = classToDescribe;
	}
	
	public double getNodeScore(SearchTreeNode node){
		double score = node.getAccuracy();
		OWLClassExpression d = node.getDescriptionTree().transformToClassExpression();
		for (RelevanceMetric metric : relevanceMetrics) {
			score += weightings.getWeight(metric.getClass()) * metric.getRelevance(classToDescribe, d);
		}
		return score;
	}
	
	@Override
	public int compare(SearchTreeNode o1, SearchTreeNode o2) {
	
//		int diff = o2.getCoveredNegatives() - o1.getCoveredNegatives();
		double score1 = o1.getScore().getAccuracy();
		double score2 = o2.getScore().getAccuracy();
		int diff = Double.compare(score1, score2);
		if(diff>0) {		
			return 1;
		} else if(diff<0) {
			return -1;
		} else {
			
			double sizeDiff = o2.getDescriptionTree().getSize()- o1.getDescriptionTree().getSize();
			
			if(sizeDiff == 0) {
				return cmp.compare(o1.getDescriptionTree(), o2.getDescriptionTree());
			} else if(sizeDiff>0) {
				return 1;
			} else {
				return -1;
			}
			
		}		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
	}

}
