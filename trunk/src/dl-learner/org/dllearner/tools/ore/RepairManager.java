package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

public class RepairManager implements OREManagerListener{

	private static RepairManager instance;
	
	private List<RepairManagerListener> listeners;
	
	private OWLOntologyManager manager;
	private Reasoner reasoner;
	
	private Set<OWLOntologyChange> repairPlan;
	
	private Stack<List<OWLOntologyChange>> undoStack;
	private Stack<List<OWLOntologyChange>> redoStack;
	
	private Set<OWLAxiom> selectedAxioms;
	
	private Set<OWLAxiom> scheduled2Remove;
	private Set<OWLAxiom> scheduled2Add;

	private RepairManager(OREManager oreMan){
		this.reasoner = oreMan.getReasoner().getReasoner();
		this.manager = reasoner.getManager();

		listeners = new ArrayList<RepairManagerListener>();
	
		undoStack = new Stack<List<OWLOntologyChange>>();
		redoStack = new Stack<List<OWLOntologyChange>>();
		
		repairPlan = new LinkedHashSet<OWLOntologyChange>();
		
		selectedAxioms = new HashSet<OWLAxiom>();
		
		scheduled2Remove = new HashSet<OWLAxiom>();
		scheduled2Add = new HashSet<OWLAxiom>();
		
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
		if(change instanceof RemoveAxiom){
			scheduled2Remove.add(change.getAxiom());
		} else {
			scheduled2Add.add(change.getAxiom());
		}
		fireRepairPlanChanged();
	}
	
	public void addToRepairPlan(List<OWLOntologyChange> changes){
		for(OWLOntologyChange change : changes){
			if(change instanceof RemoveAxiom){
				if(scheduled2Add.contains(change.getAxiom())){
					scheduled2Add.remove(change.getAxiom());
					repairPlan.remove(new AddAxiom(change.getOntology(), change.getAxiom()));
				} else {
					scheduled2Remove.add(change.getAxiom());
					repairPlan.add(change);
				}
				
			} else {
				scheduled2Add.add(change.getAxiom());
				repairPlan.add(change);
			}
			
		}
//		repairPlan.addAll(changes);
		fireRepairPlanChanged();
	}
	
	public void removeFromRepairPlan(OWLOntologyChange change){
		repairPlan.remove(change);
		if(change instanceof RemoveAxiom){
			scheduled2Remove.remove(change.getAxiom());
		} else {
			scheduled2Add.remove(change.getAxiom());
		}
		fireRepairPlanChanged();
	}
	
	public void removeFromRepairPlan(List<OWLOntologyChange> changes){
		for(OWLOntologyChange change : changes){
			if(change instanceof RemoveAxiom){
				scheduled2Remove.add(change.getAxiom());
			} else {
				scheduled2Add.add(change.getAxiom());
			}
			repairPlan.remove(change);
		}
//		repairPlan.removeAll(changes);
		fireRepairPlanChanged();
	}
	
	
	public boolean isScheduled2Remove(OWLAxiom ax){
		return scheduled2Remove.contains(ax);
	}
	
	public boolean isScheduled2Add(OWLAxiom ax){
		return scheduled2Add.contains(ax);
	}
	
	public List<OWLOntologyChange> getRepairPlan(){
		return new ArrayList<OWLOntologyChange>(repairPlan);
	}
	
	public boolean isUndoable(){
		return !undoStack.isEmpty();
	}
	
	public void executeRepairPlan(){
		OREManager.getInstance().getModifier().applyOntologyChanges(new ArrayList<OWLOntologyChange>(repairPlan));
//		try {
//			
//			manager.applyChanges(new ArrayList<OWLOntologyChange>(repairPlan));
//		} catch (OWLOntologyChangeException e) {
//			System.out.println("Error in Repairmanager: Couldn't apply ontology changes");
//			e.printStackTrace();
//		}
		undoStack.push(new ArrayList<OWLOntologyChange>(repairPlan));
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>(repairPlan);
		
		repairPlan.clear();
		fireRepairPlanExecuted(changes);
		
	}
	
	public void undo(){
		List<OWLOntologyChange> changes = undoStack.pop();
		redoStack.push(changes);
		OREManager.getInstance().getModifier().applyOntologyChanges(getInverseChanges(changes));
//		try {
//			manager.applyChanges(getInverseChanges(changes));
//		} catch (OWLOntologyChangeException e) {
//			System.out.println("Error in Repairmanager: Couldn't apply ontology changes");
//			e.printStackTrace();
//		}
		
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
