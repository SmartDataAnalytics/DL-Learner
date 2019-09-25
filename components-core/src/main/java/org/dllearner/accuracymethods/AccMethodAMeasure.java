package org.dllearner.accuracymethods;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.Heuristics;

@ComponentAnn(name = "AMeasure", shortName = "ameasure", version = 0.1)
public class AccMethodAMeasure implements Component, AccMethodTwoValued, AccMethodWithBeta {

	@ConfigOption(description = "beta factor (0 = do not use)", defaultValue = "0")
	protected double beta = 0;

	public AccMethodAMeasure() {}
	
	public AccMethodAMeasure(boolean init) {
		if(init)init();
	}

	@Override
	public void init() {
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		double recall = Heuristics.divideOrZero( tp , tp+fn );
		double precision = Heuristics.divideOrZero( tp , tp+fp );

        // best reachable concept has same recall and precision 1:
		// 1/t+1 * (t*r + 1)
		if (beta == 0) {
			if (recall + 1 < 1 - noise) {
				return -1;
			}
		} else {
			if ((beta * recall + 1) / (beta + 1) < 1 - noise) {
				return -1;
			}
		}

		if (beta == 0) {
			return Heuristics.getAScore(recall, precision);
		} else {
			return Heuristics.getAScore(recall, precision, beta);
		}
	}

	@Override
	public void setBeta(double beta) {
		this.beta = beta;
	}
}
