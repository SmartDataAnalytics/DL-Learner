package org.dllearner.tools.ore;

import java.util.List;

import org.semanticweb.owl.model.OWLOntologyChange;

public interface RepairManagerListener {
	public abstract void repairPlanExecuted(List<OWLOntologyChange> changes);
	public abstract void repairPlanChanged();
}
