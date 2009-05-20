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
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

public class ImpactManager {
	
	private static ImpactManager instance;
	private Map<OWLAxiom, Set<OWLAxiom>> impact;
	private AxiomRanker ranker;
	private OWLAxiom actual;
	private List<OWLAxiom> selectedAxioms;
	private List<ImpactManagerListener> listeners;
	private OWLOntology ontology;
	private OWLOntologyManager manager;

	private ImpactManager(Reasoner reasoner) {
		this.ontology = reasoner.getLoadedOntologies().iterator().next();
		this.manager = reasoner.getManager();
		impact = new HashMap<OWLAxiom, Set<OWLAxiom>>();
		selectedAxioms = new ArrayList<OWLAxiom>();
		listeners = new ArrayList<ImpactManagerListener>();
		ranker = new AxiomRanker(ontology, reasoner, manager);

	}
	
	private ImpactManager(){
		
	}
	
	public void addListener(ImpactManagerListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(ImpactManagerListener listener){
		listeners.remove(listener);
	}

	public static synchronized ImpactManager getImpactManager(Reasoner reasoner) {
		if (instance == null) {
			instance = new ImpactManager(reasoner);
		}
		return instance;
	}
	

	public Set<OWLAxiom> getImpactAxioms(OWLAxiom ax) {
		
		Set<OWLAxiom> imp = impact.get(ax);
		if (imp == null) {
			imp = new HashSet<OWLAxiom>();
			impact.put(ax, imp);
			if(ax != null){
				imp.addAll(ranker.computeImpactOnRemoval(ax));
				imp.addAll(ranker.computeImpactSOS(ax));//computeImpactSOS(actual));
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
		fireAxiomForImpactChanged();
	}
	
	public void addAxiom2ImpactList(OWLAxiom ax){
		selectedAxioms.add(ax);
		
		fireAxiomForImpactChanged();
	}
	
	public void removeAxiomFromImpactList(OWLAxiom ax){
		selectedAxioms.remove(ax);
		fireAxiomForImpactChanged();
	}
	
	public boolean isSelected(OWLAxiom ax){
		return selectedAxioms.contains(ax);
	}
	
	public List<OWLAxiom> getAxioms2Remove(){
		return selectedAxioms;
	}
	
	public void executeRepairPlan(){
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for(OWLAxiom ax : selectedAxioms){
			changes.add(new RemoveAxiom(ontology, ax));
		}
		try {
			manager.applyChanges(changes);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		impact.clear();
		selectedAxioms.clear();
		fireRepairPlanExecuted();
	}
	
	private void fireAxiomForImpactChanged(){
		for(ImpactManagerListener listener : listeners){
			listener.axiomForImpactChanged();
		}
	}
	
	private void fireRepairPlanExecuted(){
		for(ImpactManagerListener listener : listeners){
			listener.repairPlanExecuted();
		}
	}
	

}
