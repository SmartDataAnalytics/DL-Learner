package org.dllearner.tools.ore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.tools.ore.explanation.AxiomRanker;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyManager;

public class ImpactManager implements RepairManagerListener{
	
	private static ImpactManager instance;
	private Map<OWLAxiom, Set<OWLAxiom>> impact;
	private AxiomRanker ranker;
	private OWLAxiom actual;
	private List<OWLAxiom> selectedAxioms;
	private List<ImpactManagerListener> listeners;
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private Reasoner reasoner;

	private ImpactManager(OREManager oreMan) {
		this.reasoner = oreMan.getPelletReasoner().getReasoner();
		this.ontology = reasoner.getLoadedOntologies().iterator().next();
		this.manager = reasoner.getManager();
		impact = new HashMap<OWLAxiom, Set<OWLAxiom>>();
		selectedAxioms = new ArrayList<OWLAxiom>();
		listeners = new ArrayList<ImpactManagerListener>();
		ranker = new AxiomRanker(ontology, reasoner, manager);
		RepairManager.getRepairManager(oreMan).addListener(this);

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
	

	public Set<OWLAxiom> getImpactAxioms(OWLAxiom ax) {
		
		Set<OWLAxiom> imp = impact.get(ax);
		if (imp == null) {
			imp = new HashSet<OWLAxiom>();
			impact.put(ax, imp);
			if(ax != null){
//				imp.addAll(ranker.computeImpactOnRemoval(ax));
				imp.addAll(ranker.computeImpactSOS(ax));
			}
		}
		return imp;
	}
	
	public Set<OWLAxiom> getImpactForAxioms2Remove(){
		Set<OWLAxiom> totalImpact = new HashSet<OWLAxiom>();
		for(OWLAxiom ax : selectedAxioms){
			Set<OWLAxiom> imp = getImpactAxioms(ax);
			if(imp != null){
				totalImpact.addAll(imp);
			}
			
			
		}
		return totalImpact;
	}
	
	public void setActualAxiom(OWLAxiom ax){
		actual = ax;
		fireImpactListChanged();
	}
	
	public void addAxiom2ImpactList(OWLAxiom ax){
		selectedAxioms.add(ax);
		
		fireImpactListChanged();
	}
	
	public void removeAxiomFromImpactList(OWLAxiom ax){
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
		impact.clear();
		fireImpactListChanged();
		
	}
	

}
