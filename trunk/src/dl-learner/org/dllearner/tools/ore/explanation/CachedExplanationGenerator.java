package org.dllearner.tools.ore.explanation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dllearner.tools.ore.explanation.laconic.LaconicExplanationGenerator;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.modularity.ModuleType;

import com.clarkparsia.explanation.PelletExplanation;
import com.clarkparsia.modularity.ModularityUtils;
import com.clarkparsia.owlapi.OntologyUtils;

public class CachedExplanationGenerator implements ExplanationGenerator{
	
	private Map<OWLAxiom, OWLOntology> axiom2Module;
	private Map<OWLAxiom, Set<Set<OWLAxiom>>> regularExplanationCache;
	private Map<OWLAxiom, Set<Set<OWLAxiom>>> laconicExplanationCache;
	private Map<OWLAxiom, Integer> lastRequestedRegularSize;
	private Map<OWLAxiom, Integer> lastRequestedLaconicSize;
	private boolean laconicMode = false;
	private PelletExplanation regularExpGen;
	private LaconicExplanationGenerator laconicExpGen;
	
	private OWLOntology ontology;
	private OWLOntologyManager manager;

	public CachedExplanationGenerator(OWLOntology ontology){
		this.ontology = ontology;
		this.manager = OWLManager.createOWLOntologyManager();
		
		axiom2Module = new HashMap<OWLAxiom, OWLOntology>();
		regularExplanationCache = new HashMap<OWLAxiom, Set<Set<OWLAxiom>>>();
		laconicExplanationCache = new HashMap<OWLAxiom, Set<Set<OWLAxiom>>>();
		lastRequestedRegularSize = new HashMap<OWLAxiom, Integer>();
		lastRequestedLaconicSize = new HashMap<OWLAxiom, Integer>();
//		regularExpGen = new PelletExplanation(manager, Collections.singleton(ontology));
//		laconicExpGen = new LaconicExplanationGenerator(manager, new PelletReasonerFactory(), Collections.singleton(ontology));

	}
	
	public void setComputeLaconicExplanations(boolean laconic){
		laconicMode = laconic;
	}

	@Override
	public Set<OWLAxiom> getExplanation(OWLAxiom entailment){
		
		return getExplanations(entailment, 1).iterator().next();
	}

	@Override
	public Set<Set<OWLAxiom>> getExplanations(OWLAxiom entailment){
		
		return getExplanations(entailment, -1);
	}

	@Override
	public Set<Set<OWLAxiom>> getExplanations(OWLAxiom entailment, int limit){
		Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
		
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
	
	private Set<Set<OWLAxiom>> computeLaconicExplanations(OWLAxiom entailment, int limit) throws ExplanationException{
		
		Set<Set<OWLAxiom>> explanations = laconicExplanationCache.get(entailment);
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
	
	private Set<Set<OWLAxiom>> computeRegularExplanations(OWLAxiom entailment, int limit) throws ExplanationException{
		Set<Set<OWLAxiom>> explanations = regularExplanationCache.get(entailment);
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
			regularExpGen = new PelletExplanation(manager, Collections.singleton(module));
			if(limit == -1){
				explanations = regularExpGen.getEntailmentExplanations(entailment);
			} else {
				explanations = regularExpGen.getEntailmentExplanations(entailment, limit);
			}
			regularExplanationCache.put(entailment, explanations);
			lastRequestedRegularSize.put(entailment, Integer.valueOf(limit));
		}
		return explanations;
	}
}
