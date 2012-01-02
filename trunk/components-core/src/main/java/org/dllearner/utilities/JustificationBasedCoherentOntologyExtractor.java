package org.dllearner.utilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.ModularityUtils;
import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapiv3.OntologyUtils;

public class JustificationBasedCoherentOntologyExtractor implements CoherentOntologyExtractor{
	
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JustificationBasedCoherentOntologyExtractor.class);

	private static final int NUMBER_OF_JUSTIFICATIONS = 5;
//	private PelletReasoner reasoner;
	private IncrementalClassifier reasoner;

	private OWLOntology incoherentOntology;
	private OWLOntology ontology;
	
	private Map<OWLClass, OWLOntology> cls2ModuleMap = new HashMap<OWLClass, OWLOntology>();
	
	static {PelletExplanation.setup();}
	
	@Override
	public OWLOntology getCoherentOntology(OWLOntology ontology) {
		this.ontology = ontology;ontology.getOWLOntologyManager().removeAxioms(ontology, ontology.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY));
		this.incoherentOntology = getOntologyWithoutAnnotations(ontology);
		
//		reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(incoherentOntology);
//		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		reasoner = new IncrementalClassifier(incoherentOntology);
		reasoner.classify();
		
		OWLOntologyManager man = incoherentOntology.getOWLOntologyManager();
//		man.addOntologyChangeListener(reasoner);
		
		//compute the unsatisfiable classes
		StructureBasedRootClassFinder rootFinder = new StructureBasedRootClassFinder(reasoner);
		rootFinder.computeRootDerivedClasses();
		Set<OWLClass> unsatClasses = rootFinder.getRootUnsatisfiableClasses();
		Set<OWLClass> derivedUnsatClasses = rootFinder.getDerivedUnsatisfiableClasses();
		int rootCnt = unsatClasses.size();
		int derivedCnt = derivedUnsatClasses.size();
