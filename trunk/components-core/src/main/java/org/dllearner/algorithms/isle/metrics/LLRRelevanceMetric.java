/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.core.owl.Entity;

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
		return pXY * Math.log(pXY/(pX*pY));
	}

	@Override
	public synchronized double getRelevance(Entity entityA, Entity entityB){
		double fA = index.getNumberOfDocumentsFor(entityA);
		double fB = index.getNumberOfDocumentsFor(entityB);
		double fAB = index.getNumberOfDocumentsFor(entityA, entityB);
		double N = index.getTotalNumberOfDocuments();
		
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
	public synchronized double getNormalizedRelevance(Entity entityA, Entity entityB){
		return Double.NaN;
	}

}
