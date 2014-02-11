/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.core.owl.Entity;

/**
 * @author Lorenz Buehmann
 *
 */
public class SignificantPMIRelevanceMetric extends AbstractRelevanceMetric {

	protected final double delta;
	
	/**
	 * 
	 * @param index:  semantic index
	 * @param delta: parameter varying from 0 to 1 
	 */
	public SignificantPMIRelevanceMetric(Index index, double delta) {
		super(index);
		if (delta<0 ||delta>1)
			throw new IllegalArgumentException("Delta parameter should be in [0,1]");
		this.delta = delta;
	}

	@Override
	public synchronized double getRelevance(Entity entityA, Entity entityB){
		double fA = index.getNumberOfDocumentsFor(entityA);
		double fB = index.getNumberOfDocumentsFor(entityB);
		double fAB = index.getNumberOfDocumentsFor(entityA, entityB);
		
		double N = index.getTotalNumberOfDocuments();
		
		double pmi = Math.log(fAB / (fA*fB/N + Math.sqrt(fA)*Math.sqrt(Math.log(delta)/-2)));
		
		return pmi;
	}
	
	@Override
	public synchronized double getNormalizedRelevance(Entity entityA, Entity entityB){
		//TODO
		return Double.NaN;
	}

}
