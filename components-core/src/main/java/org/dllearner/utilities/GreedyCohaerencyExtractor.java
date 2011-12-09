package org.dllearner.utilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.modularity.IncrementalClassifier;

public class GreedyCohaerencyExtractor {
	
	private static final double STEP_SIZE = 0.001;
	private static final int ALLOWED_UNSATISFIABLE_CLASSES = 5;
	
	public GreedyCohaerencyExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	public OWLOntology getCoharentOntology(OWLOntology ontology) throws OWLOntologyCreationException{
		BidiMap<AxiomType<? extends OWLAxiom>, Integer> axiomType2CountMap = getAxiomTypeCount(ontology);
		
		Map<AxiomType<? extends OWLAxiom>, List<OWLAxiom>> axiomType2AxiomsMap = new HashMap<AxiomType<? extends OWLAxiom>, List<OWLAxiom>>();
		for(AxiomType<? extends OWLAxiom> type : AxiomType.AXIOM_TYPES){
			axiomType2AxiomsMap.put(type, new ArrayList<OWLAxiom>(ontology.getAxioms(type)));
		}
		System.out.println(ontology.getLogicalAxiomCount());
		double[] stepSize = new double[axiomType2CountMap.entrySet().size()];
		double[] cnt = new double[axiomType2CountMap.entrySet().size()];
		AxiomType[] type = new AxiomType[axiomType2CountMap.entrySet().size()];
		int i=0;
		for(Entry<AxiomType<? extends OWLAxiom>, Integer> entry : axiomType2CountMap.entrySet()){
			stepSize[i] = STEP_SIZE * entry.getValue();
			type[i] = entry.getKey();
			cnt[i] = 0;
			i++;
		}
		
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology cohaerentOntology = man.createOntology();
		
		IncrementalClassifier reasoner = new IncrementalClassifier(cohaerentOntology);
		man.addOntologyChangeListener(reasoner);
		reasoner.classify();
		
		
		boolean isCohaerent = true;
		for(double j = 0; j < 1; j += STEP_SIZE){System.out.println(j);
			if(isCohaerent){
				for(i = 0; i < stepSize.length; i++){
					cnt[i] = cnt[i] + stepSize[i];
					int x = (int)cnt[i];
					System.out.println("Adding " + x + " " + type[i] + " axioms from " + axiomType2CountMap.get(type[i]));
//					System.out.println(axiomType2AxiomsMap.get(type[i]).size());
//					for(int k = 0; k < x; k++){
//						OWLAxiom ax = axiomType2AxiomsMap.get(type[i]).remove(0);
//						man.addAxiom(cohaerentOntology, ax);
//						isCohaerent = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().isEmpty();
//						if(!isCohaerent){
//							man.removeAxiom(cohaerentOntology, ax);
//							break;
//						}
//					}
					Set<OWLAxiom> toAdd = new HashSet<OWLAxiom>(axiomType2AxiomsMap.get(type[i]).subList(0, x));
					man.addAxioms(cohaerentOntology, toAdd);
					axiomType2AxiomsMap.get(type[i]).removeAll(toAdd);
					isCohaerent = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size() <= ALLOWED_UNSATISFIABLE_CLASSES;
					if(!isCohaerent){
						man.removeAxioms(cohaerentOntology, toAdd);System.out.println("Incohaerency detected");
						break;
					}
					cnt[i] = cnt[i] - x;
				}
			}
			System.out.println(cohaerentOntology.getLogicalAxiomCount());
			
		}
		try {
			man.saveOntology(cohaerentOntology, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream(new File("coherent.owl"))));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cohaerentOntology;
	}
	
	public OWLOntology getCoharentOntology(OWLReasoner reasoner) throws OWLOntologyCreationException{
		return getCoharentOntology(reasoner.getRootOntology());
	}
	
	private BidiMap<AxiomType<? extends OWLAxiom>, Integer> getAxiomTypeCount(OWLOntology ontology){
		BidiMap<AxiomType<? extends OWLAxiom>, Integer> axiomType2CountMap = new DualHashBidiMap<AxiomType<? extends OWLAxiom>, Integer>();
		
		for(AxiomType<? extends OWLAxiom> type : AxiomType.AXIOM_TYPES){
			int cnt = ontology.getAxiomCount(type);
			if(cnt > 0){
				axiomType2CountMap.put(type, Integer.valueOf(cnt));
			}
			Set<? extends OWLAxiom> axioms = ontology.getAxioms(type);
		}
		
		return axiomType2CountMap;
	}
	
	public static void main(String[] args) throws Exception{
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("/home/lorenz/arbeit/dbpedia_0.75_no_datapropaxioms.owl"));
		
		GreedyCohaerencyExtractor ge = new GreedyCohaerencyExtractor();
		ge.getCoharentOntology(schema);
	}

}
