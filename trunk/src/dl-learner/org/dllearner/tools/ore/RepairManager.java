package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

public class RepairManager {

	private static RepairManager instance;
	

	private OWLAxiom actual;
	private List<OWLAxiom> axioms2Remove;
	private List<RepairManagerListener> listeners;
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private Reasoner reasoner;
	
	private Stack<List<OWLOntologyChange>> undoStack;
	private Stack<List<OWLOntologyChange>> redoStack;

	private RepairManager(Reasoner reasoner) {
		this.reasoner = reasoner;
		this.ontology = reasoner.getLoadedOntologies().iterator().next();
		this.manager = reasoner.getManager();
		
		axioms2Remove = new ArrayList<OWLAxiom>();
		listeners = new ArrayList<RepairManagerListener>();
	
		undoStack = new Stack<List<OWLOntologyChange>>();
		redoStack = new Stack<List<OWLOntologyChange>>();

	}
	
	private RepairManager(){
		
	}
	
	public void addListener(RepairManagerListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(RepairManagerListener listener){
		listeners.remove(listener);
	}

	public static synchronized RepairManager getRepairManager(Reasoner reasoner) {
		if (instance == null) {
			instance = new RepairManager(reasoner);
		}
		return instance;
	}
	
	public void addAxiom2Remove(OWLAxiom ax){
		axioms2Remove.add(ax);
		fireRepairPlanChanged();
	}
	
	public void removeAxiom2Remove(OWLAxiom ax){
		axioms2Remove.remove(ax);
		fireRepairPlanChanged();
	}
	
	public boolean isSelected(OWLAxiom ax){
		return axioms2Remove.contains(ax);
	}
	
	public List<OWLAxiom> getAxioms2Remove(){
		return axioms2Remove;
	}
	
	public void executeRepairPlan(){
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for(OWLAxiom ax : axioms2Remove){
			changes.add(new RemoveAxiom(ontology, ax));
		}
		try {
			manager.applyChanges(changes);
		} catch (OWLOntologyChangeException e) {
			System.out.println("Error in Repairmanager: Couldn't apply ontology changes");
			e.printStackTrace();
		}
		undoStack.push(changes);
		axioms2Remove.clear();
		fireRepairPlanExecuted(changes);
	}
	
	public void undo(){
		List<OWLOntologyChange> changes = undoStack.pop();
		redoStack.push(changes);
		try {
			manager.applyChanges(changes);
		} catch (OWLOntologyChangeException e) {
			System.out.println("Error in Repairmanager: Couldn't apply ontology changes");
			e.printStackTrace();
		}
	}
	
	public void redo(){
		
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
}
