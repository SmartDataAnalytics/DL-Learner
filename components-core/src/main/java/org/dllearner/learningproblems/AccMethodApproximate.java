package org.dllearner.learningproblems;

import org.dllearner.core.Component;
import org.dllearner.core.Reasoner;


public interface AccMethodApproximate extends Component {
	double getApproxDelta();

	void setApproxDelta(double approxDelta);

	void setReasoner(Reasoner reasoner);

}