//		Set<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		int cnt = rootCnt + derivedCnt;
		logger.info("Detected " + cnt + " unsatisfiable classes, " + rootCnt + " of them as root.");
		
		//if the ontology is not incoherent we return it here
		if(unsatClasses.isEmpty()){
			return incoherentOntology;
		}
		//compute the logical modules for each unsatisfiable class
		logger.info("Computing module for each unsatisfiable class...");
		cls2ModuleMap = extractModules(unsatClasses);
		logger.info("...done.");
				
		//compute initial explanations for each unsatisfiable class
		logger.info("Computing initial explanations...");
		Map<OWLClass, Set<Set<OWLAxiom>>> cls2Explanations = getInitialExplanationsForUnsatClasses(unsatClasses);
		logger.info("...done.");
		
		while(!unsatClasses.isEmpty()){
			//get frequency for each axiom
			Map<OWLAxiom, Integer> axiom2CountMap = getAxiomFrequency(cls2Explanations);
			
			//get a sorted list of entries with the highest axiom count first
			List<Entry<OWLAxiom, Integer>> sortedEntries = MapUtils.sortByValues(axiom2CountMap);
			for(Entry<OWLAxiom, Integer> entry : sortedEntries){
				System.out.println(entry.getKey() + ":" + entry.getValue());
			}
			//we remove the most frequent axiom from the ontology
			OWLAxiom toRemove = sortedEntries.get(0).getKey();
			logger.info("Removing axiom " + toRemove + ".");
			man.removeAxiom(incoherentOntology, toRemove);
			man.applyChange(new RemoveAxiom(incoherentOntology, toRemove));
			removeFromExplanations(cls2Explanations, toRemove);
			removeFromModules(toRemove);
			
			//recompute the unsatisfiable classes
			reasoner.classify();
//			unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			
			rootFinder = new StructureBasedRootClassFinder(reasoner);
			rootFinder.computeRootDerivedClasses();
			unsatClasses = rootFinder.getRootUnsatisfiableClasses();
			rootCnt = unsatClasses.size();
			derivedUnsatClasses = rootFinder.getDerivedUnsatisfiableClasses();
			derivedCnt = derivedUnsatClasses.size();
			logger.info("Remaining unsatisfiable classes: " + (rootCnt + derivedCnt) + "(" + rootCnt + " roots).");
			
			//save
			if(cnt - (rootCnt+derivedCnt) >= 10){
				cnt = rootCnt + derivedCnt;
				OWLOntology toSave = getOntologyWithAnnotations(incoherentOntology);
				try {
					toSave.getOWLOntologyManager().saveOntology(incoherentOntology, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream("log/dbpedia_" + cnt + ".owl")));
				} catch (OWLOntologyStorageException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				cnt = rootCnt + derivedCnt;
			}
			
			//recompute explanations if necessary
			logger.info("Recomputing explanations...");
			refillExplanations(unsatClasses, cls2Explanations);
			logger.info("...done.");
			
			System.gc();
		}
		try {
			incoherentOntology.getOWLOntologyManager().saveOntology(incoherentOntology, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream("log/dbpedia_coherent.owl")));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(incoherentOntology.getLogicalAxiomCount());
		
		return getOntologyWithAnnotations(incoherentOntology);
	}
	
	private void removeFromModules(OWLAxiom axiom){
		OWLOntology module;
		for(Entry<OWLClass, OWLOntology> entry : cls2ModuleMap.entrySet()){
			module = entry.getValue();
			module.getOWLOntologyManager().removeAxiom(module, axiom);
		}
	}
	
	private void removeFromExplanations(Map<OWLClass, Set<Set<OWLAxiom>>> cls2Explanations, OWLAxiom axiom){
		for(Entry<OWLClass, Set<Set<OWLAxiom>>> entry : cls2Explanations.entrySet()){
			for (Iterator<Set<OWLAxiom>> iterator = entry.getValue().iterator(); iterator.hasNext();) {
				Set<OWLAxiom> explanation = iterator.next();
				if(explanation.contains(axiom)){
					iterator.remove();
				}
			}
		}
	}
	
	private void refillExplanations(Set<OWLClass> unsatClasses, Map<OWLClass, Set<Set<OWLAxiom>>> cls2Explanations){
		for(OWLClass unsatClass : unsatClasses){
			Set<Set<OWLAxiom>> precomputedExplanations = cls2Explanations.get(unsatClass);
			if(precomputedExplanations == null || precomputedExplanations.size() < NUMBER_OF_JUSTIFICATIONS){
				Set<Set<OWLAxiom>> newExplanations = computeExplanations(unsatClass, NUMBER_OF_JUSTIFICATIONS);
				cls2Explanations.put(unsatClass, newExplanations);
			}
		}
	}
	
	private Map<OWLAxiom, Integer> getAxiomFrequency(Map<OWLClass, Set<Set<OWLAxiom>>> cls2Explanations){
		Map<OWLAxiom, Integer> axiom2CountMap = new HashMap<OWLAxiom, Integer>();
		
		for(Entry<OWLClass, Set<Set<OWLAxiom>>> entry : cls2Explanations.entrySet()){
			for(Set<OWLAxiom> explanation : entry.getValue()){
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
		
		return axiom2CountMap;
	}
	
	private Map<OWLClass, Set<Set<OWLAxiom>>> getInitialExplanationsForUnsatClasses(Set<OWLClass> unsatClasses){
		Map<OWLClass, Set<Set<OWLAxiom>>> cls2Explanations = new HashMap<OWLClass, Set<Set<OWLAxiom>>>();
		
		for(OWLClass unsatClass : unsatClasses){
			Set<Set<OWLAxiom>> explanations = computeExplanations(unsatClass);
			cls2Explanations.put(unsatClass, explanations);
		}
		
		return cls2Explanations;
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
		PelletExplanation expGen = new PelletExplanation(getModule(unsatClass));
		return expGen.getUnsatisfiableExplanations(unsatClass, NUMBER_OF_JUSTIFICATIONS);
	}
	
	private Set<Set<OWLAxiom>> computeExplanations(OWLClass unsatClass, int limit){
		PelletExplanation expGen = new PelletExplanation(getModule(unsatClass));
		return expGen.getUnsatisfiableExplanations(unsatClass, NUMBER_OF_JUSTIFICATIONS);
	}
	
	private OWLOntology getModule(OWLClass cls){
		OWLOntology module = cls2ModuleMap.get(cls);
		if(module == null){
			module = OntologyUtils.getOntologyFromAxioms(
					ModularityUtils.extractModule(incoherentOntology, Collections.singleton((OWLEntity)cls), ModuleType.TOP_OF_BOT));
			cls2ModuleMap.put(cls, module);
		}
		return module;
	}
	
	private Map<OWLClass, OWLOntology> extractModules(Set<OWLClass> classes){
		Map<OWLClass, OWLOntology> cls2ModuleMap = new HashMap<OWLClass, OWLOntology>();
		for(OWLClass cls : classes){
			OWLOntology module = getModule(cls);
			cls2ModuleMap.put(cls, module);
		}
		return cls2ModuleMap;
	}
	
	public static void main(String[] args) throws Exception{
		Logger.getLogger(RBox.class.getName()).setLevel(Level.OFF);
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
//		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("/home/lorenz/dbpedia_0.75_no_datapropaxioms.owl"));
		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("log/dbpedia_95.owl"));
//		System.out.println(schema.getLogicalAxiomCount());
//		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("log/dbpedia_coherent.owl"));
		System.out.println(schema.getLogicalAxiomCount());
		
		JustificationBasedCoherentOntologyExtractor extractor = new JustificationBasedCoherentOntologyExtractor();
		OWLOntology coherentOntology = extractor.getCoherentOntology(schema);System.out.println(coherentOntology.getLogicalAxiomCount());
	}

}
