/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.index.Index;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Log Likelihood Ratio
 * @author Andre Melo
 *
 */
public class LLRRelevanceMetric extends AbstractRelevanceMetric {

	public LLRRelevanceMetric(Index index) {
		super(index);
	}
	
	private double llrIteration(double pXY, double pX, double pY) {
		if (pXY==0 || pX==0 || pY==0)
			return 0;
		return pXY * Math.log(pXY/(pX*pY));
	}

	@Override
	public synchronized double getRelevance(OWLEntity entityA, OWLEntity entityB){
		double fA = index.getNumberOfDocumentsFor(entityA);
		double fB = index.getNumberOfDocumentsFor(entityB);
		double N = index.getTotalNumberOfDocuments();
		double fAB = index.getNumberOfDocumentsFor(entityA, entityB);
		
		if (N==0 || fA==0 || fB==0)
			return 0;
		
		
		
		double pA = fA/N;
		double pB = fB/N;
		double pAB = fAB/N;

		double llr = 0;
		
		// X=A 		and 	Y=B
		llr += llrIteration( pAB, 		pA, 	pB	 );
		// X=A 		and 	Y=not B
		llr += llrIteration( pA-pAB, 	pA, 	1-pB );
		// X=not A 	and 	Y=B
		llr += llrIteration( pB-pAB, 	1-pA, 	pB	 );
		// X=not A 	and 	Y=not B
		llr += llrIteration( 1-pA-pB+pAB,1-pA, 	1-pB );
		
		return llr;
	}
	
	@Override
	public synchronized double getNormalizedRelevance(OWLEntity entityA, OWLEntity entityB){
		return Double.NaN;
	}

}
