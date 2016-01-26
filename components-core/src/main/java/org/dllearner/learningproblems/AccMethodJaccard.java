package org.dllearner.learningproblems;

public class AccMethodJaccard implements AccMethodTwoValued {

	public AccMethodJaccard() {}
	
	public AccMethodJaccard(boolean init) {
		if(init)init();
	}

	@Override
	public void init() {
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		if(tp / (double) (tp+fn) <= 1 - noise) {
			return -1;
		}
		return Heuristics.getJaccardCoefficient(tp, tp + fn + fp);
	}

}
