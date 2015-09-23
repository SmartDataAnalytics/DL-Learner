package org.dllearner.learningproblems;

import org.dllearner.core.Component;

public interface AccMethodTwoValued extends Component {
	/**
	 * Compute accuracy according to this method
	 * @param tp True Positives (positive as positive)
	 * @param fn False Negative (positive as negative)
	 * @param fp False Positive (negative as positive)
	 * @param tn True Negative (negative as negative)
	 * @param noise Noise
	 * @return accuracy value or -1 if too weak
	 */
	double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise);
}
