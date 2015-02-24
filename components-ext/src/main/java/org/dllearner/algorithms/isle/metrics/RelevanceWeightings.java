/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import java.util.HashMap;

import org.dllearner.algorithms.isle.metrics.RelevanceMetric;

/**
 * @author Lorenz Buehmann
 *
 */
public class RelevanceWeightings extends HashMap<Class<? extends RelevanceMetric>, Double>{
	
	public double getWeight(RelevanceMetric metric){
		return get(metric.getClass());
	}
	
	public double getWeight(Class<? extends RelevanceMetric> metricClass){
		return get(metricClass);
	}

}
