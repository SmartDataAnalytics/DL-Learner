package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.index.Index;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Andre Melo
 *
 */
public class JaccardRelevanceMetric extends AbstractRelevanceMetric{

	public JaccardRelevanceMetric(Index index) {
		super(index);
	}

	@Override
	public double getRelevance(OWLEntity entityA, OWLEntity entityB) {
		long nrOfDocumentsA = index.getNumberOfDocumentsFor(entityA);
		long nrOfDocumentsB = index.getNumberOfDocumentsFor(entityB);
		
		if (nrOfDocumentsA==0 || nrOfDocumentsB==0)
			return 0;
		
		double nrOfDocumentsAandB = index.getNumberOfDocumentsFor(entityA, entityB);
		double nrOfDocumentsAorB = nrOfDocumentsA + nrOfDocumentsB - nrOfDocumentsAandB;
		
		double jaccard = nrOfDocumentsAandB / nrOfDocumentsAorB;
		
		return jaccard;
	}

	@Override
	public double getNormalizedRelevance(OWLEntity entity1, OWLEntity entity2) {
		return getRelevance(entity1, entity2);
	}


}
