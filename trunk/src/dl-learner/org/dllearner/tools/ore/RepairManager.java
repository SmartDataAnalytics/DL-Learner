package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

public class RepairManager implements OREManagerListener{

	private static RepairManager instance;
	
	private List<RepairManagerListener> listeners;
	
	private OWLOntologyManager manager;
	private Reasoner reasoner;
	
	private List<OWLOntologyChange> repairPlan;
	
	private Stack<List<OWLOntologyChange>> undoStack;
	private Stack<List<OWLOntologyChange>> redoStack;

	private RepairManager(OREManager oreMan){
		this.reasoner = oreMan.getReasoner().getReasoner();
		this.manager = reasoner.getManager();

		listeners = new ArrayList<RepairManagerListener>();
	
		undoStack = new Stack<List<OWLOntologyChange>>();
		redoStack = new Stack<List<OWLOntologyChange>>();
		
		repairPlan = new ArrayList<OWLOntologyChange>();
		
		oreMan.addListener(this);

	}
	
	private RepairManager(){
		
	}
	
	public void addListener(RepairManagerListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(RepairManagerListener listener){
		listeners.remove(listener);
	}

	public static synchronized RepairManager getInstance(OREManager oreMan) {
		if (instance == null) {
			instance = new RepairManager(oreMan);
		}
		return instance;
	}
	
	public void addToRepairPlan(OWLOntologyChange change){
		repairPlan.add(change);
		fireRepairPlanChanged();
	}
	
	public void removeFromRepairPlan(OWLOntologyChange change){
		repairPlan.remove(change);
		fireRepairPlanChanged();
	}
	
	public List<OWLOntologyChange> getRepairPlan(){
		return repairPlan;
	}
	
	public boolean isUndoable(){
		return !undoStack.isEmpty();
	}
	
	public void executeRepairPlan(){
		
		try {
			manager.applyChanges(repairPlan);
		} catch (OWLOntologyChangeException e) {
			System.out.println("Error in Repairmanager: Couldn't apply ontology changes");
			e.printStackTrace();
		}
		undoStack.push(new ArrayList<OWLOntologyChange>(repairPlan));
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>(repairPlan);
		
		repairPlan.clear();
		fireRepairPlanExecuted(changes);
		
	}
	
	public void undo(){
		List<OWLOntologyChange> changes = undoStack.pop();
		redoStack.push(changes);
		
		try {
			manager.applyChanges(getInverseChanges(changes));
		} catch (OWLOntologyChangeException e) {
			System.out.println("Error in Repairmanager: Couldn't apply ontology changes");
			e.printStackTrace();
		}
		
		fireRepairPlanExecuted(changes);
	}
	
	public void redo(){
		
	}
	
	public List<OWLOntologyChange> getInverseChanges(List<OWLOntologyChange> changes){
		List<OWLOntologyChange> inverseChanges = new ArrayList<OWLOntologyChange>(changes.size());
		for(OWLOntologyChange change : changes){
			if(change instanceof RemoveAxiom){
				inverseChanges.add(new AddAxiom(change.getOntology(), change.getAxiom()));
			} else {
				inverseChanges.add(new RemoveAxiom(change.getOntology(), change.getAxiom()));
			}
		}
		return inverseChanges;
	}
	
	private void fireRepairPlanChanged(){
		for(RepairManagerListener listener : listeners){
			listener.repairPlanChanged();
		}
	}
	
	private void fireRepairPlanExecuted(List<OWLOntologyChange> changes){
		for(RepairManagerListener listener : listeners){
			listener.repairPlanExecuted(changes);
		}
	}

	@Override
	public void activeOntologyChanged() {
		repairPlan.clear();
		
	}
}
