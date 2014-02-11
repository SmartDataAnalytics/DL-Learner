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
		long nrOfDocumentsA = index.getNumberOfDocumentsFor(entityA);
		long nrOfDocumentsB = index.getNumberOfDocumentsFor(entityB);
		long nrOfDocumentsAB = index.getNumberOfDocumentsFor(entityA, entityB);
		
		long nrOfDocuments = index.getTotalNumberOfDocuments();
		
		double pA = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsA / (double) nrOfDocuments);
		double pB = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsB / (double) nrOfDocuments);
		double pAB = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsAB / (double) nrOfDocuments);
		
		double sci = pAB / (pA * Math.sqrt(pB));
		
		return sci;
	}
	
	@Override
	public synchronized double getNormalizedRelevance(Entity entityA, Entity entityB){
		//TODO
		return Double.NaN;
	}

}
