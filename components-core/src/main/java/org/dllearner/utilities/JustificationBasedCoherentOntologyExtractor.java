package org.dllearner.utilities;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindswap.pellet.RBox;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.ModularityUtils;
import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapiv3.OntologyUtils;

public class JustificationBasedCoherentOntologyExtractor implements CoherentOntologyExtractor{

	private static final int NUMBER_OF_JUSTIFICATIONS = 2;
//	private PelletReasoner reasoner;
	private IncrementalClassifier reasoner;

	private OWLOntology incoherentOntology;
	private OWLOntology ontology;
	
	private Map<OWLClass, OWLOntology> cls2ModuleMap;
	
	static {PelletExplanation.setup();}
	
	@Override
	public OWLOntology getCoherentOntology(OWLOntology ontology) {
		this.ontology = ontology;
		this.incoherentOntology = getOntologyWithoutAnnotations(ontology);
		
//		reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(incoherentOntology);
//		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		reasoner = new IncrementalClassifier(incoherentOntology);
		reasoner.classify();
		
		OWLOntologyManager man = incoherentOntology.getOWLOntologyManager();
//		man.addOntologyChangeListener(reasoner);
		
		Set<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		
		//if the ontology is not incoherent we return it here
		if(unsatClasses.isEmpty()){
			return incoherentOntology;
		}
		
		cls2ModuleMap = extractModules(unsatClasses);
		
		while(!unsatClasses.isEmpty()){
			//for each unsatisfiable class we compute n justifications here and count how often each axiom occurs globally
			Map<OWLAxiom, Integer> axiom2CountMap = new HashMap<OWLAxiom, Integer>();
			for(OWLClass unsatClass : unsatClasses){
				Set<Set<OWLAxiom>> explanations = computeExplanations(unsatClass);
				for(Set<OWLAxiom> explanation : explanations){
					for(OWLAxiom ax : explanation){
						Integer cnt = axiom2CountMap.get(ax);
						if(cnt == null){
							cnt = 0;
						}
						cnt = cnt + 1;
						axiom2CountMap.put(ax, cnt);
					}
				}
			}
			//get a sorted list of entries with the highest axiom count first
			List<Entry<OWLAxiom, Integer>> sortedEntries = MapUtils.sortByValues(axiom2CountMap);
			for(Entry<OWLAxiom, Integer> entry : sortedEntries){
				System.out.println(entry.getKey() + ":" + entry.getValue());
			}
			//we remove the most occuring axiom
			OWLAxiom toRemove = sortedEntries.get(0).getKey();
			man.removeAxiom(incoherentOntology, toRemove);
			man.applyChange(new RemoveAxiom(incoherentOntology, toRemove));
			reasoner.classify();
			unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		}
		System.out.println(incoherentOntology.getLogicalAxiomCount());
		
		return getOntologyWithAnnotations(incoherentOntology);
	}
	
	private OWLOntology getOntologyWithoutAnnotations(OWLOntology ontology){
		try {
			OWLOntologyManager man = ontology.getOWLOntologyManager();
			OWLOntology ontologyWithoutAnnotations = ontology.getOWLOntologyManager().createOntology();
			for(OWLAxiom ax : ontology.getLogicalAxioms()){
				man.addAxiom(ontologyWithoutAnnotations, ax.getAxiomWithoutAnnotations());
			}
			return ontologyWithoutAnnotations;
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private OWLOntology getOntologyWithAnnotations(OWLOntology ontologyWithOutAnnotations){
		OWLOntologyManager man = ontology.getOWLOntologyManager();
		for (Iterator<OWLLogicalAxiom> iterator = ontology.getLogicalAxioms().iterator(); iterator.hasNext();) {
			OWLLogicalAxiom axiom = iterator.next();
			if(!ontologyWithOutAnnotations.containsAxiomIgnoreAnnotations(axiom)){
				man.removeAxiom(ontology, axiom);
			}
		}
		return ontology;
	}
	
	private Set<Set<OWLAxiom>> computeExplanations(OWLClass unsatClass){
		PelletExplanation expGen = new PelletExplanation(cls2ModuleMap.get(unsatClass));
		return expGen.getUnsatisfiableExplanations(unsatClass, NUMBER_OF_JUSTIFICATIONS);
	}
	
	private Map<OWLClass, OWLOntology> extractModules(Set<OWLClass> classes){
		Map<OWLClass, OWLOntology> cls2ModuleMap = new HashMap<OWLClass, OWLOntology>();
		for(OWLClass cls : classes){
			OWLOntology module = OntologyUtils.getOntologyFromAxioms(
					ModularityUtils.extractModule(incoherentOntology, Collections.singleton((OWLEntity)cls), ModuleType.TOP_OF_BOT));
			cls2ModuleMap.put(cls, module);
		}
		return cls2ModuleMap;
	}
	
	public static void main(String[] args) throws Exception{
		Logger.getLogger(RBox.class.getName()).setLevel(Level.OFF);
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
//		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("../components-core/cohaerent.owl"));
//		System.out.println(schema.getLogicalAxiomCount());
		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("/home/lorenz/arbeit/dbpedia_0.75_no_datapropaxioms.owl"));
		
		JustificationBasedCoherentOntologyExtractor extractor = new JustificationBasedCoherentOntologyExtractor();
		OWLOntology coherentOntology = extractor.getCoherentOntology(schema);System.out.println(coherentOntology.getLogicalAxiomCount());
	}

}
