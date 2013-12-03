/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import java.util.HashMap;
import java.util.Map;

import org.dllearner.algorithms.isle.index.semantic.SemanticIndex;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Lorenz Buehmann
 * 
 */
public abstract class AbstractRelevanceMetric implements RelevanceMetric {

	protected SemanticIndex index;

	public AbstractRelevanceMetric(SemanticIndex index) {
		this.index = index;
	}
	
	public Map<OWLEntity,Double> normalizeMinMax( Map<OWLEntity,Double> hmEntity2Score ){
		Map<OWLEntity,Double> hmEntity2Norm = new HashMap<OWLEntity,Double>();
		double dMin = Double.MAX_VALUE;
		Double dMax = Double.MIN_VALUE;
		for( OWLEntity e : hmEntity2Score.keySet() )
		{
			double dValue = hmEntity2Score.get(e);
			if( dValue < dMin ){
				dMin = dValue;
			}
			else if( dValue > dMax ){
				dMax = dValue;
			}
		}
		// System.out.println( "min="+ dMin +" max="+ dMax );
		for( OWLEntity e : hmEntity2Score.keySet() )
		{
			double dValue = hmEntity2Score.get(e);
			double dNorm = 0;
			if( dMin == dMax ){
				dNorm = dValue;
			} 
			else {
				dNorm = ( dValue - dMin ) / ( dMax - dMin );
			}
			hmEntity2Norm.put( e, dNorm );
		}
		return hmEntity2Norm;
	}

}
