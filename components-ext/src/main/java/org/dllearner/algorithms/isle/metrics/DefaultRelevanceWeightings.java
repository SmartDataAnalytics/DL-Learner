/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import org.dllearner.algorithms.isle.metrics.ChiSquareRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.DiceRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.JaccardRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.LLRRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.PMIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.SCIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.SignificantPMIRelevanceMetric;
import org.dllearner.algorithms.isle.metrics.TTestRelevanceMetric;

/**
 * @author Lorenz Buehmann
 *
 */
public class DefaultRelevanceWeightings extends RelevanceWeightings{
	
	public DefaultRelevanceWeightings() {
		put(PMIRelevanceMetric.class, 1.0);
		put(SignificantPMIRelevanceMetric.class, 1.0);
		put(ChiSquareRelevanceMetric.class, 1.0);
		put(TTestRelevanceMetric.class, 1.0);
		put(JaccardRelevanceMetric.class, 1.0);
		put(DiceRelevanceMetric.class, 1.0);
		put(SCIRelevanceMetric.class, 1.0);
		put(LLRRelevanceMetric.class, 1.0);
	}
}
