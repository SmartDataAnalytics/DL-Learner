package org.dllearner.accuracymethods;

/**
 * Accuracy Method with beta factor;
 * the beta factor is used to indicate the coverage on class learning problems.
 * beta also influences weak elimination
 */
public interface AccMethodWithBeta {
	/**
	 * set the beta value;
	 * the consumer of the accuracy method should call this on all implementing accuracy methods
	 * @param beta
	 */
	void setBeta(double beta);
}
