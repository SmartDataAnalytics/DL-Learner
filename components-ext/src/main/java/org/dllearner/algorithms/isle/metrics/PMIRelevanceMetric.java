/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.index.Index;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Lorenz Buehmann
 *
 */
public class PMIRelevanceMetric extends AbstractRelevanceMetric {

	public PMIRelevanceMetric(Index index) {
		super(index);
	}

	@Override
	public double getRelevance(OWLEntity entityA, OWLEntity entityB){
		long nrOfDocumentsA = index.getNumberOfDocumentsFor(entityA);
		long nrOfDocumentsB = index.getNumberOfDocumentsFor(entityB);
		long nrOfDocumentsAB = index.getNumberOfDocumentsFor(entityA, entityB);
		
		long nrOfDocuments = index.getTotalNumberOfDocuments();
		
		double pA = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsA / (double) nrOfDocuments);
		double pB = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsB / (double) nrOfDocuments);
		double pAB = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsAB / (double) nrOfDocuments);
		
		if(pAB == 0 || (pA * pB) == 0){
			return 0;
		}
		
		double pmi = Math.log(pAB / pA * pB);
		
		return pmi;
	}
	
	@Override
	public double getNormalizedRelevance(OWLEntity entityA, OWLEntity entityB){
		long nrOfDocumentsA = index.getNumberOfDocumentsFor(entityA);
		long nrOfDocumentsB = index.getNumberOfDocumentsFor(entityB);
		long nrOfDocumentsAB = index.getNumberOfDocumentsFor(entityA, entityB);
		
		long nrOfDocuments = index.getTotalNumberOfDocuments();
		
		double pA = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsA / (double) nrOfDocuments);
		double pB = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsB / (double) nrOfDocuments);
		double pAB = nrOfDocuments == 0 ? 0 : ((double) nrOfDocumentsAB / (double) nrOfDocuments);
		
		if(pAB == 0 || pA * pB == 0){
			return 0;
		}
		double pmi = Math.log(pAB / (pA * pB));
		
		double denominator = -Math.log(pAB);
		if(denominator == 0){
			return 0;
		}
		
		double normalizedPMI = (pmi/denominator + 1)/2;
		
		return normalizedPMI;
	}

}
