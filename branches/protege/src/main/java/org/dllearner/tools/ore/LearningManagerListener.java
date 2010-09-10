package org.dllearner.tools.ore;

import java.util.List;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;

public interface LearningManagerListener {

	public abstract void newDescriptionSelected(int index);
	public abstract void noDescriptionsLeft();
	public abstract void newDescriptionsAdded(List<EvaluatedDescriptionClass> descriptions);
	
}
