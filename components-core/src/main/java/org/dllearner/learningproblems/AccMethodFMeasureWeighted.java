package org.dllearner.learningproblems;

import org.dllearner.core.ComponentAnn;

@ComponentAnn(name = "Weighted FMeasure", shortName = "weighted.fmeasure", version = 0)
public class AccMethodFMeasureWeighted implements AccMethodTwoValued {
	
	private boolean balanced = false;
	private double posWeight = 1;
	private double negWeight = 1;

	@Override
	public void init() {
	}
	
	public AccMethodFMeasureWeighted() {
	}
	
	public AccMethodFMeasureWeighted(boolean init) {
		if (init) init();
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		int posExamples = tp + fn;
		int negExamples = fp + tn;

		double recall = Heuristics.divideOrZero( tp , tp+fn );

		if(recall == 0 || recall < 1 - noise) {
			return -1;
		}

		if (balanced) {
			posWeight = 1/(double)posExamples;
			negWeight = 1/(double)negExamples;
		}
		double precision = tp == 0 ? 0 : ( tp*posWeight ) / ( tp*posWeight+fp*negWeight );
		return Heuristics.getFScore(recall, precision);
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
