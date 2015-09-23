package org.dllearner.learningproblems;

import org.dllearner.core.Reasoner;


public interface AccMethodApproximate {
	public double getApproxDelta();

	public void setApproxDelta(double approxDelta);

	public void setReasoner(Reasoner reasoner);

}
