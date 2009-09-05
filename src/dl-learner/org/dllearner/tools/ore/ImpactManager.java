package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.tools.ore.explanation.LostEntailmentsChecker;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyManager;

public class ImpactManager implements RepairManagerListener, OREManagerListener{
	
	private static ImpactManager instance;
	private LostEntailmentsChecker lostEntailmentsChecker;
	private List<OWLAxiom> selectedAxioms;
	private List<ImpactManagerListener> listeners;
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private Reasoner reasoner;
	private OREManager oreMan;
	
	private Set<OWLAxiom> lostEntailments;
	private Set<OWLAxiom> addedEntailments;

	private ImpactManager(OREManager oreMan) {
		this.oreMan = oreMan;
		this.reasoner = oreMan.getPelletReasoner().getReasoner();
		this.ontology = reasoner.getLoadedOntologies().iterator().next();
		this.manager = reasoner.getManager();
		
		lostEntailments = new HashSet<OWLAxiom>();
		addedEntailments = new HashSet<OWLAxiom>();
		
		selectedAxioms = new ArrayList<OWLAxiom>();
		listeners = new ArrayList<ImpactManagerListener>();
		
		lostEntailmentsChecker = new LostEntailmentsChecker(ontology, oreMan.getPelletReasoner().getClassifier(), manager);
		RepairManager.getInstance(oreMan).addListener(this);
		oreMan.addListener(this);
	

	}
	
	private ImpactManager(){
		
	}
	
	public void addListener(ImpactManagerListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(ImpactManagerListener listener){
		listeners.remove(listener);
	}

	public static synchronized ImpactManager getInstance(OREManager oreMan) {
		if (instance == null) {
			instance = new ImpactManager(oreMan);
		}
		return instance;
	}
	
	public Set<OWLAxiom> getLostEntailments(){
		return lostEntailments;
	}
	
	public Set<OWLAxiom> getAddedEntailments(){
		return addedEntailments;
	}
	
	public void computeImpactForAxiomsInRepairPlan(){
		lostEntailments.clear();
		addedEntailments.clear();
		List<OWLOntologyChange> repairPlan = RepairManager.getInstance(oreMan).getRepairPlan();
		List<Set<OWLAxiom>> classificationImpact = lostEntailmentsChecker.computeClassificationImpact(repairPlan);
		lostEntailments.addAll(classificationImpact.get(0));
		addedEntailments.addAll(classificationImpact.get(1));
		Set<OWLAxiom> structuralImpact = lostEntailmentsChecker.computeStructuralImpact(repairPlan);
		lostEntailments.addAll(structuralImpact);
	}
	
	public void addSelection(OWLAxiom ax){
		selectedAxioms.add(ax);	
		fireImpactListChanged();
	}
	
	public void removeSelection(OWLAxiom ax){
		selectedAxioms.remove(ax);
		fireImpactListChanged();
	}
	
	public boolean isSelected(OWLAxiom ax){
		return selectedAxioms.contains(ax);
	}
	
	
	private void fireImpactListChanged(){
		for(ImpactManagerListener listener : listeners){
			listener.impactListChanged();
		}
	}

	@Override
	public void repairPlanChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		selectedAxioms.clear();
		lostEntailments.clear();
		addedEntailments.clear();
		fireImpactListChanged();
		
	}

	@Override
	public void activeOntologyChanged() {
		this.reasoner = oreMan.getPelletReasoner().getReasoner();
		this.ontology = reasoner.getLoadedOntologies().iterator().next();
		this.manager = reasoner.getManager();
		lostEntailmentsChecker = new LostEntailmentsChecker(ontology, oreMan.getPelletReasoner().getClassifier(), manager);
		selectedAxioms.clear();
		lostEntailments.clear();
		addedEntailments.clear();
		fireImpactListChanged();
		
	}
	
	
	

}
