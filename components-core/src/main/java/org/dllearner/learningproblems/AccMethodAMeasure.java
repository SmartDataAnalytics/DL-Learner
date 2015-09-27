package org.dllearner.learningproblems;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;

public class AccMethodAMeasure implements Component, AccMethodTwoValued {

	public AccMethodAMeasure() {}
	
	public AccMethodAMeasure(boolean init) throws ComponentInitException {
		if(init)init();
	}

	@Override
	public void init() throws ComponentInitException {
		throw new ComponentInitException("Todo");
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		// TODO Auto-generated method stub
		return -1;
	}

}
