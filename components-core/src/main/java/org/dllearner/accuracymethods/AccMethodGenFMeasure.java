package org.dllearner.accuracymethods;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.Heuristics;

@ComponentAnn(name = "Generalised FMeasure", shortName = "gen_fmeasure", version = 0.1)
public class AccMethodGenFMeasure implements AccMethodThreeValued, AccMethodWithBeta {

	@ConfigOption(description = "beta factor (0 = do not use)", defaultValue = "0")
	private double beta = 0;

	@Override
	public double getAccOrTooWeak3(int pos1, int neg1, int icPos, int icNeg, int posEx, int negatedPosEx, double noise) {
			// Cn(I_C) \cap D_C is the same set if we ignore Cn ...
		int tmp1Size = pos1 + neg1; // true positives (correct examples)

		int icSize = icPos + icNeg;
		double prec = Heuristics.divideOrZero(tmp1Size, icSize);
		double rec = tmp1Size / (double) (posEx + negatedPosEx);

			// we only return too weak if there is no recall
			if(rec <= 0.0000001) {
				return -1;
			}

			return Heuristics.getFScoreBalanced(rec,prec,beta);
	}

	@Override
	public void init() {
	}

	@Override
	public void setBeta(double beta) {
		this.beta = beta;

	}

}
