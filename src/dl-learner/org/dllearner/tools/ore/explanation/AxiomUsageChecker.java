package org.dllearner.tools.ore.explanation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;

public class AxiomUsageChecker {
	
	private OWLOntology ontology;
	private Map<OWLAxiom, Set<OWLEntity>> axiom2UsageMap;
	
	public AxiomUsageChecker(OWLOntology ontology){
		this.ontology = ontology;
		axiom2UsageMap = new HashMap<OWLAxiom, Set<OWLEntity>>();
	}

	private Set<OWLEntity> computeUsage(OWLAxiom axiom) {
		Set<OWLEntity> usage = new HashSet<OWLEntity>();
		for(OWLEntity ent : axiom.getSignature()){
			for(OWLAxiom ax : ontology.getLogicalAxioms()){
				if(ax.getSignature().contains(ent)){
					usage.addAll(ax.getSignature());
				}
			}
		}
		
		return usage;
	}
	
	public Set<OWLEntity> getUsage(OWLAxiom axiom){
		Set<OWLEntity> usage = axiom2UsageMap.get(axiom);
		if(usage == null){
			usage = computeUsage(axiom);
			axiom2UsageMap.put(axiom, usage);
		}
		return usage;
	}
}
