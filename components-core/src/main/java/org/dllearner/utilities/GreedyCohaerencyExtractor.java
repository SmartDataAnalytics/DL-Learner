package org.dllearner.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.modularity.IncrementalClassifier;

public class GreedyCohaerencyExtractor {
	
	public GreedyCohaerencyExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	public OWLOntology getCoharentOntology(OWLOntology ontology) throws OWLOntologyCreationException{
		IncrementalClassifier reasoner = new IncrementalClassifier(ontology);
		reasoner.classify();
		
		BidiMap<AxiomType<? extends OWLAxiom>, Integer> axiomType2CountMap = getAxiomTypeCount(ontology);
		
		Map<AxiomType<? extends OWLAxiom>, List<OWLAxiom>> axiomType2AxiomsMap = new HashMap<AxiomType<? extends OWLAxiom>, List<OWLAxiom>>();
		for(AxiomType<? extends OWLAxiom> type : AxiomType.AXIOM_TYPES){
			axiomType2AxiomsMap.put(type, new ArrayList<OWLAxiom>(ontology.getAxioms(type)));
		}
		
		int lcm = lcm(new ArrayList<Integer>(axiomType2CountMap.values()));
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		man.addOntologyChangeListener(reasoner);
		OWLOntology cohaerentOntology = man.createOntology();
		
		boolean isCohaerent = true;
		for(int i = 0; i < lcm; i++){
			if(isCohaerent){
				for(Entry<AxiomType<? extends OWLAxiom>, Integer> entry : axiomType2CountMap.entrySet()){
					if((i % entry.getValue()) == 0){
						OWLAxiom ax = axiomType2AxiomsMap.get(entry.getKey()).remove(0);
						man.addAxiom(cohaerentOntology, ax);
						isCohaerent = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().isEmpty();
						if(!isCohaerent){
							man.removeAxiom(cohaerentOntology, ax);
							break;
						}
					}
				}
			}
		}
		return cohaerentOntology;
	}
	
	public OWLOntology getCoharentOntology(OWLReasoner reasoner) throws OWLOntologyCreationException{
		return getCoharentOntology(reasoner.getRootOntology());
	}
	
	private BidiMap<AxiomType<? extends OWLAxiom>, Integer> getAxiomTypeCount(OWLOntology ontology){
		BidiMap<AxiomType<? extends OWLAxiom>, Integer> axiomType2CountMap = new DualHashBidiMap<AxiomType<? extends OWLAxiom>, Integer>();
		
		for(AxiomType<? extends OWLAxiom> type : AxiomType.AXIOM_TYPES){
			axiomType2CountMap.put(type, ontology.getAxiomCount(type));
		}
		
		return axiomType2CountMap;
	}
	
	private int lcm(int x1,int x2) {
	      if(x1<=0 || x2<=0) {
	          throw new IllegalArgumentException("Cannot compute the least "+
	                                             "common multiple of two "+
	                                             "numbers if one, at least,"+
	                                             "is negative.");
	      }
	      int max,min;
	      if (x1>x2) {
	          max = x1;
	          min = x2;
	      } else {
	          max = x2;
	          min = x1;
	      }
	      for(int i=1; i<=min; i++) {
	          if( (max*i)%min == 0 ) {
	              return i*max;
	          }
	      }
	      throw new Error("Cannot find the least common multiple of numbers "+
	                      x1+" and "+x2);
	  }
	
	private int lcm(List<Integer> values) {
		if(values.size() == 1){
			return values.get(0);
		} else {
			List<Integer> list = new ArrayList<Integer>();
			list.add(lcm(values.get(0), values.get(1)));
			if(values.size() > 2){
				list.addAll(values.subList(2, values.size()));
			}
			return lcm(list);
		}
	}

}
