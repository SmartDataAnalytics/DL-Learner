/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.index.Index;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Chi Squared
 * @author Andre Melo
 *
 */
public class ChiSquareRelevanceMetric extends AbstractRelevanceMetric {

	public ChiSquareRelevanceMetric(Index index) {
		super(index);
	}
	
	private double chiSquareIteration(double fXY, double e_fXY) {
		return Math.pow(fXY - e_fXY, 2)/e_fXY;	
	}

	@Override
	public synchronized double getRelevance(OWLEntity entityA, OWLEntity entityB){
		double fA = index.getNumberOfDocumentsFor(entityA);
		double fB = index.getNumberOfDocumentsFor(entityB);
		double N = index.getTotalNumberOfDocuments();
		
		if (N==0 || fA==0 || fB==0)
			return 0;
		
		double fAB = index.getNumberOfDocumentsFor(entityA, entityB);
		
		double e_fAB = fA*fB/N; // Expected frequency of A and B assuming independence
		
		double chi2 = 0;
		
		// X=A 		and 	Y=B
		chi2 += chiSquareIteration(fAB, e_fAB);
		// X=A 		and 	Y=not B
		chi2 += chiSquareIteration(fA-fAB, fA-e_fAB);
		// X=not A 	and 	Y=B
		chi2 += chiSquareIteration(fB-fAB, fB-e_fAB);
		// X=not A 	and 	Y=not B
		chi2 += chiSquareIteration(N-fA-fB+fAB, N-fA-fB+e_fAB);
		
		return chi2;
	}
	
	@Override
	public synchronized double getNormalizedRelevance(OWLEntity entityA, OWLEntity entityB){
		return Double.NaN;
	}

}
