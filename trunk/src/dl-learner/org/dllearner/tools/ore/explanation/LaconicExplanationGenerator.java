
package org.dllearner.tools.ore.explanation;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import com.clarkparsia.explanation.PelletExplanation;

public class LaconicExplanationGenerator
{

    private PelletExplanation pelletExplanation;
    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private Set<Set<OWLAxiom>> lastRegularJusts;
    private Set<Set<OWLAxiom>> allPreviouslyFoundJustifications;
    private OPlus oPlus;
    
    public LaconicExplanationGenerator(OWLOntologyManager manager,
			OWLReasonerFactory reasonerFactory, Set<OWLOntology> ontologies) {

		this.manager = manager;

		try {
			ontology = manager.createOntology(URI.create(new StringBuilder().append(
					"http://laconic").append(System.nanoTime()).toString()),
					ontologies, true);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
		
		pelletExplanation = new PelletExplanation(manager, ontologies);
		lastRegularJusts = new HashSet<Set<OWLAxiom>>();
	}
    
    /**
     * Computes a more fine grained representation for a set of axioms, which means to split them
     * e.g. for A \sqsubseteq B \sqcap C returning A \sqsubseteq B and A \sqsubseteq C
     * @param axioms to split
     * @return splitted axioms
     */
    public Set<OWLAxiom> computeOPlus(Set<OWLAxiom> axioms) {
    	oPlus = new OPlus(manager.getOWLDataFactory());
    	Set<OWLAxiom> oPlusAxioms = new HashSet<OWLAxiom>();
    	
    	for(OWLAxiom ax : axioms){
    		Set<OWLAxiom> weakenedAxioms = ax.accept(oPlus);
    		oPlusAxioms.addAll(weakenedAxioms);
    	}
	   	return oPlusAxioms;
    }
    
        
    public Set<Set<OWLAxiom>> getLastRegularJustifications() {
    	return lastRegularJusts;
    }
    
    /**
     * Computes the precise explanations
     * @param entailment
     * @param limit
     * @return
     * @throws OWLException
     */
    public Set<Set<OWLAxiom>> computePreciseJusts(OWLAxiom entailment, int limit) throws OWLException {
	
	Set<Set<OWLAxiom>> regularJusts = pelletExplanation.getEntailmentExplanations((OWLAxiom)entailment);
	
	System.out.println(new StringBuilder().append
			       ("Got regular justifications: ").append
			       (regularJusts.size()).toString());
	lastRegularJusts.clear();
	lastRegularJusts.addAll(regularJusts);
	allPreviouslyFoundJustifications = new HashSet<Set<OWLAxiom>>();
	allPreviouslyFoundJustifications.addAll(regularJusts);
	Set<Set<OWLAxiom>> nonLaconicJusts = new HashSet<Set<OWLAxiom>>();
	Set<Set<OWLAxiom>> laconicJusts = new HashSet<Set<OWLAxiom>>();
	Set<OWLAxiom> axiomsInPreviousOntology = new HashSet<OWLAxiom>();
	long counter = 0L;
	for (;;) {
	    counter++;
	    System.out.println(new StringBuilder().append("Count ").append
				   (counter).toString());
	    Set<OWLAxiom> unionOfAllJustifications = new HashSet<OWLAxiom>();
	    for(Set<OWLAxiom> just : allPreviouslyFoundJustifications){
	    	unionOfAllJustifications.addAll(just);
	    }
	    
	    
//	    Set<OWLAxiom> lastOPlus = new HashSet<OWLAxiom>(computeOPlus(unionOfAllJustifications));
	    Set<OWLAxiom> oPlus = computeOPlus(unionOfAllJustifications);
	    OWLOntologyManager man2 = OWLManager.createOWLOntologyManager();
	    OWLOntology extendedOnt = man2.createOntology(oPlus);
	    for(OWLLogicalAxiom logAx : ontology.getLogicalAxioms()){
	    	if (!unionOfAllJustifications.contains(logAx) || oPlus.contains(logAx)){
			    man2.addAxiom(extendedOnt, logAx);
	    	}
	    }
	    	
	    if (extendedOnt.getLogicalAxioms().equals(axiomsInPreviousOntology)) {
	    	System.out.println("\t ***** No change in ontology. Early termination.");
	    	break;
	    }
//	    man2.saveOntology(extendedOnt, URI.create("file:/home/lorenz/neu.owl"));
	    axiomsInPreviousOntology.clear();
	    axiomsInPreviousOntology.addAll(extendedOnt.getLogicalAxioms());
	    Set<Set<OWLAxiom>> allPrevJustsCopy = new HashSet<Set<OWLAxiom>>(allPreviouslyFoundJustifications);
	   
	   
	    Set<OWLOntology> ont2 = new HashSet<OWLOntology>();
	    ont2.add(extendedOnt); 
	    PelletExplanation expGen = new PelletExplanation(man2, ont2);
	    Set<Set<OWLAxiom>> currentJustifications = expGen.getEntailmentExplanations((OWLAxiom)entailment);
	    
	    
	    allPreviouslyFoundJustifications.addAll(currentJustifications);
	    if (allPreviouslyFoundJustifications.equals(allPrevJustsCopy)){
	    	break;
		}
	    for(Set<OWLAxiom> currentJust : currentJustifications){
	    	if(!laconicJusts.contains(currentJust) && !nonLaconicJusts.contains(currentJust)){
	    		if(isLaconic(currentJust, entailment)){
	    			laconicJusts.add(currentJust);
	    		} else{
	    			nonLaconicJusts.add(currentJust);
	    		}
	    		if(laconicJusts.size() == limit){
	    			return retrieveAxioms(laconicJusts);
	    		}
	    	}
	    }
	   
	}
	Set<Set<OWLAxiom>> laconicJustifications = new HashSet<Set<OWLAxiom>>();
	for(Set<OWLAxiom> just : allPreviouslyFoundJustifications){
		if(!nonLaconicJusts.contains(just)){
			if(laconicJusts.contains(just)){
				laconicJustifications.add(just);
			} else if(isLaconic(just, entailment)){
				laconicJustifications.add(just);
			}
		}
	}
	
	
	return retrieveAxioms(laconicJustifications);
    }
    
    public boolean isLaconic(Set<OWLAxiom> justification, OWLAxiom entailment)
			throws ExplanationException {
		boolean laconic;
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

			Set<OWLAxiom> justificationSigmaClosure = computeOPlus(justification);

			OWLOntology justificationSigmaClosureOnt = manager
					.createOntology(justificationSigmaClosure);

			PelletExplanation expGen2 = new PelletExplanation(manager,
					Collections.singleton(justificationSigmaClosureOnt));
			Set<Set<OWLAxiom>> exps = expGen2.getEntailmentExplanations(
					entailment, Integer.MAX_VALUE);

			laconic = Collections.singleton(justification).equals(exps);

		} catch (OWLOntologyCreationException e) {
			throw new ExplanationException(e);
		} catch (OWLOntologyChangeException e) {
			throw new ExplanationException(e);
		}
		return laconic;
	}
    
