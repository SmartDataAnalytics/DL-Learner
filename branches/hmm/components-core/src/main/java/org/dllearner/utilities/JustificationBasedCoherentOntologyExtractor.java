package org.dllearner.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import com.clarkparsia.modularity.ModularityUtils;
import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class JustificationBasedCoherentOntologyExtractor implements CoherentOntologyExtractor{
	
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JustificationBasedCoherentOntologyExtractor.class);
	private static final String DIFF_ONTOLOGY_NAME = "diff.owl";
	
	private int numberOfJustifications = 10;
	private PelletReasoner baseReasoner;
	private PelletReasoner reasoner;//IncrementalClassifier reasoner;
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
	private Set<OWLClass> unsatClasses;
	
	//whether to debug classes and properties in parallel
	private boolean computeParallel = false;
	
	private OWLAnnotationProperty confidenceProperty;

	private OWLOntology dbpediaOntology;
	
	private String fileName;
	private String diffFileName = "diff.owl";
	
	public JustificationBasedCoherentOntologyExtractor() {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		dbpediaOntology = loadDBpediaOntologyOWLDL();
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
		
		/*only to avoid Pellet warnings during the process and this axioms are only removed from the ontology, 
		 which is used during the debugging and not from the ontology which is always saved and returned finally*/
		removedTransitiveAxioms = incoherentOntology.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY);
		incoherentOntology.getOWLOntologyManager().removeAxioms(incoherentOntology, removedTransitiveAxioms);
		
		manager = incoherentOntology.getOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		
		long startTime = System.currentTimeMillis();
		baseReasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(incoherentOntology);
		reasoner = baseReasoner;//new IncrementalClassifier(baseReasoner);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		//compute the unsatisfiable object properties and their corresponding modules
		unsatObjectProperties = getUnsatisfiableObjectProperties();
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
		OWLOntologyManager man = incoherentOntology.getOWLOntologyManager();
		factory = man.getOWLDataFactory();
