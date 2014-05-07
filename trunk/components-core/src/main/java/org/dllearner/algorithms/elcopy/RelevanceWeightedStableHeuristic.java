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

package org.dllearner.algorithms.elcopy;

import java.util.Arrays;
import java.util.List;

import org.dllearner.algorithms.isle.metrics.RelevanceMetric;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.NamedClass;


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
	private NamedClass classToDescribe;
	
	
	public RelevanceWeightedStableHeuristic(NamedClass classToDescribe, RelevanceWeightings weightings, RelevanceMetric... relevanceMetrics) {
		this.classToDescribe = classToDescribe;
		this.weightings = weightings;
		this.relevanceMetrics = Arrays.asList(relevanceMetrics);
	}
	
	public RelevanceWeightedStableHeuristic(NamedClass classToDescribe, RelevanceWeightings weightings, List<RelevanceMetric> relevanceMetrics) {
		this.classToDescribe = classToDescribe;
		this.weightings = weightings;
		this.relevanceMetrics = relevanceMetrics;
	}
	
	public RelevanceWeightedStableHeuristic(NamedClass classToDescribe, RelevanceMetric... relevanceMetrics) {
		this(classToDescribe, new DefaultRelevanceWeightings(), relevanceMetrics);
	}
	
	public RelevanceWeightedStableHeuristic(NamedClass classToDescribe, List<RelevanceMetric> relevanceMetrics) {
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
	public void setClassToDescribe(NamedClass classToDescribe) {
		this.classToDescribe = classToDescribe;
	}
	
	public double getNodeScore(SearchTreeNode node){
		double score = node.getAccuracy();
		Description d = node.getDescriptionTree().transformToDescription();
		for (RelevanceMetric metric : relevanceMetrics) {
			score += weightings.getWeight(metric.getClass()) * metric.getRelevance(classToDescribe, d);
		}
		return score;
	}
	
	@Override
	public int compare(SearchTreeNode o1, SearchTreeNode o2) {
	
//		int diff = o2.getCoveredNegatives() - o1.getCoveredNegatives();
		double score1 = o1.getScore();
		double score2 = o2.getScore();
		int diff = Double.compare(score1, score2);
		if(diff>0) {		
			return 1;
		} else if(diff<0) {
			return -1;
		} else {
			
			double sizeDiff = o2.getDescriptionTree().size - o1.getDescriptionTree().size;
			
			if(sizeDiff == 0) {
				return cmp.compare(o1.getDescriptionTree(), o2.getDescriptionTree());
			} else if(sizeDiff>0) {
				return 1;
			} else {
				return -1;
			}
			
		}		
	}

}