    private Set<Set<OWLAxiom>> retrieveAxioms(Set<Set<OWLAxiom>> explanations) {

		Map<OWLAxiom, Set<OWLAxiom>> sourceAxioms2OPlus = new HashMap<OWLAxiom, Set<OWLAxiom>>();

		for (Set<OWLAxiom> just : allPreviouslyFoundJustifications) {
			for (OWLAxiom ax : just) {
				if (ontology.containsAxiom(ax)) {
					sourceAxioms2OPlus.put(ax, computeOPlus(Collections.singleton(ax)));
				}
			}
		}
		Set<Set<OWLAxiom>> reconstituedExplanations = new HashSet<Set<OWLAxiom>>();

		for (Set<OWLAxiom> expl : explanations) {
			Map<OWLClass, Map<OWLAxiom, Set<OWLSubClassAxiom>>> lhs2SubClassAxiom = new HashMap<OWLClass, Map<OWLAxiom, Set<OWLSubClassAxiom>>>();
			Set<OWLAxiom> reconstituedAxioms = new HashSet<OWLAxiom>();
			for (OWLAxiom laconicAx : expl) {
				if (laconicAx instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subAx = (OWLSubClassAxiom) laconicAx;
					if (subAx.getSubClass().isAnonymous()) {
						reconstituedAxioms.add(subAx);
					} else {
						Map<OWLAxiom, Set<OWLSubClassAxiom>> source2AxiomMap = lhs2SubClassAxiom.get(subAx.getSubClass().asOWLClass());
						if (source2AxiomMap == null) {
							source2AxiomMap = new HashMap<OWLAxiom, Set<OWLSubClassAxiom>>();
							lhs2SubClassAxiom.put(subAx.getSubClass().asOWLClass(), source2AxiomMap);
						}

						for (OWLAxiom sourceAxiom : sourceAxioms2OPlus.keySet()) {
							if ((sourceAxioms2OPlus.get(sourceAxiom)).contains(subAx)) {
								Set<OWLSubClassAxiom> subClassAxioms = source2AxiomMap.get(sourceAxiom);
								if (subClassAxioms == null) {
									subClassAxioms = new HashSet<OWLSubClassAxiom>();
									source2AxiomMap.put(sourceAxiom, subClassAxioms);
								}
								subClassAxioms.add(subAx);
							}
						}
					}
				} else {
					reconstituedAxioms.add(laconicAx);
				}
			}
			Set<OWLAxiom> consumedAxioms = new HashSet<OWLAxiom>();
			for (OWLClass lhs : (Set<OWLClass>) lhs2SubClassAxiom.keySet()) {
				Map<OWLAxiom, Set<OWLSubClassAxiom>> source2SubClassAxiom = lhs2SubClassAxiom.get(lhs);
				for (OWLAxiom source : source2SubClassAxiom.keySet()) {
					Set<OWLDescription> rightHandSides = new HashSet<OWLDescription>();
					for (OWLSubClassAxiom sub : source2SubClassAxiom.get(source)) {
						if (!consumedAxioms.contains(sub)) {
							rightHandSides.add(sub.getSuperClass());
							consumedAxioms.add(sub);
						}
					}

					if (rightHandSides.size() == 1)
						reconstituedAxioms.add(manager.getOWLDataFactory().getOWLSubClassAxiom((OWLDescription) lhs,((OWLDescription) rightHandSides.iterator().next())));
					else if (rightHandSides.size() > 1) {
						org.semanticweb.owl.model.OWLObjectIntersectionOf conjunction = manager.getOWLDataFactory().getOWLObjectIntersectionOf(rightHandSides);
						reconstituedAxioms.add(manager.getOWLDataFactory().getOWLSubClassAxiom((OWLDescription) lhs,conjunction));
					}
				}
			}

			reconstituedExplanations.add(reconstituedAxioms);

		}
		
		return reconstituedExplanations;
	}
    	    
    	
    
