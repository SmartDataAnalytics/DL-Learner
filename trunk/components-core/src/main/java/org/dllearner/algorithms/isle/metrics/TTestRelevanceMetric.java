/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.core.owl.Entity;

/**
 * @author Andre Melo
 *
 */
public class TTestRelevanceMetric extends AbstractRelevanceMetric {

	public TTestRelevanceMetric(Index index) {
		super(index);
	}

	@Override
	public synchronized double getRelevance(Entity entityA, Entity entityB){
		double nrOfDocumentsA = index.getNumberOfDocumentsFor(entityA);
		double nrOfDocumentsB = index.getNumberOfDocumentsFor(entityB);
		double nrOfDocumentsAB = index.getNumberOfDocumentsFor(entityA, entityB);
		
		double nrOfDocuments = index.getTotalNumberOfDocuments();
		
		if (nrOfDocumentsA==0 || nrOfDocumentsB==0 || nrOfDocumentsAB==0)
			return 0;
		
		double ttest = 	(nrOfDocumentsAB - (nrOfDocumentsA*nrOfDocumentsB)/nrOfDocuments) /
						Math.sqrt(nrOfDocumentsAB*(1-nrOfDocumentsAB/nrOfDocuments));
		
		return ttest;
	}
	
	@Override
	public synchronized double getNormalizedRelevance(Entity entityA, Entity entityB){
		//TODO
		return Double.NaN;
	}

}
