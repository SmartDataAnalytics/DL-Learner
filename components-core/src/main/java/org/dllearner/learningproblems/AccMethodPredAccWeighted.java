package org.dllearner.learningproblems;

import org.dllearner.core.ComponentAnn;

@ComponentAnn(name = "Weighted Predictive Accuracy", shortName = "weighted.pred_acc", version = 0)
public class AccMethodPredAccWeighted implements AccMethodTwoValued {
	
	private boolean balanced = false;
	private double posWeight = 1;
	private double negWeight = 1;

	public AccMethodPredAccWeighted() {
	}

	public AccMethodPredAccWeighted(boolean init) {
		if (init) init();
	}

	@Override
	public void init() {
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		int posExamples = tp + fn;
		int negExamples = fp + tn;
		
		int maxNotCovered = (int) Math.ceil(noise*posExamples);
		
		if(fn != 0 && fn >= maxNotCovered) {
			return -1;
		}
		
		if (balanced) {
			posWeight = 1/(double)posExamples;
			negWeight = 1/(double)negExamples;
		}
		return ((tp*posWeight) + (tn*negWeight)) / ((posExamples*posWeight)+(negExamples*negWeight));
	}

	public boolean isBalanced() {
		return balanced;
	}

	public void setBalanced(boolean balanced) {
		this.balanced = balanced;
	}

	public double getPosWeight() {
		return posWeight;
	}

	public void setPosWeight(double posWeight) {
		this.posWeight = posWeight;
	}

	public double getNegWeight() {
		return negWeight;
	}

	public void setNegWeight(double negWeight) {
		this.negWeight = negWeight;
	}

}