//		man.addOntologyChangeListener(reasoner);
		
		//compute the unsatisfiable classes
		logger.info("Computing root/derived unsatisfiable classes...");
		long startTime = System.currentTimeMillis();
		StructureBasedRootClassFinder rootFinder = new StructureBasedRootClassFinder(reasoner, this);
		unsatClasses = rootFinder.getRootUnsatisfiableClasses();
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
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
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
				unsatObjectProperties = getUnsatisfiableObjectProperties();
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
			if(unsatClasses.isEmpty() && (!computeParallel || (computeParallel && unsatObjectProperties.isEmpty()))){
				cnt = 0;
				unsatPropCnt = unsatObjectProperties.size();
				break;
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
			unsatObjectProperties = getUnsatisfiableObjectProperties();
			logger.info("Remaining unsatisfiable object properties: " + unsatObjectProperties.size());
			
//			entity2ModuleMap.putAll(extractModules(unsatObjectProperties));
			
			logger.info("Recomputing explanations...");
			startTime = System.currentTimeMillis();
			computeExplanations(unsatObjectProperties);
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			while(!unsatObjectProperties.isEmpty()){
				//we remove the most appropriate axiom from the ontology
				removeAppropriateAxiom();
				
				//recompute unsatisfiable object properties
				unsatObjectProperties = getUnsatisfiableObjectProperties();
				logger.info("Remaining unsatisfiable object properties: " + unsatObjectProperties.size());
				
				//save
				if((unsatPropCnt - unsatObjectProperties.size()) >= 5){
					save("log/" + fileName + "_" + cnt + "cls" + unsatPropCnt + "prop.owl");
					unsatPropCnt = unsatObjectProperties.size();
				}
				
				if(unsatObjectProperties.isEmpty()){
					break;
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
		logger.info("Finished. \n Coherent ontology contains " + ontology.getLogicalAxiomCount() + " logical axioms." +
				"Removed axioms: " + diffOntology.getLogicalAxiomCount());
		
		return ontology;
	}
	
	private OWLOntology computeCoherentOntology(OWLOntology ontology) {
		OWLOntologyManager man = incoherentOntology.getOWLOntologyManager();
		factory = man.getOWLDataFactory();
//		man.addOntologyChangeListener(reasoner);
		
		//compute the unsatisfiable classes
		logger.info("Computing unsatisfiable classes...");
		long startTime = System.currentTimeMillis();
		unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		logger.info("Detected " + unsatClasses.size() + " unsatisfiable classes.");
		
		//if the ontology is not incoherent we return it here
		if(unsatClasses.isEmpty()){
			return incoherentOntology;
		}
				
		//compute initial explanations for each unsatisfiable class
		logger.info("Computing initial explanations...");
		startTime = System.currentTimeMillis();
		computeExplanations(unsatClasses);
		if(computeParallel){
			computeExplanations(unsatObjectProperties);
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		
		int cnt = unsatClasses.size();
		if(computeParallel){
			cnt += unsatObjectProperties.size();
		}
		
		while(!runLocalDebugging() && cnt > 0){
			//we remove the most appropriate axiom from the ontology
			removeAppropriateAxiom();
			
			logger.info("Computing unsatisfiable classes...");
			startTime = System.currentTimeMillis();
			unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			logger.info("Remaining unsatisfiable classes: " + unsatClasses.size());
			
			//recompute unsatisfiable object properties
			unsatObjectProperties = getUnsatisfiableObjectProperties();
			logger.info("Remaining unsatisfiable object properties: " + unsatObjectProperties.size());
			
			//save
			if((!computeParallel && (cnt-unsatClasses.size()>= 10))
					|| (computeParallel && (cnt-unsatClasses.size()+unsatObjectProperties.size())>=10)){
				cnt = unsatClasses.size();
				save("log/" + fileName + "_" + unsatClasses.size() + "cls" + unsatObjectProperties.size() + "prop.owl");
				if(computeParallel){
					cnt += unsatObjectProperties.size();
				}
			}
			if(unsatClasses.isEmpty() && (!computeParallel || (computeParallel && unsatObjectProperties.isEmpty()))){
				cnt = 0;
				break;
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
		entitiesWithLessExplanations.clear();
		entity2ExpGen.clear();
		
		save("log/" + fileName + "_" + unsatClasses.size() + "cls" + unsatObjectProperties.size() + "prop.owl");
		
		
		if(!computeParallel){
			unsatObjectProperties = getUnsatisfiableObjectProperties();
			int unsatPropCnt = unsatObjectProperties.size();
			logger.info("Remaining unsatisfiable object properties: " + unsatObjectProperties.size());
			
			logger.info("Computing explanations...");
			startTime = System.currentTimeMillis();
			computeExplanations(unsatObjectProperties);
			logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
			while(!runLocalDebugging() && unsatPropCnt > 0){
				//we remove the most appropriate axiom from the ontology
				removeAppropriateAxiom();
				
				//recompute unsatisfiable object properties
				unsatObjectProperties = getUnsatisfiableObjectProperties();
				logger.info("Remaining unsatisfiable object properties: " + unsatObjectProperties.size());
				
				//save
				if((unsatPropCnt - unsatObjectProperties.size()) >= 10){
					save("log/" + fileName + "_" + unsatClasses.size() + "cls" + unsatPropCnt + "prop.owl");
					unsatPropCnt = unsatObjectProperties.size();
				}
				if(unsatObjectProperties.isEmpty()){
					break;
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
		logger.info("Finished. \n Coherent ontology contains " + ontology.getLogicalAxiomCount() + " logical axioms." +
				"Removed axioms: " + diffOntology.getLogicalAxiomCount());
		
		return ontology;
	}
	
	/*
	 * check here if all explanations are found, i.e. for each entity the number of justifications is lower than the limit which was set
	 */
	private boolean allExplanationsFound(){
		boolean allExplanationsFound = false;
		if(computeParallel){
			allExplanationsFound = entitiesWithLessExplanations.size() == (unsatClasses.size() + unsatObjectProperties.size()); 
		} else {
			allExplanationsFound = entitiesWithLessExplanations.size() == unsatClasses.size();
		}
		return allExplanationsFound;
	}
	
	private boolean runLocalDebugging(){
		if(allExplanationsFound()){
			//add all explanations into one set
			Set<Set<OWLAxiom>> explanations = new HashSet<Set<OWLAxiom>>();
			for(Entry<OWLEntity, Set<Set<OWLAxiom>>> e: entity2Explanations.entrySet()){
				explanations.addAll(e.getValue());
			}
			//get the frequency for each axiom
			Map<OWLAxiom, Integer> axiom2CountMap = getAxiomFrequency(entity2Explanations);
			//get a sorted list of entries with the highest axiom count first
			List<Entry<OWLAxiom, Integer>> candidates = sort(axiom2CountMap);
			//remove axioms until no further explanation exists
			while(!explanations.isEmpty()){
				removeAppropriateAxiomLocal(explanations, candidates);
			}
			if(computeParallel){
				unsatClasses.clear();
				unsatObjectProperties.clear();
			} else {
				unsatClasses.clear();
			}
			return true;
		}
		return false;
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
		PelletExplanation expGen = new PelletExplanation(baseReasoner);
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
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
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
		save("log/" + fileName + "_coherent.owl");
		
		return ontology;
	}
	
	private void removeAppropriateAxiom(){
		logger.info("Searching for appropriate axiom to remove...");
		//get frequency for each axiom
		Map<OWLAxiom, Integer> axiom2CountMap = getAxiomFrequency(entity2Explanations);
		//get a sorted list of entries with the highest axiom count first
		List<Entry<OWLAxiom, Integer>> sortedEntries = sort(axiom2CountMap);
		logger.info("Candidates: " + sortedEntries.size());
		if(sortedEntries.size() >= 2){
			logger.info("First: " + sortedEntries.get(0) + "(" + getConfidence(sortedEntries.get(0).getKey()) + ")");
			logger.info("Second: " + sortedEntries.get(1) + "(" + getConfidence(sortedEntries.get(1).getKey()) + ")");
		}
		
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
	
	private void removeAppropriateAxiomLocal(Set<Set<OWLAxiom>> explanations, List<Entry<OWLAxiom, Integer>> candidates){
		logger.info("Searching for appropriate axiom to remove...");
		logger.info("Candidates: " + candidates.size());
		if(candidates.size() >= 2){
			logger.info("First: " + candidates.get(0) + "(" + getConfidence(candidates.get(0).getKey()) + ")");
			logger.info("Second: " + candidates.get(1) + "(" + getConfidence(candidates.get(1).getKey()) + ")");
		}
		
		//we remove the most frequent axiom from the ontology which is not contained in the original DBpedia ontology
		for(Iterator<Entry<OWLAxiom, Integer>> iter = candidates.iterator(); iter.hasNext();){
			OWLAxiom axiom = iter.next().getKey();
			if(!dbpediaOntology.containsAxiomIgnoreAnnotations(axiom)){
				iter.remove();
				logger.info("Removing axiom " + axiom + ".");
				manager.removeAxiom(incoherentOntology, axiom);
				//remove the axiom also from the loaded ontology
				OWLAxiom originalAnnotatedAxiom = ontology.getAxiomsIgnoreAnnotations(axiom).iterator().next();
				ontology.getOWLOntologyManager().removeAxiom(ontology, originalAnnotatedAxiom);
				//add the removed annotated axiom to the diff ontology
				manager.addAxiom(diffOntology, originalAnnotatedAxiom);
				//remove each explanation which contains the axiom
				for (Iterator<Set<OWLAxiom>> iterator = explanations.iterator(); iterator.hasNext();) {
					Set<OWLAxiom> explanation = iterator.next();
					if(explanation.contains(axiom)){
						iterator.remove();
					}
				}
				return;
			} else {
				iter.remove();
			}
		}
	}
	
	private void save(String fileName){
		logger.info("Writing to disk...");
		long startTime = System.currentTimeMillis();
		try {
			ontology.getOWLOntologyManager().saveOntology(ontology, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream(fileName)));
			diffOntology.getOWLOntologyManager().saveOntology(diffOntology, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream("log/" + diffFileName)));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
	}
	
	private Set<OWLObjectProperty> getUnsatisfiableObjectProperties(){
		logger.info("Computing unsatisfiable object properties...");
		long startTime = System.currentTimeMillis();
		SortedSet<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>();
		OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		for(OWLObjectProperty p : incoherentOntology.getObjectPropertiesInSignature()){
//			boolean satisfiable = reasoner.isSatisfiable(f.getOWLObjectExactCardinality(1, p));
			boolean satisfiable = reasoner.isSatisfiable(f.getOWLObjectSomeValuesFrom(p, factory.getOWLThing()));
			if(!satisfiable){
				properties.add(p);
			}
		}
		/*
		 * this method down't seem to work TODO ask Pellet developers why
		for(OWLObjectPropertyExpression p : reasoner.getEquivalentObjectProperties(factory.getOWLBottomObjectProperty()).getEntitiesMinusBottom()){
			if(!p.isAnonymous()){
				properties.add(p.asOWLObjectProperty());
			}
		}*/
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
		if(axiomsWithAnnotations.isEmpty()){//this should never happen
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
	
	/**
	 * First try to clean up ontology with JENA as original ontology is in OWL Full because of some user-defined datatypes.
	 * We could either (1) return the rdfs:range triples of the properties with user-defined datatypes or (2) remove all triples about the property.
	 * @return
	 */
	private OWLOntology loadDBpediaOntologyOWLDL() {
		long startTime = System.currentTimeMillis();
		logger.info("Loading DBpedia reference ontology...");
		OWLOntology ontology = null;
		try {
			URL dbpediaURL = new URL("http://downloads.dbpedia.org/3.7/dbpedia_3.7.owl.bz2");
			InputStream is = dbpediaURL.openStream();
			is = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			Model model = ModelFactory.createDefaultModel();
			model.read(is, null);
			//get all subjects where URI of RDFS:range starts with http://dbpedia.org/datatype/ 
			for(StmtIterator iter = model.listStatements(null, RDFS.range, (RDFNode)null); iter.hasNext();){
				Statement st = iter.next();
				if(st.getObject().asResource().getURI().startsWith("http://dbpedia.org/datatype/")){
					iter.remove();
				}
				//solution 2
//				for(StmtIterator iter2 = model.listStatements(iter.next().getSubject(), null, (RDFNode)null); iter2.hasNext();){
//				iter2.remove();
//			}
			}
			

			
			return convert(model);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompressorException e) {
			e.printStackTrace();
		}
		logger.info("...done in " + (System.currentTimeMillis()-startTime) + "ms.");
		return ontology;
	}
	
	private OWLOntology convert(Model model) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos, "N-TRIPLE");
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology retOnt = null;
		try {
			retOnt = manager.loadOntologyFromOntologyDocument(bais);
		} catch (OWLOntologyCreationException e) {

		}
		return retOnt;
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
