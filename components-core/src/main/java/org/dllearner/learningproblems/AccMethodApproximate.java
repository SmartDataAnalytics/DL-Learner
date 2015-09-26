package org.dllearner.learningproblems;

import org.dllearner.core.Component;
import org.dllearner.core.Reasoner;


public interface AccMethodApproximate extends Component {
	public double getApproxDelta();

	public void setApproxDelta(double approxDelta);

	public void setReasoner(Reasoner reasoner);

}
