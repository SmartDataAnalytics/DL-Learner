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

package org.dllearner.algorithms.isle;

import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.core.AbstractHeuristic;
import org.dllearner.core.config.ConfigOption;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * 
 * TODO: NLP-Heuristiken in Statistik integrieren
 * 
 * @author Jens Lehmann
 *
 */
public class NLPHeuristic extends AbstractHeuristic{
	
	// strong penalty for long descriptions
	private double expansionPenaltyFactor = 0.1;
	// bonus for being better than parent node
	private double gainBonusFactor = 0.3;
	// penalty if a node OWLClassExpression has very many refinements since exploring 
	// such a node is computationally very expensive
	private double nodeRefinementPenalty = 0.0001;
	
	@ConfigOption(defaultValue="0.1")
	private double startNodeBonus = 0.1;
	
	private double nlpBonusFactor = 1;
	
	private Map<OWLEntity, Double> entityRelevance;
	
	public NLPHeuristic() {}
	
	public NLPHeuristic(Map<OWLEntity,Double> entityRelevance) {
		this.entityRelevance = entityRelevance;
	}
	
	@Override
	public double getNodeScore(OENode node) {
		// accuracy as baseline
		double score = node.getAccuracy();

		// being better than the parent gives a bonus;
		if(!node.isRoot()) {
			double accuracyGain = node.getAccuracy() - node.getParent().getAccuracy();
			score += accuracyGain * gainBonusFactor;
			// the root node also gets a bonus to possibly spawn useful disjunctions
		} else {
			score += startNodeBonus;
		}

		// penalty for horizontal expansion
		score -= node.getHorizontalExpansion() * expansionPenaltyFactor;

		// penalty for having many child nodes (stuck prevention)
		score -= node.getRefinementCount() * nodeRefinementPenalty;
		
		
//		the NLP based scoring
		OWLClassExpression expression = node.getExpression();//System.out.println(expression);
//		OWLClassExpression owlapiDescription = OWLAPIConverter.getOWLAPIDescription(expression);
//		Set<OWLEntity> entities = OWLAPIConverter.getEntities(owlapiDescription.getSignature());
		Set<OWLEntity> entities = expression.getSignature();
//		double sum = 0;
//		for (OWLEntity entity : entities) {
//			double relevance = entityRelevance.containsKey(entity) ? entityRelevance.get(entity) : 0;//System.out.println(entity + ":" + relevance);
//			if(!Double.isInfinite(relevance)){
//				sum += relevance;
//			}
//		}
//		score += nlpBonusFactor * sum;
		
		return score;
	}

	/**
	 * @param entityRelevance the entityRelevance to set
	 */
	public void setEntityRelevance(Map<OWLEntity, Double> entityRelevance) {
		this.entityRelevance = entityRelevance;
	}
	
	/**
	 * @return the entityRelevance
	 */
	public Map<OWLEntity, Double> getEntityRelevance() {
		return entityRelevance;
	}

}
