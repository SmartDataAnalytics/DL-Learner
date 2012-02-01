package org.dllearner.utilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import openlink.util.MD5;

import org.mindswap.pellet.RBox;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.ModularityUtils;
import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapiv3.OntologyUtils;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

public class JustificationBasedCoherentOntologyExtractor implements CoherentOntologyExtractor{
	
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JustificationBasedCoherentOntologyExtractor.class);

	private int numberOfJustifications = 10;
//	private PelletReasoner reasoner;
	private IncrementalClassifier reasoner;
	private Reasoner hermitReasoner;

	private OWLOntology incoherentOntology;
	private OWLOntology ontology;
	private OWLDataFactory factory;
	
	private Map<OWLClass, OWLOntology> cls2ModuleMap = new HashMap<OWLClass, OWLOntology>();
	private Map<OWLEntity, OWLOntology> entity2ModuleMap = new HashMap<OWLEntity, OWLOntology>();
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
	MessageDigest md5;
	
	private Set<OWLTransitiveObjectPropertyAxiom> removedTransitiveAxioms;
	private Set<OWLObjectProperty> unsatObjectProperties;
	
	public JustificationBasedCoherentOntologyExtractor() {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	static {PelletExplanation.setup();}
	
	@Override
	public OWLOntology getCoherentOntology(OWLOntology ontology){
		return getCoherentOntology(ontology, false);
	}
	
	@Override
	public OWLOntology getCoherentOntology(OWLOntology ontology, boolean preferRoots){
		this.ontology = ontology;
		this.incoherentOntology = getOntologyWithoutAnnotations(ontology);
		
		//only for debugging
		removedTransitiveAxioms = ontology.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY);
		incoherentOntology.getOWLOntologyManager().removeAxioms(ontology, removedTransitiveAxioms);
		
		manager = incoherentOntology.getOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		
		long startTime = System.currentTimeMillis();
		reasoner = new IncrementalClassifier(incoherentOntology);
		reasoner.classify();
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		//compute the unsatisfiable object properties
		unsatObjectProperties = getUnsatisfiableObjectProperties(reasoner);
		
		//start main process, either preferring root classes or not
		if(preferRoots){
			return computeCoherentOntologyRootBased(incoherentOntology);
		} else {
			return computeCoherentOntology(incoherentOntology);
		}
	}
	
	private OWLOntology computeCoherentOntologyRootBased(OWLOntology ontology) {
		
//		startTime = System.currentTimeMillis();
//		hermitReasoner = new Reasoner(incoherentOntology);
//		hermitReasoner.classifyClasses();
//		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		OWLOntologyManager man = incoherentOntology.getOWLOntologyManager();
		factory = man.getOWLDataFactory();
//		man.addOntologyChangeListener(reasoner);
		
		//compute the unsatisfiable classes
		logger.info("Computing root/derived unsatisfiable classes...");
		long startTime = System.currentTimeMillis();
		StructureBasedRootClassFinder rootFinder = new StructureBasedRootClassFinder(reasoner);
		Set<OWLClass> unsatClasses = rootFinder.getRootUnsatisfiableClasses();
		Set<OWLClass> derivedUnsatClasses = rootFinder.getDerivedUnsatisfiableClasses();
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
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
		startTime = System.currentTimeMillis();
		cls2ModuleMap = extractModules(unsatClasses);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
				
		//compute initial explanations for each unsatisfiable class
		logger.info("Computing initial explanations...");
		startTime = System.currentTimeMillis();
		Map<OWLClass, Set<Set<OWLAxiom>>> cls2Explanations = getInitialExplanationsForUnsatClasses(unsatClasses);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		while(!unsatClasses.isEmpty()){
			//get frequency for each axiom
			Map<OWLAxiom, Integer> axiom2CountMap = getAxiomFrequency(cls2Explanations);
			
			//get a sorted list of entries with the highest axiom count first
			List<Entry<OWLAxiom, Integer>> sortedEntries = MapUtils.sortByValues(axiom2CountMap);
			for(Entry<OWLAxiom, Integer> entry : sortedEntries){
//				System.out.println(entry.getKey() + ":" + entry.getValue());
			}
			//we remove the most frequent axiom from the ontology
			OWLAxiom toRemove = sortedEntries.get(0).getKey();
			logger.info("Removing axiom " + toRemove + ".");
			man.removeAxiom(incoherentOntology, toRemove);
			man.applyChange(new RemoveAxiom(incoherentOntology, toRemove));
			removeFromExplanations(cls2Explanations, toRemove);
			removeFromModules(toRemove);
			
			//recompute the unsatisfiable classes
			logger.info("Reclassifying...");
			startTime = System.currentTimeMillis();
			reasoner.classify();
//			hermitReasoner.classifyClasses();
//			unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			
			logger.info("Computing root/derived unsatisfiable classes...");
			startTime = System.currentTimeMillis();
			rootFinder = new StructureBasedRootClassFinder(reasoner);
			unsatClasses = rootFinder.getRootUnsatisfiableClasses();
			derivedUnsatClasses = rootFinder.getDerivedUnsatisfiableClasses();
			rootCnt = unsatClasses.size();
			derivedCnt = derivedUnsatClasses.size();
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			
			logger.info("Remaining unsatisfiable classes: " + (rootCnt + derivedCnt) + "(" + rootCnt + " roots).");
			
			//save
			if(cnt - (rootCnt+derivedCnt) >= 10){
				cnt = rootCnt + derivedCnt;
				save("log/dbpedia_" + cnt + ".owl");
				cnt = rootCnt + derivedCnt;
			}
			
			//recompute explanations if necessary
			logger.info("Recomputing explanations...");
			startTime = System.currentTimeMillis();
			refillExplanations(unsatClasses, cls2Explanations);
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			
			System.gc();
		}
		try {
			incoherentOntology.getOWLOntologyManager().saveOntology(getOntologyWithAnnotations(incoherentOntology), new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream("log/dbpedia_coherent.owl")));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(incoherentOntology.getLogicalAxiomCount());
		
		return getOntologyWithAnnotations(incoherentOntology);
	}
	
	private OWLOntology computeCoherentOntology(OWLOntology ontology) {
		//compute the unsatisfiable classes
		logger.info("Computing unsatisfiable classes...");
		long startTime = System.currentTimeMillis();
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		Set<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		int cnt = unsatClasses.size();
		logger.info("Detected " + cnt + " unsatisfiable classes.");
		
		//if the ontology is not incoherent we return it here
		if(unsatClasses.isEmpty()){
			return incoherentOntology;
		}
		//compute the logical modules for each unsatisfiable class
		logger.info("Computing module for each unsatisfiable class...");
		startTime = System.currentTimeMillis();
		cls2ModuleMap = extractModules(unsatClasses);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
				
		//compute initial explanations for each unsatisfiable class
		logger.info("Computing initial explanations...");
		startTime = System.currentTimeMillis();
		Map<OWLClass, Set<Set<OWLAxiom>>> cls2Explanations = getInitialExplanationsForUnsatClasses(unsatClasses);
		Map<OWLObjectProperty, Set<Set<OWLAxiom>>> prop2Explanations = getInitialExplanationsForUnsatObjectProperties(unsatObjectProperties);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		while(!unsatClasses.isEmpty()){
			//get frequency for each axiom
			Map<OWLAxiom, Integer> axiom2CountMap = getAxiomFrequency(cls2Explanations);
			
			//get a sorted list of entries with the highest axiom count first
			List<Entry<OWLAxiom, Integer>> sortedEntries = MapUtils.sortByValues(axiom2CountMap);
			for(Entry<OWLAxiom, Integer> entry : sortedEntries){
//				System.out.println(entry.getKey() + ":" + entry.getValue());
			}
			//we remove the most frequent axiom from the ontology
			OWLAxiom toRemove = sortedEntries.get(0).getKey();
			logger.info("Removing axiom " + toRemove + ".");
			manager.removeAxiom(incoherentOntology, toRemove);
			manager.applyChange(new RemoveAxiom(incoherentOntology, toRemove));
			removeFromExplanations(cls2Explanations, toRemove);
			removeFromModules(toRemove);
			
			//recompute the unsatisfiable classes
			logger.info("Reclassifying...");
			startTime = System.currentTimeMillis();
			reasoner.classify();
			unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			logger.info("Remaining unsatisfiable classes: " + unsatClasses.size());
			
			//save
			if(cnt - unsatClasses.size() >= 10){
				cnt = unsatClasses.size();
				save("log/dbpedia_" + cnt + ".owl");
			}
			
			//recompute explanations if necessary
			logger.info("Recomputing explanations...");
			startTime = System.currentTimeMillis();
			refillExplanations(unsatClasses, cls2Explanations);
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			
			System.gc();
		}
		try {
			incoherentOntology.getOWLOntologyManager().saveOntology(getOntologyWithAnnotations(incoherentOntology), new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream("log/dbpedia_coherent.owl")));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(incoherentOntology.getLogicalAxiomCount());
		
		return getOntologyWithAnnotations(incoherentOntology);
	}
	
	private void save(String fileName){
		OWLOntology toSave = getOntologyWithAnnotations(incoherentOntology);
		try {
			toSave.getOWLOntologyManager().saveOntology(incoherentOntology, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream(fileName)));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private Set<OWLObjectProperty> getUnsatisfiableObjectProperties(IncrementalClassifier reasoner){
		SortedSet<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>();
		OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		for(OWLObjectProperty p : reasoner.getRootOntology().getObjectPropertiesInSignature()){
//			boolean satisfiable = reasoner.isSatisfiable(f.getOWLObjectExactCardinality(1, p));
			boolean satisfiable = reasoner.isSatisfiable(f.getOWLObjectSomeValuesFrom(p, factory.getOWLThing()));
			if(!satisfiable){
				properties.add(p);
			}
		}
		return properties;
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
			if(precomputedExplanations == null || precomputedExplanations.size() < numberOfJustifications){
				Set<Set<OWLAxiom>> newExplanations = computeExplanations(unsatClass, numberOfJustifications);
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
	
	private Map<OWLObjectProperty, Set<Set<OWLAxiom>>> getInitialExplanationsForUnsatObjectProperties(Set<OWLObjectProperty> unsatObjProperties){
		Map<OWLObjectProperty, Set<Set<OWLAxiom>>> prop2Explanations = new HashMap<OWLObjectProperty, Set<Set<OWLAxiom>>>();
		
		for(OWLObjectProperty unsatClass : unsatObjProperties){
			Set<Set<OWLAxiom>> explanations = computeExplanations(unsatClass);
			prop2Explanations.put(unsatClass, explanations);
		}
		
		return prop2Explanations;
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
		return expGen.getUnsatisfiableExplanations(unsatClass, numberOfJustifications);
	}
	
	private Set<Set<OWLAxiom>> computeExplanations(OWLObjectProperty unsatProp){
		PelletExplanation expGen = new PelletExplanation(getModule(unsatProp));
		return expGen.getUnsatisfiableExplanations(factory.getOWLObjectExactCardinality(1, unsatProp), numberOfJustifications);
	}
	
	private Set<Set<OWLAxiom>> computeExplanations(OWLClass unsatClass, int limit){
		PelletExplanation expGen = new PelletExplanation(getModule(unsatClass));
		return expGen.getUnsatisfiableExplanations(unsatClass, numberOfJustifications);
	}
	
	private Set<Set<OWLAxiom>> computeExplanationsBlackBox(OWLClass unsatClass, int limit){
		BlackBoxExplanation singleExpGen = new BlackBoxExplanation(incoherentOntology, new HermiTReasonerFactory(), hermitReasoner);
		HSTExplanationGenerator expGen = new HSTExplanationGenerator(singleExpGen);
		return expGen.getExplanations(unsatClass, limit);
	}
	
//	private Set<Set<OWLAxiom>> computeExplanationsBlackbox(OWLClass unsatClass, int limit){
//		BlackBoxExplanation b = new BlackBoxExplanation(incoherentOntology, reasonerFactory, hermitReasoner)
//		MultipleExplanationGenerator expGen = new HSTExplanationGenerator(b);
//		PelletExplanation expGen = new PelletExplanation(getModule(unsatClass));
//		return expGen.getUnsatisfiableExplanations(unsatClass, NUMBER_OF_JUSTIFICATIONS);
//	}
	
	private OWLOntology getModule(OWLClass cls){
		OWLOntology module = cls2ModuleMap.get(cls);
		new File("log").mkdir();
		if(module == null){
			md5.reset();
			md5.update((ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology).toString() + cls.toStringID()).getBytes());
			String hash = MD5.asHex(md5.digest());
			String filename = "log/" + hash + ".owl";
			File file = new File(filename);
			if(file.exists()){
				module = loadModule(file);
			} else {
				module = OntologyUtils.getOntologyFromAxioms(
						ModularityUtils.extractModule(incoherentOntology, Collections.singleton((OWLEntity)cls), ModuleType.TOP_OF_BOT));
				try {
					manager.saveOntology(module, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream(filename)));
				} catch (OWLOntologyStorageException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			cls2ModuleMap.put(cls, module);
		}
		return module;
	}
	
	private OWLOntology getModule(OWLEntity entity){
		OWLOntology module = entity2ModuleMap.get(entity);
		new File("log").mkdir();
		if(module == null){
			md5.reset();
			md5.update((ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology).toString() + entity.toStringID()).getBytes());
			String hash = MD5.asHex(md5.digest());
			String filename = "log/" + hash + ".owl";
			File file = new File(filename);
			if(file.exists()){
				module = loadModule(file);
			} else {
				module = OntologyUtils.getOntologyFromAxioms(
						ModularityUtils.extractModule(incoherentOntology, Collections.singleton(entity), ModuleType.TOP_OF_BOT));
				try {
					manager.saveOntology(module, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream(filename)));
				} catch (OWLOntologyStorageException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			entity2ModuleMap.put(entity, module);
		}
		return module;
	}
	
	
	private OWLOntology loadModule(File file){
		OWLOntology module = null;
		try {
			module = manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return module;
	}
	
//	private Map<OWLClass, OWLOntology> extractModules(Set<OWLClass> classes){
//		Map<OWLClass, OWLOntology> cls2ModuleMap = new HashMap<OWLClass, OWLOntology>();
//		for(OWLClass cls : classes){
//			OWLOntology module = getModule(cls);
//			cls2ModuleMap.put(cls, module);
//		}
//		return cls2ModuleMap;
//	}
//	
//	private Map<OWLObjectProperty, OWLOntology> extractModules(Set<OWLObjectProperty> objectProperties){
//		Map<OWLObjectProperty, OWLOntology> prop2ModuleMap = new HashMap<OWLObjectProperty, OWLOntology>();
//		for(OWLObjectProperty prop : objectProperties){
//			OWLOntology module = getModule(prop);
//			prop2ModuleMap.put(prop, module);
//		}
//		return prop2ModuleMap;
//	}
	
	private <T extends OWLEntity> Map<T, OWLOntology> extractModules(Set<T> entities){
		Map<T, OWLOntology> entity2ModuleMap = new HashMap<T, OWLOntology>();
		for(T entity : entities){
			OWLOntology module = getModule(entity);
			entity2ModuleMap.put(entity, module);
		}
		return entity2ModuleMap;
	}
	
	public void setNumberOfJustifications(int numberOfJustifications) {
		this.numberOfJustifications = numberOfJustifications;
	}
	
	public static void main(String[] args) throws Exception{
		Logger.getLogger(RBox.class.getName()).setLevel(Level.OFF);
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		
		if(args.length != 3){
			System.out.println("USAGE: JustificationBasedCoherentOntologyExtractor <incoherent.owl> <numberOfJustifcations> <preferRootClasses(true|false)>");
			System.exit(0);
		}
		String filename = args[0];
		int numberOfJustifications = Integer.parseInt(args[1]);
		boolean preferRoots = Boolean.valueOf(args[2]);
		
		System.out.println("Loading ontology...");
		File file = new File(filename);
		OWLOntology schema = man.loadOntologyFromOntologyDocument(file);
		man.removeAxioms(schema, schema.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY));
		
//		OWLOntology cleaned = man.createOntology(IRI.create("http://dbpedia_cleaned.owl"));
//		man.addAxioms(cleaned, schema.getLogicalAxioms());
//		man.removeAxioms(cleaned, cleaned.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY));
//		man.removeAxioms(cleaned, cleaned.getAxioms(AxiomType.REFLEXIVE_OBJECT_PROPERTY));
//		man.removeAxioms(cleaned, cleaned.getAxioms(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY));
//		man.removeAxioms(cleaned, cleaned.getAxioms(AxiomType.SYMMETRIC_OBJECT_PROPERTY));
//		man.removeAxioms(cleaned, cleaned.getAxioms(AxiomType.ASYMMETRIC_OBJECT_PROPERTY));
//		man.removeAxioms(cleaned, cleaned.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY));
//		man.removeAxioms(cleaned, cleaned.getAxioms(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY));
//		man.saveOntology(cleaned, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream(file.getParent() + "/cleaned.owl")));
//		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("log/dbpedia_95.owl"));
//		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("/home/lorenz/arbeit/dbpedia_0.75_no_datapropaxioms.owl"));
//		System.out.println(schema.getLogicalAxiomCount());
//		OWLOntology schema = man.loadOntologyFromOntologyDocument(new File("log/dbpedia_coherent.owl"));
//		System.out.println(schema.getLogicalAxiomCount());
		System.out.println("...done.");
		
		JustificationBasedCoherentOntologyExtractor extractor = new JustificationBasedCoherentOntologyExtractor();
		extractor.setNumberOfJustifications(numberOfJustifications);
		OWLOntology coherentOntology = extractor.getCoherentOntology(schema, preferRoots);
		System.out.println("Coherent ontology contains " + coherentOntology.getLogicalAxiomCount() + " logical axioms.");
	}
	
	class HermiTReasonerFactory implements OWLReasonerFactory{

		@Override
		public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
			return new Reasoner(ontology);
		}

		@Override
		public OWLReasoner createNonBufferingReasoner(OWLOntology ontology,
				OWLReasonerConfiguration config)
				throws IllegalConfigurationException {
			return new Reasoner((Configuration) config, ontology);
		}

		@Override
		public OWLReasoner createReasoner(OWLOntology ontology) {
			return new Reasoner(ontology);
		}

		@Override
		public OWLReasoner createReasoner(OWLOntology ontology,
				OWLReasonerConfiguration config)
				throws IllegalConfigurationException {
			return new Reasoner((Configuration) config, ontology);
		}

		@Override
		public String getReasonerName() {
			return "HermiT Reasoner";
		}
		
	}

}
