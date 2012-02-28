package org.dllearner.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.modularity.IncrementalClassifier;

public class GreedyCohaerencyExtractor {
	
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JustificationBasedCoherentOntologyExtractor.class);
	
	private double stepSize = 0.001;
	private int allowedUnsatClasses = 5;
	private int allowedUnsatProperties = 5;
	
	private OWLOntologyManager manager;
	private OWLOntology coherentOntology;
	private OWLDataFactory factory;
	private IncrementalClassifier reasoner;
	
	public GreedyCohaerencyExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	public OWLOntology getCoherentOntology(OWLOntology ontology, String target, double stepSize, int allowedUnsatClasses, int allowedUnsatProperties) throws OWLOntologyCreationException{
		stepSize = stepSize/100;
		this.allowedUnsatClasses = allowedUnsatClasses;
		this.allowedUnsatProperties = allowedUnsatProperties;
		BidiMap<AxiomType<? extends OWLAxiom>, Integer> axiomType2CountMap = getAxiomTypeCount(ontology);
		
		Map<AxiomType<? extends OWLAxiom>, List<OWLAxiom>> axiomType2AxiomsMap = new HashMap<AxiomType<? extends OWLAxiom>, List<OWLAxiom>>();
		for(AxiomType<? extends OWLAxiom> type : AxiomType.AXIOM_TYPES){
			axiomType2AxiomsMap.put(type, new ArrayList<OWLAxiom>(ontology.getAxioms(type)));
		}
		
		logger.info("Source ontology contains " + ontology.getLogicalAxiomCount() + " logical axioms.");
		double[] stepSizeArray = new double[axiomType2CountMap.entrySet().size()];
		double[] cnt = new double[axiomType2CountMap.entrySet().size()];
		AxiomType[] type = new AxiomType[axiomType2CountMap.entrySet().size()];
		int i=0;
		for(Entry<AxiomType<? extends OWLAxiom>, Integer> entry : axiomType2CountMap.entrySet()){
			stepSizeArray[i] = stepSize * entry.getValue();
			type[i] = entry.getKey();
			cnt[i] = 0;
			i++;
		}
		
		
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		coherentOntology = manager.createOntology();
		
		reasoner = new IncrementalClassifier(coherentOntology);
		reasoner.setMultiThreaded(false);
//		manager.addOntologyChangeListener(reasoner);
		reasoner.classify();
		
		
		boolean isCoherent = true;
		for(double j = 0; j < 1; j += stepSize){//increase by stepsize p until 100% 
			if(isCoherent){
				for(i = 0; i < stepSizeArray.length; i++){//for each axiomtype
					cnt[i] = cnt[i] + stepSizeArray[i];//sum up value which was computed by p * #axioms
					int x = (int)cnt[i];
					if(x > 0){
						logger.info("Adding " + x + " " + type[i] + " axioms from " + axiomType2CountMap.get(type[i]));
						Set<OWLAxiom> toAdd = new HashSet<OWLAxiom>(axiomType2AxiomsMap.get(type[i]).subList(0, x));
						manager.addAxioms(coherentOntology, toAdd);
						axiomType2AxiomsMap.get(type[i]).removeAll(toAdd);
						isCoherent = isCoherent();
						if(!isCoherent){
							manager.removeAxioms(coherentOntology, toAdd);
							logger.info("Incoherency detected. Undoing changes.");
							isCoherent = true;
//							break;
						}
					}
					
					//same procedure with divide and conquer optimization
					/*List<OWLAxiom> toAdd = axiomType2AxiomsMap.get(type[i]).subList(0, x);
					addAxioms(toAdd);
					axiomType2AxiomsMap.get(type[i]).removeAll(toAdd);
					*/
					
					cnt[i] = cnt[i] - x;
				}
			}
			logger.info("Coherent ontology contains " + coherentOntology.getLogicalAxiomCount() + " logical axioms.");
			
		}
		try {
			manager.saveOntology(coherentOntology, new RDFXMLOntologyFormat(), new BufferedOutputStream(new FileOutputStream(new File(target))));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return coherentOntology;
	}
	
	private boolean isCoherent(){
		return (reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size() <= allowedUnsatClasses)
				&& (getUnsatisfiableObjectProperties(reasoner).size() <= allowedUnsatProperties);
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
	
	private Set<OWLAxiom> addAxioms(List<OWLAxiom> axioms){
		Set<OWLAxiom> addedAxioms = new HashSet<OWLAxiom>();
		
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>(axioms);
		manager.addAxioms(coherentOntology, axiomSet);
		reasoner.classify();
		boolean isCoherent = isCoherent();
		if(!isCoherent){
			System.out.println("Incohaerency detected. Splitting...");
			manager.removeAxioms(coherentOntology, axiomSet);
			if(axioms.size() == 1){
				return addedAxioms;
			}
			
			int size = axioms.size();
			int pivot = size/2;
			
			List<OWLAxiom> left = axioms.subList(0, pivot);
			List<OWLAxiom> right = axioms.subList(pivot, size-1);
			
			addedAxioms.addAll(addAxioms(left));
			addedAxioms.addAll(addAxioms(right));
			
		} else {
			addedAxioms.addAll(axioms);
		}
		
		return addedAxioms;
	}
	
	public OWLOntology getCoherentOntology(OWLReasoner reasoner, String target, double stepSize, int allowedUnsatClasses, int allowedUnsatProperties) throws OWLOntologyCreationException{
		return getCoherentOntology(reasoner.getRootOntology(), target, stepSize, allowedUnsatClasses, allowedUnsatProperties);
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
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		Logger.getRootLogger().addAppender(new FileAppender(new SimpleLayout(), "log/greedy_out.log"));
		
		if(args.length != 5){
			System.out.println("USAGE: GreedyCoherencyExtractor <incoherent.owl> <target.owl> <stepsizeInPercent> <nrOfallowedUnsatClasses> <nrOfallowedUnsatProperties>");
			System.exit(0);
		}
		String filename = args[0];
		String target = args[1];
		double stepSize = Double.parseDouble(args[2]);
		int nrOfallowedUnsatClasses = Integer.parseInt(args[3]);
		int nrOfallowedUnsatProperties = Integer.parseInt(args[4]);
		
		System.out.println("Loading ontology...");
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		if(args[0].endsWith("bz2")){
			is = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
		}
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology schema = man.loadOntologyFromOntologyDocument(is);
		man.removeAxioms(schema, schema.getAxioms(AxiomType.ANNOTATION_ASSERTION));
		System.out.println("...done.");
		
		GreedyCohaerencyExtractor ge = new GreedyCohaerencyExtractor();
		ge.getCoherentOntology(schema, target, stepSize, nrOfallowedUnsatClasses, nrOfallowedUnsatProperties);
	}

}
