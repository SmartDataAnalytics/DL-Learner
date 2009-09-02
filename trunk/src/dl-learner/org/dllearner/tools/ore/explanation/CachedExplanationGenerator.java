package org.dllearner.tools.ore.explanation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.dllearner.tools.ore.explanation.laconic.LaconicExplanationGenerator;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

import uk.ac.manchester.cs.owl.modularity.ModuleType;

import com.clarkparsia.modularity.ModularityUtils;
import com.clarkparsia.owlapi.OntologyUtils;

public class CachedExplanationGenerator implements ExplanationGenerator, RepairManagerListener{
	
	private Map<OWLAxiom, OWLOntology> axiom2Module;
	private Map<OWLAxiom, Set<Explanation>> regularExplanationCache;
	private Map<OWLAxiom, Set<Explanation>> laconicExplanationCache;
	private Map<OWLAxiom, Integer> lastRequestedRegularSize;
	private Map<OWLAxiom, Integer> lastRequestedLaconicSize;
	private boolean laconicMode = false;
	private PelletExplanationGenerator regularExpGen;
	private LaconicExplanationGenerator laconicExpGen;
	
	private OWLOntology ontology;
	private OWLOntologyManager manager;

	public CachedExplanationGenerator(OWLOntology ontology, Reasoner reasoner){
		this.ontology = ontology;
		this.manager = OWLManager.createOWLOntologyManager();
		
		axiom2Module = new HashMap<OWLAxiom, OWLOntology>();
		regularExplanationCache = new HashMap<OWLAxiom, Set<Explanation>>();
		laconicExplanationCache = new HashMap<OWLAxiom, Set<Explanation>>();
		lastRequestedRegularSize = new HashMap<OWLAxiom, Integer>();
		lastRequestedLaconicSize = new HashMap<OWLAxiom, Integer>();
		RepairManager.getRepairManager(OREManager.getInstance()).addListener(this);
//		regularExpGen = new PelletExplanation(manager, Collections.singleton(ontology));
//		laconicExpGen = new LaconicExplanationGenerator(manager, new PelletReasonerFactory(), Collections.singleton(ontology));

	}
	
	public void setComputeLaconicExplanations(boolean laconic){
		laconicMode = laconic;
	}
	
	public boolean isLaconicMode(){
		return laconicMode;
	}

	@Override
	public Explanation getExplanation(OWLAxiom entailment){
		
		return getExplanations(entailment, 1).iterator().next();
	}

	@Override
	public Set<Explanation> getExplanations(OWLAxiom entailment){
		
		return getExplanations(entailment, -1);
	}

	@Override
	public Set<Explanation> getExplanations(OWLAxiom entailment, int limit){
		Set<Explanation> explanations = new HashSet<Explanation>();
		
		try {
			if(!laconicMode){
				explanations = computeRegularExplanations(entailment, limit);
			} else {
				explanations = computeLaconicExplanations(entailment, limit);
			}
		} catch (ExplanationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return explanations;
	}
	
	private Set<Explanation> computeLaconicExplanations(OWLAxiom entailment, int limit) throws ExplanationException{
		
		Set<Explanation> explanations = laconicExplanationCache.get(entailment);
		Integer lastRequestedSize = lastRequestedLaconicSize.get(entailment);
		if(lastRequestedSize == null){
            lastRequestedSize = Integer.valueOf(0);
		}
		if(explanations == null || lastRequestedSize.intValue() != -1 && lastRequestedSize.intValue() < limit){
			OWLOntology module = axiom2Module.get(entailment);
			if(module == null){
				module = OntologyUtils.getOntologyFromAxioms(ModularityUtils.extractModule(Collections.singleton(ontology), entailment.getSignature(), ModuleType.TOP_OF_BOT));
			}
			axiom2Module.put(entailment, module);
			laconicExpGen = new LaconicExplanationGenerator(manager, new PelletReasonerFactory(), Collections.singleton(module));
			if(limit == -1){
				explanations = laconicExpGen.getExplanations(entailment);
			} else {
				explanations = laconicExpGen.getExplanations(entailment, limit);
			}
			laconicExplanationCache.put(entailment, explanations);
			lastRequestedLaconicSize.put(entailment, Integer.valueOf(limit));
		}
		return explanations;
	}
	
	private Set<Explanation> computeRegularExplanations(OWLAxiom entailment, int limit) throws ExplanationException{
		Set<Explanation> explanations = regularExplanationCache.get(entailment);
		Integer lastRequestedSize = lastRequestedRegularSize.get(entailment);
		if(lastRequestedSize == null){
            lastRequestedSize = Integer.valueOf(0);
		}
		if(explanations == null || lastRequestedSize.intValue() != -1 && lastRequestedSize.intValue() < limit){
			OWLOntology module = axiom2Module.get(entailment);
			if(module == null){
				module = OntologyUtils.getOntologyFromAxioms(ModularityUtils.extractModule(Collections.singleton(ontology), entailment.getSignature(), ModuleType.TOP_OF_BOT));
			}
			axiom2Module.put(entailment, module);
			regularExpGen = new PelletExplanationGenerator(manager, Collections.singleton(module));
			if(limit == -1){
				explanations = regularExpGen.getExplanations(entailment);
			} else {
				explanations = regularExpGen.getExplanations(entailment, limit);
			}
			regularExplanationCache.put(entailment, explanations);
			lastRequestedRegularSize.put(entailment, Integer.valueOf(limit));
		}
		return explanations;
	}

	@Override
	public void repairPlanChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
//		Map<OWLAxiom, Set<Explanation>> copy = new HashMap<OWLAxiom, Set<Explanation>>();
//		for(OWLOntologyChange change : changes){
//			if(change instanceof RemoveAxiom){
//				for(Entry<OWLAxiom, Set<Explanation>> entry: regularExplanationCache.entrySet()){
//					Set<Explanation> explanationsCopy = new HashSet<Explanation>();
//					for(Explanation explanation : entry.getValue()){
//						if(explanation.getAxioms().contains(change.getAxiom())){
//							explanationsCopy.add(explanation);
//						}
//					}
//					if(!explanationsCopy.isEmpty()){
//						copy.put(entry.getKey(), explanationsCopy);
//					}
//					
//					
//				}
//			}
//		}
//		for(Entry<OWLAxiom, Set<Explanation>> copyEntry : copy.entrySet()){
//			regularExplanationCache.get(copyEntry.getKey()).removeAll(copyEntry.getValue());
//			
//		}
		regularExplanationCache.clear();
		laconicExplanationCache.clear();
		
		axiom2Module.clear();
		lastRequestedRegularSize.clear();
		lastRequestedLaconicSize.clear();
		
	}
	
	public Set<OWLAxiom> getSourceAxioms(OWLAxiom ax){
		return laconicExpGen.getSourceAxioms(ax);
	}
	
	public Set<OWLAxiom> getRemainingAxioms(OWLAxiom source, OWLAxiom part){
		return laconicExpGen.getRemainingAxioms(source, part);
	}
}