    public Set<Set<OWLAxiom>> getExplanations(OWLAxiom entailment) throws ExplanationException {
	Set<Set<OWLAxiom>> set;
	try {
	    set = computePreciseJusts(entailment, 2147483647);
	} catch (OWLException e) {
	    throw new ExplanationException(e);
	}
	return set;
    }
    
    public Set<Set<OWLAxiom>> getExplanations(OWLAxiom entailment, int limit)
	throws ExplanationException {
	Set<Set<OWLAxiom>> set;
	try {
	    set = computePreciseJusts(entailment, limit);
	} catch (OWLException e) {
	    throw new ExplanationException(e);
	}
	return set;
    }
    
    public Set<Set<OWLAxiom>> getRegularExplanations(OWLAxiom entailment) throws ExplanationException {
    	Set<Set<OWLAxiom>> regularJusts;
    	
    		regularJusts = pelletExplanation.getEntailmentExplanations((OWLAxiom)entailment);
    	lastRegularJusts.addAll(regularJusts);
    	return regularJusts;
        }
    
    public void returnSourceAxioms(Set<Set<OWLAxiom>> explanations){
    	Map<OWLAxiom, Set<OWLAxiom>> sourceMap = oPlus.getAxiomsMap();
    	System.out.println(sourceMap);
    	for(Set<OWLAxiom> explanation: explanations){
    		for(OWLAxiom ax : explanation){
    			System.out.println(ax + " gehört zu " + sourceMap.get(ax));
    			
    		}
    	}
    }

	
		
}
