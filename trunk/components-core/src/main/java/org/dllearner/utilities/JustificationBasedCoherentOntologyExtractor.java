package org.dllearner.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import openlink.util.MD5;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
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

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.ModularityUtils;
import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class JustificationBasedCoherentOntologyExtractor implements CoherentOntologyExtractor{
	
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JustificationBasedCoherentOntologyExtractor.class);
	private static final String DIFF_ONTOLOGY_NAME = "diff.owl";
	
	private int numberOfJustifications = 10;
//	private PelletReasoner reasoner;
	private IncrementalClassifier reasoner;
	private Reasoner hermitReasoner;

	private OWLOntology incoherentOntology;
	private OWLOntology ontology;
	private OWLDataFactory factory = new OWLDataFactoryImpl();;
	
	//we store the removed axioms in it
	private OWLOntology diffOntology;
	
	private Map<OWLEntity, OWLOntology> entity2ModuleMap = new HashMap<OWLEntity, OWLOntology>();
	private Map<OWLEntity, Set<Set<OWLAxiom>>> entity2Explanations = new HashMap<OWLEntity, Set<Set<OWLAxiom>>>();
	private Map<OWLEntity, PelletExplanation> entity2ExpGen = new HashMap<OWLEntity, PelletExplanation>();
	private Set<OWLEntity> entitiesWithLessExplanations = new HashSet<OWLEntity>();
	
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
	MessageDigest md5;
	
	private Set<OWLTransitiveObjectPropertyAxiom> removedTransitiveAxioms;
	private Set<OWLObjectProperty> unsatObjectProperties;
	
	//whether to debug classes and properties in parallel
	private boolean computeParallel = false;
	
	private OWLAnnotationProperty confidenceProperty;

	private OWLOntology dbpediaOntology;
	
	private String fileName = "dbpedia";
	private String diffFileName = "diff.owl";
	
	public JustificationBasedCoherentOntologyExtractor() {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		dbpediaOntology = loadDBpediaOntology();
	}
	
	static {PelletExplanation.setup();}
	
	@Override
	public OWLOntology getCoherentOntology(OWLOntology ontology){
		return getCoherentOntology(ontology, false);
	}
	
	@Override
	public OWLOntology getCoherentOntology(OWLOntology ontology, boolean preferRoots){
		ontology.getOWLOntologyManager().addAxioms(ontology, dbpediaOntology.getLogicalAxioms());
		
		this.ontology = ontology;
		this.incoherentOntology = getOntologyWithoutAnnotations(ontology);
		
		new File("log").mkdir();
		
		File diffFile = new File("log/" + DIFF_ONTOLOGY_NAME);
		try {
			if(diffFile.exists()){
				diffOntology = manager.loadOntologyFromOntologyDocument(diffFile);
			} else {
				diffOntology = manager.createOntology(IRI.create("http://diff.org/"));
			}
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		
		//only for debugging
		removedTransitiveAxioms = incoherentOntology.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY);
		incoherentOntology.getOWLOntologyManager().removeAxioms(incoherentOntology, removedTransitiveAxioms);
		
		manager = incoherentOntology.getOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		
		long startTime = System.currentTimeMillis();
		reasoner = new IncrementalClassifier(incoherentOntology);
		reasoner.classify();
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		//compute the unsatisfiable object properties and their corresponding modules
		unsatObjectProperties = getUnsatisfiableObjectProperties(reasoner);
		logger.info("Found unsatisfiable object properties: " + unsatObjectProperties.size());
		if(computeParallel){
			entity2ModuleMap.putAll(extractModules(unsatObjectProperties));
		}
		
		//start main process, either preferring root classes or not
		if(preferRoots){
			return computeCoherentOntologyRootBased(incoherentOntology);
		} else {
			return computeCoherentOntology(incoherentOntology);
		}
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
		diffFileName = "diff_" + fileName;
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
		StructureBasedRootClassFinder rootFinder = new StructureBasedRootClassFinder(reasoner, this);
		Set<OWLClass> unsatClasses = rootFinder.getRootUnsatisfiableClasses();
		Set<OWLClass> derivedUnsatClasses = rootFinder.getDerivedUnsatisfiableClasses();
		
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		int rootCnt = unsatClasses.size();
		int derivedCnt = derivedUnsatClasses.size();
		
		//if no roots are found we use all unsat classes
				if(rootCnt == 0){
					unsatClasses = derivedUnsatClasses;
				}
				
//		Set<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		int cnt = rootCnt + derivedCnt;
		int unsatPropCnt = unsatObjectProperties.size();
		logger.info("Detected " + cnt + " unsatisfiable classes, " + rootCnt + " of them as root.");
		
		if(unsatClasses.isEmpty()){
			unsatClasses = derivedUnsatClasses;
		}
		
		//if the ontology is not incoherent we return it here
		if(unsatClasses.isEmpty()){
			return incoherentOntology;
		}
		//compute the logical modules for each unsatisfiable class
		logger.info("Computing module for each unsatisfiable entity...");
		startTime = System.currentTimeMillis();
		entity2ModuleMap.putAll(extractModules(unsatClasses));
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
				
		//compute initial explanations for each unsatisfiable class
		logger.info("Computing initial explanations...");
		startTime = System.currentTimeMillis();
		computeExplanations(unsatClasses);
		if(computeParallel){
			computeExplanations(unsatObjectProperties);
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		if(computeParallel){
			cnt += unsatPropCnt;
		}
		
		while(cnt > 0){
			//we remove the most appropriate axiom from the ontology
			removeAppropriateAxiom();
			
			//recompute the unsatisfiable classes
			logger.info("Reclassifying...");
			startTime = System.currentTimeMillis();
			reasoner.classify();
//			hermitReasoner.classifyClasses();
			//Set<OWLClass> unsatClasses2 = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			
			logger.info("Computing root/derived unsatisfiable classes...");
			startTime = System.currentTimeMillis();
			rootFinder = new StructureBasedRootClassFinder(reasoner, this);
			unsatClasses = rootFinder.getRootUnsatisfiableClasses();
			derivedUnsatClasses = rootFinder.getDerivedUnsatisfiableClasses();
			rootCnt = unsatClasses.size();
			derivedCnt = derivedUnsatClasses.size();
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			
			//if no roots are found we use all unsat classes
			if(rootCnt == 0){
				unsatClasses = derivedUnsatClasses;
			}
			
			logger.info("Remaining unsatisfiable classes: " + (rootCnt + derivedCnt) + "(" + rootCnt + " roots).");

			if(unsatClasses.isEmpty()){
				unsatClasses = derivedUnsatClasses;
			}
			
			//recompute unsatisfiable object properties
		//	if(computeParallel){
				unsatObjectProperties = getUnsatisfiableObjectProperties(reasoner);
				logger.info("Remaining unsatisfiable object properties: " + unsatObjectProperties.size());
		//	}
			
			//save
			if(cnt - (rootCnt+derivedCnt) >= 5 || (unsatPropCnt - unsatObjectProperties.size()) >= 5){
				cnt = rootCnt + derivedCnt;
				save("log/" + fileName + "_" + cnt + "cls" + unsatPropCnt + "prop.owl");
				cnt = rootCnt + derivedCnt;
				unsatPropCnt = unsatObjectProperties.size();
				if(computeParallel){
					cnt += unsatPropCnt;
				}
			}
			
			//recompute explanations if necessary
			logger.info("Recomputing explanations...");
			startTime = System.currentTimeMillis();
			computeExplanations(unsatClasses);
			if(computeParallel){
				computeExplanations(unsatObjectProperties);
			}
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			
			System.gc();
		}
		entity2Explanations.clear();
		entity2ModuleMap.clear();
		
		save("log/" + fileName + "_" + cnt + "cls" + unsatPropCnt + "prop.owl");
		
		if(!computeParallel){
			unsatObjectProperties = getUnsatisfiableObjectProperties(reasoner);
			logger.info("Remaining unsatisfiable object properties: " + unsatObjectProperties.size());
			
			entity2ModuleMap.putAll(extractModules(unsatObjectProperties));
			
			logger.info("Recomputing explanations...");
			startTime = System.currentTimeMillis();
			computeExplanations(unsatObjectProperties);
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			while(!unsatObjectProperties.isEmpty()){
				//we remove the most appropriate axiom from the ontology
				removeAppropriateAxiom();
				
				//recompute the unsatisfiable classes
				logger.info("Reclassifying...");
				startTime = System.currentTimeMillis();
				reasoner.classify();
				logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
				
				//recompute unsatisfiable object properties
				unsatObjectProperties = getUnsatisfiableObjectProperties(reasoner);
				logger.info("Remaining unsatisfiable object properties: " + unsatObjectProperties.size());
				
				//save
				if((unsatPropCnt - unsatObjectProperties.size()) >= 1){
					save("log/" + fileName + "_" + cnt + "cls" + unsatPropCnt + "prop.owl");
					unsatPropCnt = unsatObjectProperties.size();
				}
				
				//recompute explanations if necessary
				logger.info("Recomputing explanations...");
				startTime = System.currentTimeMillis();
				computeExplanations(unsatObjectProperties);
				logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
				
				System.gc();
			}
		}
		
		save("log/" + fileName + "_coherent.owl");
		
		return ontology;
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
		entity2ModuleMap = extractModules(unsatClasses);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
				
		//compute initial explanations for each unsatisfiable class
		logger.info("Computing initial explanations...");
		startTime = System.currentTimeMillis();
		computeExplanations(unsatClasses);
//		entity2Explanations.putAll(getInitialExplanations(unsatObjectProperties));
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		while(!unsatClasses.isEmpty()){// && !unsatObjectProperties.isEmpty()){
			//we remove the most appropriate axiom from the ontology
			removeAppropriateAxiom();
			
			//recompute the unsatisfiable classes
			logger.info("Reclassifying...");
			startTime = System.currentTimeMillis();
			reasoner.classify();
			unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			logger.info("Remaining unsatisfiable classes: " + unsatClasses.size());
			
			//recompute unsatisfiable object properties
			unsatObjectProperties = getUnsatisfiableObjectProperties(reasoner);
			logger.info("Remaining unsatisfiable object properties: " + unsatObjectProperties.size());
			
			//save
			if(cnt - unsatClasses.size() >= 10){
				cnt = unsatClasses.size();
				save("log/" + fileName + "_" + cnt + "cls" + unsatObjectProperties.size() + "prop.owl");
			}
			
			//recompute explanations if necessary
			logger.info("Recomputing explanations...");
			startTime = System.currentTimeMillis();
			refillExplanations(unsatClasses, entity2Explanations);
			//refillExplanations(unsatObjectProperties, entity2Explanations);
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
	
	private OWLOntology computeCoherentOntology2(OWLOntology ontology) {
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
				
		//compute initial explanations for each unsatisfiable class
		logger.info("Computing initial explanations...");
		startTime = System.currentTimeMillis();
		PelletExplanation expGen = new PelletExplanation(reasoner.getReasoner());
		Set<Set<OWLAxiom>> explanations;
		for(OWLClass unsatCls : unsatClasses){
			explanations = expGen.getUnsatisfiableExplanations(unsatCls, numberOfJustifications);
			logger.info(unsatCls);
			entity2Explanations.put(unsatCls, explanations);
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		while(!unsatClasses.isEmpty() && !unsatObjectProperties.isEmpty()){
			//we remove the most appropriate axiom from the ontology
			removeAppropriateAxiom();
			
			//recompute the unsatisfiable classes
			logger.info("Reclassifying...");
			startTime = System.currentTimeMillis();
			reasoner.classify();
			unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			logger.info("Remaining unsatisfiable classes: " + unsatClasses.size());
			
			//save
			if(cnt - unsatClasses.size() >= 1){
				cnt = unsatClasses.size();
				save("log/" + fileName + "_" + cnt + "cls" + ".owl");
			}
			
			//recompute explanations if necessary
			logger.info("Recomputing explanations...");
			startTime = System.currentTimeMillis();
			for(OWLClass unsatCls : unsatClasses){
				if(entity2Explanations.get(unsatCls).size() < numberOfJustifications){
					explanations = expGen.getUnsatisfiableExplanations(unsatCls, numberOfJustifications);
					entity2Explanations.put(unsatCls, explanations);
				}
			}
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			
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
	
	private void removeAppropriateAxiom(){
		logger.info("Searching for appropriate axiom to remove...");
		//get frequency for each axiom
		Map<OWLAxiom, Integer> axiom2CountMap = getAxiomFrequency(entity2Explanations);
		//get a sorted list of entries with the highest axiom count first
		List<Entry<OWLAxiom, Integer>> sortedEntries = sort(axiom2CountMap);
		//we remove the most frequent axiom from the ontology which is not contained in the original DBpedia ontology
		for(Entry<OWLAxiom, Integer> e : sortedEntries){
			OWLAxiom axiom = e.getKey();
			if(!dbpediaOntology.containsAxiomIgnoreAnnotations(axiom)){
				logger.info("Removing axiom " + axiom + ".");
				manager.removeAxiom(incoherentOntology, axiom);
				//remove the axiom also from the loaded ontology
				OWLAxiom originalAnnotatedAxiom = ontology.getAxiomsIgnoreAnnotations(axiom).iterator().next();
				ontology.getOWLOntologyManager().removeAxiom(ontology, originalAnnotatedAxiom);
				
				manager.addAxiom(diffOntology, axiom);
				manager.applyChange(new RemoveAxiom(incoherentOntology, axiom));
				removeFromExplanations(entity2Explanations, axiom);
				removeFromModules(axiom);
				return;
			}
		}
	}
	
	private void save(String fileName){
		try {
			ontology.getOWLOntologyManager().saveOntology(ontology, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream(fileName)));
			diffOntology.getOWLOntologyManager().saveOntology(diffOntology, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream("log/" + diffFileName)));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private Set<OWLObjectProperty> getUnsatisfiableObjectProperties(IncrementalClassifier reasoner){
		logger.info("Computing unsatisfiable object properties...");
		long startTime = System.currentTimeMillis();
		SortedSet<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>();
		OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		for(OWLObjectProperty p : reasoner.getRootOntology().getObjectPropertiesInSignature()){
//			boolean satisfiable = reasoner.isSatisfiable(f.getOWLObjectExactCardinality(1, p));
			boolean satisfiable = reasoner.isSatisfiable(f.getOWLObjectSomeValuesFrom(p, factory.getOWLThing()));
			if(!satisfiable){
				properties.add(p);
			}
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return properties;
	}
	
	private void removeFromModules(OWLAxiom axiom){
		OWLOntology module;
		for(Entry<? extends OWLEntity, OWLOntology> entry : entity2ModuleMap.entrySet()){
			module = entry.getValue();
			module.getOWLOntologyManager().removeAxiom(module, axiom);
		}
	}
	
	private void removeFromExplanations(Map<OWLEntity, Set<Set<OWLAxiom>>> entity2Explanations, OWLAxiom axiom){
		for(Entry<OWLEntity, Set<Set<OWLAxiom>>> entry : entity2Explanations.entrySet()){
			for (Iterator<Set<OWLAxiom>> iterator = entry.getValue().iterator(); iterator.hasNext();) {
				Set<OWLAxiom> explanation = iterator.next();
				if(explanation.contains(axiom)){
					iterator.remove();
				}
			}
		}
	}
	
	private void refillExplanations(Set<? extends OWLEntity> unsatEntities, Map<OWLEntity, Set<Set<OWLAxiom>>> entity2Explanations){
		for(OWLEntity unsatClass : unsatEntities){
			Set<Set<OWLAxiom>> precomputedExplanations = entity2Explanations.get(unsatClass);
			if(precomputedExplanations == null || precomputedExplanations.size() < numberOfJustifications){
				Set<Set<OWLAxiom>> newExplanations = computeExplanations(unsatClass, numberOfJustifications);
				logger.info(unsatClass + ": " + newExplanations.size());
				entity2Explanations.put(unsatClass, newExplanations);
			}
		}
	}
	
	private Map<OWLAxiom, Integer> getAxiomFrequency(Map<OWLEntity, Set<Set<OWLAxiom>>> entity2Explanations){
		Map<OWLAxiom, Integer> axiom2CountMap = new HashMap<OWLAxiom, Integer>();
		
		for(Entry<OWLEntity, Set<Set<OWLAxiom>>> entry : entity2Explanations.entrySet()){
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
	
	private void computeExplanations(Set<? extends OWLEntity> unsatEntities){
		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		for(final OWLEntity unsatEntity : unsatEntities){
			Set<Set<OWLAxiom>> precomputedExplanations = entity2Explanations.get(unsatEntity);
			if(precomputedExplanations == null || (!entitiesWithLessExplanations.contains(unsatEntity) && precomputedExplanations.size() < numberOfJustifications)){
				executor.execute(new Runnable(){
	
					@Override
					public void run() {
						Set<Set<OWLAxiom>> explanations = computeExplanations(unsatEntity);
						logger.info("Computed "+ explanations.size() + " explanations for " + unsatEntity);
						entity2Explanations.put(unsatEntity, explanations);
						if(explanations.size() < numberOfJustifications){
							entitiesWithLessExplanations.add(unsatEntity);
						}
					}
					
				});
			}
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {

		}
		
	}
	
	
	private OWLOntology getOntologyWithoutAnnotations(OWLOntology ontology){
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontologyWithoutAnnotations = man.createOntology();
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
		logger.info("BEFORE: "+ ontology.getSubClassAxiomsForSubClass(factory.getOWLClass(IRI.create("http://dbpedia.org/ontology/Award"))));
		OWLOntologyManager man = ontology.getOWLOntologyManager();
		for (Iterator<OWLLogicalAxiom> iterator = ontology.getLogicalAxioms().iterator(); iterator.hasNext();) {
			OWLLogicalAxiom axiom = iterator.next();
			if(!ontologyWithOutAnnotations.containsAxiomIgnoreAnnotations(axiom)){
				man.removeAxiom(ontology, axiom);
			}
		}logger.info("AFTER: "+ ontology.getSubClassAxiomsForSubClass(factory.getOWLClass(IRI.create("http://dbpedia.org/ontology/Award"))));
		return ontology;
	}
	
	private Set<Set<OWLAxiom>> computeExplanations(OWLEntity unsatEntity){
		return computeExplanations(unsatEntity, numberOfJustifications);
	}
	
	private Set<Set<OWLAxiom>> computeExplanations(OWLEntity unsatEntity, int limit){
		PelletExplanation expGen = getExplanationGenerator(unsatEntity);
		if(unsatEntity instanceof OWLClass){
			return expGen.getUnsatisfiableExplanations((OWLClass) unsatEntity, limit);
		} else if(unsatEntity instanceof OWLObjectProperty){
			return expGen.getUnsatisfiableExplanations(factory.getOWLObjectExactCardinality(1, (OWLObjectProperty)unsatEntity), limit);
		}
		return null;
	}
	
	private PelletExplanation getExplanationGenerator(OWLEntity entity){
		PelletExplanation expGen = entity2ExpGen.get(entity);
//		if(expGen == null){
			expGen = new PelletExplanation(PelletReasonerFactory.getInstance().createNonBufferingReasoner(getModule(entity)));
//			entity2ExpGen.put(entity, expGen);
//		}
		return expGen;
	}
	
	private Set<Set<OWLAxiom>> computeExplanationsBlackBox(OWLClass unsatClass, int limit){
		BlackBoxExplanation singleExpGen = new BlackBoxExplanation(incoherentOntology, new HermiTReasonerFactory(), hermitReasoner);
		HSTExplanationGenerator expGen = new HSTExplanationGenerator(singleExpGen);
		return expGen.getExplanations(unsatClass, limit);
	}

	private double getConfidence(OWLAxiom axiom){
		Set<OWLAxiom> axiomsWithAnnotations = ontology.getAxiomsIgnoreAnnotations(axiom);
		if(axiomsWithAnnotations.isEmpty()){
			logger.info("Axiom with annotations not found: " + axiom);
			return 2;
		}
		OWLAxiom axiomWithAnnotations = axiomsWithAnnotations.iterator().next();
		Set<OWLAnnotation> annotations = axiomWithAnnotations.getAnnotations(confidenceProperty);
		if(!annotations.isEmpty()){
			OWLAnnotation anno = annotations.iterator().next();
			OWLLiteral literal = (OWLLiteral) anno.getValue();
			return literal.parseDouble();
		}
		return 2;
		
	}
	
	
	public OWLOntology getModule(OWLEntity entity){
		OWLOntology module = null;
		try {
			module = OWLManager.createOWLOntologyManager().createOntology(ModularityUtils.extractModule(incoherentOntology, Collections.singleton(entity), ModuleType.TOP_OF_BOT));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		OWLOntology module = entity2ModuleMap.get(entity);
//		new File("log").mkdir();
//		if(module == null){
//			md5.reset();
//			md5.update((ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology).toString() + entity.toStringID()).getBytes());
//			String hash = MD5.asHex(md5.digest());
//			String filename = "log/" + hash + ".owl";
//			File file = new File(filename);
//			boolean load = false;
//			if(load){//file.exists()){
//				module = loadModule(file);
//			} else {
//				try {
//					module = OWLManager.createOWLOntologyManager().createOntology(ModularityUtils.extractModule(incoherentOntology, Collections.singleton(entity), ModuleType.TOP_OF_BOT));
//				} catch (OWLOntologyCreationException e) {
//					e.printStackTrace();
//				}
//				/*
//				module = OntologyUtils.getOntologyFromAxioms(
//						ModularityUtils.extractModule(incoherentOntology, Collections.singleton(entity), ModuleType.TOP_OF_BOT));
//				
//				try {
//					manager.saveOntology(module, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream(filename)));
//				} catch (OWLOntologyStorageException e) {
//					e.printStackTrace();
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				}*/
//			}
//			
//			//entity2ModuleMap.put(entity, module);
//		}
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
	
	private OWLOntology loadDBpediaOntology() {
		long startTime = System.currentTimeMillis();
		logger.info("Loading DBpedia reference ontology...");
		OWLOntology ontology = null;
		try {
			URL dbpediaURL = new URL("http://downloads.dbpedia.org/3.7/dbpedia_3.7.owl.bz2");
			InputStream is = dbpediaURL.openStream();
			is = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(is);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompressorException e) {
			e.printStackTrace();
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return ontology;
	}
	
	private Map<OWLEntity, OWLOntology> extractModules(Set<? extends OWLEntity> entities){
		logger.info("Computing modules...");
		long startTime = System.currentTimeMillis();
		Map<OWLEntity, OWLOntology> entity2ModuleMap = new HashMap<OWLEntity, OWLOntology>();
		for(OWLEntity entity : entities){
			logger.info(" for " + entity.toStringID());
			OWLOntology module = getModule(entity);
			entity2ModuleMap.put(entity, module);
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return entity2ModuleMap;
	}
	
	public void setNumberOfJustifications(int numberOfJustifications) {
		this.numberOfJustifications = numberOfJustifications;
	}
	
	public void setComputeParallel(boolean computeParallel) {
		this.computeParallel = computeParallel;
	}

	public void setConfidencePropertyIRI(String iri){
		this.confidenceProperty = factory.getOWLAnnotationProperty(IRI.create(iri));
	}
	
	private List<Entry<OWLAxiom, Integer>> sort(Map<OWLAxiom, Integer> map){
		List<Entry<OWLAxiom, Integer>> entries = new ArrayList<Entry<OWLAxiom, Integer>>(map.entrySet());
        Collections.sort(entries, new Comparator<Entry<OWLAxiom, Integer>>() {

			@Override
			public int compare(Entry<OWLAxiom, Integer> o1, Entry<OWLAxiom, Integer> o2) {
				int cmp = o2.getValue().compareTo(o1.getValue());
				//use as tie breaker the confidence value
				if(cmp == 0){
					double conf1 = getConfidence(o1.getKey());
					double conf2 = getConfidence(o2.getKey());
					double diff = conf1-conf2;
					if(diff > 0){
						return 1;
					} else if(diff < 0){
						return -1;
					} else {
						return 0;
					}
//					return Double.compare(conf2, conf1);
				}
				return cmp;
			}
		});
        return entries;
	}
	
	public static void main(String[] args) throws Exception{
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		Logger.getRootLogger().addAppender(new FileAppender(new SimpleLayout(), "log/out.log"));
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		
		if(args.length != 5){
			System.out.println("USAGE: JustificationBasedCoherentOntologyExtractor <incoherent.owl> <confidencePropertyIRI> <numberOfJustifcations> <preferRootClasses(true|false)> <computeParallel(true|false)>");
			System.exit(0);
		}
		String filename = args[0];
		String confidenceIRI = args[1];
		int numberOfJustifications = Integer.parseInt(args[2]);
		boolean preferRoots = Boolean.valueOf(args[3]);
		boolean computeParallel = Boolean.valueOf(args[4]);
		
		System.out.println("Loading ontology...");
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		if(args[0].endsWith("bz2")){
			is = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
		}
		OWLOntology schema = man.loadOntologyFromOntologyDocument(is);
		Set<OWLTransitiveObjectPropertyAxiom> removedAxioms = schema.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY);
		man.removeAxioms(schema, removedAxioms);
		System.out.println("...done.");
		
		JustificationBasedCoherentOntologyExtractor extractor = new JustificationBasedCoherentOntologyExtractor();
		extractor.setNumberOfJustifications(numberOfJustifications);
		extractor.setComputeParallel(computeParallel);
		extractor.setConfidencePropertyIRI(confidenceIRI);
		if(filename.indexOf('/') >= 0){
			filename = filename.substring( filename.lastIndexOf('/')+1, filename.length() );
		}
		
		extractor.setFileName(filename);
		OWLOntology coherentOntology = extractor.getCoherentOntology(schema, preferRoots);
		man.addAxioms(coherentOntology, removedAxioms);
		
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
