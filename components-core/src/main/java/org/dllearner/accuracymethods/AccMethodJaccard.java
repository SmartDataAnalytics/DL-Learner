package org.dllearner.accuracymethods;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.learningproblems.Heuristics;
import org.slf4j.LoggerFactory;

@ComponentAnn(name = "Jaccard Coefficient", shortName = "jaccard", version = 0.1)
public class AccMethodJaccard implements AccMethodTwoValued {

	public AccMethodJaccard() {}
	
	public AccMethodJaccard(boolean init) {
		if(init)init();
	}

	@Override
	public void init() {
		LoggerFactory.getLogger(Component.class).trace("initialising {}", this);
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		if(tp / (double) (tp+fn) <= 1 - noise) {
			return -1;
		}
		return Heuristics.getJaccardCoefficient(tp, tp + fn + fp);
	}

}
