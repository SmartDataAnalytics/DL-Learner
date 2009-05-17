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
import org.semanticweb.owl.model.OWLOntologyManager;

public class ImpactManager {
	
	private static ImpactManager instance;
	private Map<OWLAxiom, Set<OWLAxiom>> impact;
	private AxiomRanker ranker;
	private OWLAxiom actual;
	private List<OWLAxiom> selectedAxioms;
	private List<ImpactManagerListener> listeners;

	private ImpactManager(OWLOntologyManager manager, Reasoner reasoner, OWLOntology ontology) {
		impact = new HashMap<OWLAxiom, Set<OWLAxiom>>();
		selectedAxioms = new ArrayList<OWLAxiom>();
		listeners = new ArrayList<ImpactManagerListener>();
		ranker = new AxiomRanker(ontology, reasoner, manager.getOWLDataFactory());

	}
	
	private ImpactManager(){
		
	}
	
	public void addListener(ImpactManagerListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(ImpactManagerListener listener){
		listeners.remove(listener);
	}

	public static synchronized ImpactManager getImpactManager(OWLOntologyManager manager, Reasoner reasoner, OWLOntology ontology) {
		if (instance == null) {
			instance = new ImpactManager(manager, reasoner, ontology);
		}
		return instance;
	}
	

	public Set<OWLAxiom> getImpactAxioms() {
		Set<OWLAxiom> imp = impact.get(actual);
		if (imp == null) {
			imp = new HashSet<OWLAxiom>();
			impact.put(actual, imp);
			imp.addAll(ranker.computeImpactSOS(actual));
		}
		return imp;
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
	
	private void fireAxiomForImpactChanged(){
		for(ImpactManagerListener listener : listeners){
			listener.axiomForImpactChanged();
		}
	}
	

}
