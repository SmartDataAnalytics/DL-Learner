package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.core.owl.Entity;

/**
 * @author Andre Melo
 *
 */
public class DiceRelevanceMetric extends AbstractRelevanceMetric{

	public DiceRelevanceMetric(Index index) {
		super(index);
	}

	@Override
	public double getRelevance(Entity entityA, Entity entityB) {
		double nrOfDocumentsA = index.getNumberOfDocumentsFor(entityA);
		double nrOfDocumentsB = index.getNumberOfDocumentsFor(entityB);
		double nrOfDocumentsAandB = index.getNumberOfDocumentsFor(entityA, entityB);
		
		double dice = 2 * nrOfDocumentsAandB / (nrOfDocumentsA + nrOfDocumentsB);
		
		return dice;
	}

	@Override
	public double getNormalizedRelevance(Entity entity1, Entity entity2) {
		return getRelevance(entity1, entity2);
	}


}
