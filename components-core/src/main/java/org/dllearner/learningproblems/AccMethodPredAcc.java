package org.dllearner.learningproblems;

import org.dllearner.core.ComponentAnn;

@ComponentAnn(name = "Predictive Accuracy", shortName = "pred_acc", version = 0)
public class AccMethodPredAcc implements AccMethodTwoValued {

	public AccMethodPredAcc() {
	}

	public AccMethodPredAcc(boolean init) {
		if (init) init();
	}

	@Override
	public void init() {
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		int posExamples = tp + fn;
		int allExamples = posExamples + fp + tn;
		
		int maxNotCovered = (int) Math.ceil(noise*posExamples);
		
		if(fn != 0 && fn >= maxNotCovered) {
			return -1;
		}
		
		return (tp + tn) / (double) allExamples;
	}

}
