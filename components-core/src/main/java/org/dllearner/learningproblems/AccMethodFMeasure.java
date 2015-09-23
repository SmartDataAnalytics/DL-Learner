package org.dllearner.learningproblems;

import org.dllearner.core.ComponentAnn;

@ComponentAnn(name = "FMeasure", shortName = "fmeasure", version = 0)
public class AccMethodFMeasure implements AccMethodTwoValued {

	@Override
	public void init() {
	}
	
	public AccMethodFMeasure() {
	}
	
	public AccMethodFMeasure(boolean init) {
		if (init) init();
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		double recall = Heuristics.divideOrZero( tp , tp+fn );

		if(recall == 0 || recall < 1 - noise) {
			return -1;
		}

		double precision = Heuristics.divideOrZero( tp , tp+fp );

		return Heuristics.getFScore(recall, precision);
	}

}
