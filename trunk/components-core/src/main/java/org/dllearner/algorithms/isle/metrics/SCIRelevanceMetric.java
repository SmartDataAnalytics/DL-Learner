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
public class SCIRelevanceMetric extends AbstractRelevanceMetric {

	public SCIRelevanceMetric(Index index) {
		super(index);
	}

	@Override
	public synchronized double getRelevance(Entity entityA, Entity entityB){
		double fA = index.getNumberOfDocumentsFor(entityA);
		double fB = index.getNumberOfDocumentsFor(entityB);
		double fAB = index.getNumberOfDocumentsFor(entityA, entityB);
		double N = index.getTotalNumberOfDocuments();
		
		if (fA==0 || fB==0 || fAB==0)
			return 0;
		
		
		double pA = fA / N;
		double pB = fB / N;
		double pAB = fAB / N;
		
		double sci = pAB / (pA * Math.sqrt(pB));
		
		return sci;
	}
	
	@Override
	public synchronized double getNormalizedRelevance(Entity entityA, Entity entityB){
		//TODO
		return Double.NaN;
	}

}
