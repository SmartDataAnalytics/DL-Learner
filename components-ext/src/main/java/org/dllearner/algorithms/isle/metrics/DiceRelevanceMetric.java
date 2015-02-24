package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.index.Index;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Andre Melo
 *
 */
public class DiceRelevanceMetric extends AbstractRelevanceMetric{

	public DiceRelevanceMetric(Index index) {
		super(index);
	}

	@Override
	public double getRelevance(OWLEntity entityA, OWLEntity entityB) {
		double nrOfDocumentsA = index.getNumberOfDocumentsFor(entityA);
		double nrOfDocumentsB = index.getNumberOfDocumentsFor(entityB);
		
		if (nrOfDocumentsA==0 || nrOfDocumentsB==0)
			return 0;
		
		double nrOfDocumentsAandB = index.getNumberOfDocumentsFor(entityA, entityB);
		
		double dice = 2 * nrOfDocumentsAandB / (nrOfDocumentsA + nrOfDocumentsB);
		
		return dice;
	}

	@Override
	public double getNormalizedRelevance(OWLEntity entity1, OWLEntity entity2) {
		return getRelevance(entity1, entity2);
	}


}
