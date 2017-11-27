package org.dllearner.accuracymethods;

import org.dllearner.core.ComponentAnn;
import org.dllearner.learningproblems.Heuristics;

@ComponentAnn(name = "Predictive Accuracy without Weak elimination", shortName = "pred_acc.ocel", version = 0)
public class AccMethodPredAccOCEL implements AccMethodTwoValued, AccMethodNoWeakness {

	public AccMethodPredAccOCEL() {
	}

	public AccMethodPredAccOCEL(boolean init) {
		if (init) init();
	}

	@Override
	public void init() {
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		int posExamples = tp + fn;
		int allExamples = posExamples + fp + tn;

		return Heuristics.divideOrZero( tp + tn, allExamples );
	}

}
