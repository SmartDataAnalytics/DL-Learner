/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import java.util.HashMap;
import java.util.Map;

import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.core.owl.Entity;

/**
 * @author Lorenz Buehmann
 * 
 */
public abstract class AbstractRelevanceMetric implements RelevanceMetric {

	protected Index index;

	public AbstractRelevanceMetric(Index index) {
		this.index = index;
	}

	public static Map<Entity, Double> normalizeMinMax(Map<Entity, Double> hmEntity2Score) {
		Map<Entity, Double> hmEntity2Norm = new HashMap<Entity, Double>();

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (Entity e : hmEntity2Score.keySet()) {
			double value = hmEntity2Score.get(e);
			if (value < min) {
				min = value;
			} else if (value > max) {
				max = value;
			}
		}
		// System.out.println( "min="+ dMin +" max="+ dMax );
		for (Entity e : hmEntity2Score.keySet()) {
			double value = hmEntity2Score.get(e);
			double normalized = 0;
			if (min == max) {
				normalized = value;
				normalized = 0.5;
			} else {
				normalized = (value - min) / (max - min);
			}
			hmEntity2Norm.put(e, normalized);
		}
		return hmEntity2Norm;
	}

}
