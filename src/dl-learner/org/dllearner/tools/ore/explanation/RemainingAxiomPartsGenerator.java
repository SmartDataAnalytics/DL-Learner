package org.dllearner.tools.ore.explanation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dllearner.tools.ore.explanation.laconic.OPlus;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;

public class RemainingAxiomPartsGenerator {
	private OPlus oPlusGen;
	private Set<OWLOntology> ontologies;
	private OWLDataFactory dataFactory;
	
	private Map<OWLAxiom, Set<OWLAxiom>> source2AxiomsMap = new HashMap<OWLAxiom, Set<OWLAxiom>>();
	
	private Map<OWLAxiom, Map<OWLAxiom, Set<OWLAxiom>>> axiom2RemainingAxiomsMap = new HashMap<OWLAxiom, Map<OWLAxiom, Set<OWLAxiom>>>();
	
	public RemainingAxiomPartsGenerator(Set<OWLOntology> ontologies, OWLDataFactory factory){
		this.ontologies = ontologies;
		this.dataFactory = factory;
		
		oPlusGen = new OPlus(dataFactory);
		
	}
	
	public RemainingAxiomPartsGenerator(OWLOntology ontology, OWLDataFactory factory){
		this.ontologies = Collections.singleton(ontology);
		this.dataFactory = factory;
		
		oPlusGen = new OPlus(dataFactory);
		
	}
	
	public Map<OWLAxiom, Set<OWLAxiom>> getRemainingAxiomParts(OWLAxiom laconicAxiom){
		Set<OWLAxiom> oplus;
		Set<OWLAxiom> sourceAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> remainingAxioms;
		
		Map<OWLAxiom,Set<OWLAxiom>> source2RemainingAxiomsMap = axiom2RemainingAxiomsMap.get(laconicAxiom);
		if(source2RemainingAxiomsMap == null){
			
			source2RemainingAxiomsMap = new HashMap<OWLAxiom, Set<OWLAxiom>>();
			axiom2RemainingAxiomsMap.put(laconicAxiom, source2RemainingAxiomsMap);
			
			for(OWLOntology ont : ontologies){
				for(OWLAxiom ax : ont.getAxioms()){
					oplus = source2AxiomsMap.get(ax);
					if(oplus == null){
						oplus = ax.accept(oPlusGen);
						source2AxiomsMap.put(ax, oplus);
					}
					if(oplus.contains(laconicAxiom)){
						sourceAxioms.add(ax);
						continue;
					} else {
						for(OWLAxiom part : laconicAxiom.accept(oPlusGen)){
							if(oplus.contains(part)){
				    			sourceAxioms.add(ax);
				    			break;
				    		}
						}
					}
					
		    		
		    	}
			}
//			System.out.println("Source axioms: " + sourceAxioms);
			for(OWLAxiom sourceAx : sourceAxioms){
				 remainingAxioms = new HashSet<OWLAxiom>();
//	    		System.out.println("Source axiom: " + sourceAx);
	    		Set<OWLAxiom> temp = new HashSet<OWLAxiom>(source2AxiomsMap.get(sourceAx));
	    		Set<OWLAxiom> laconicAxiomParts = laconicAxiom.accept(oPlusGen);
	    		temp.removeAll(laconicAxiomParts);
	    		for(Iterator<OWLAxiom> i = temp.iterator();i.hasNext();){
	    			OWLAxiom ax = i.next();
	    			for(OWLAxiom laconicAxiomPart : laconicAxiomParts){
	    				if(ax.accept(oPlusGen).contains(laconicAxiomPart)){
//		    				System.out.println(ax);
		    				i.remove();
		    				break;
		    			}
	    			}
	    			
	    		}
	    		remainingAxioms.addAll(temp);
	    		for(OWLAxiom ax : temp){
//	    			System.out.println("Temp: " + ax);
	    			for(OWLAxiom a : ax.accept(oPlusGen)){
//	    				System.out.println(a);
	    				if(temp.contains(a) && !a.equals(ax)){
	    					remainingAxioms.remove(a);
	    				}
	    			}
	    		}
	    		source2RemainingAxiomsMap.put(sourceAx, remainingAxioms);
//	        	System.out.println("Remaining axioms: " + remainingAxioms);
	    	}
		}
		
		
		
		return source2RemainingAxiomsMap;
		
	}
	
	public void clear(){
		source2AxiomsMap.clear();
		axiom2RemainingAxiomsMap.clear();
	}
	

}
