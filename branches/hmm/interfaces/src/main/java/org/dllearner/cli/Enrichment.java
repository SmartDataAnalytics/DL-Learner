/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.cli;

import static java.util.Arrays.asList;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.aksw.commons.jena_owlapi.Conversion;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.SimpleSubclassLearner;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.properties.AsymmetricObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.DataPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.DataPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.DisjointDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.DisjointObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.InverseFunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.InverseObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.IrreflexiveObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.ReflexiveObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.SubDataPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.TransitiveObjectPropertyAxiomLearner;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.EnrichmentVocabulary;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.PrefixCCMap;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.dllearner.utilities.owl.DLLearnerAxiomConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

/**
 * Command Line Interface for Enrichment.
 * 
 * @author Jens Lehmann
 * 
 */
public class Enrichment {

	// data structure for holding the result of an algorithm run
	protected class AlgorithmRun {
		
		// we only store the algorithm class and not the learning algorithm object,
		// since otherwise we run into memory problems for full enrichment
		private Class<? extends LearningAlgorithm> algorithm;
		private List<EvaluatedAxiom> axioms;
		private Map<ConfigOption,Object> parameters;

		public AlgorithmRun(Class<? extends LearningAlgorithm> algorithm, List<EvaluatedAxiom> axioms, Map<ConfigOption,Object> parameters) {
			this.algorithm = algorithm;
			this.axioms = axioms;
			this.parameters = parameters;
		}
		
		public Class<? extends LearningAlgorithm> getAlgorithm() {
			return algorithm;
		}

		public List<EvaluatedAxiom> getAxioms() {
			return axioms;
		}		
		
		public Map<ConfigOption, Object> getParameters() {
			return parameters;
		}		
	}
	
	private static Logger logger = Logger.getLogger(Enrichment.class);
	private DecimalFormat df = new DecimalFormat("##0.0");
	private static final String DEFAULT_NS = "http://localhost:8080/";
	
	//used to generate unique random identifiers
	private SecureRandom random = new SecureRandom();

	// enrichment parameters
	private SparqlEndpoint se;
	private Entity resource;
	private boolean verbose;

	// max. execution time for each learner for each entity
	private int maxExecutionTimeInSeconds = 10;

	// restrict tested number of entities per type (only for testing purposes);
	// should be set to -1 in production mode
	int maxEntitiesPerType = -1;
	
	// number of axioms which will be learned/considered (only applies to
	// some learners)
	private int nrOfAxiomsToLearn = 10;	
	private double threshold = 0.7;
	private int chunksize = 1000;
	private boolean omitExistingAxioms;
	
	private boolean useInference;
	private SPARQLReasoner reasoner;
	private ExtractionDBCache cache;
	private String cacheDir = "cache";
	
	// lists of algorithms to apply
	private List<Class<? extends AxiomLearningAlgorithm>> objectPropertyAlgorithms;
	private List<Class<? extends AxiomLearningAlgorithm>> dataPropertyAlgorithms;
	private List<Class<? extends LearningAlgorithm>> classAlgorithms;
	
	// list of generated axioms while script is running
	private List<AlgorithmRun> algorithmRuns;
	
//	private CommonPrefixMap prefixes = new CommonPrefixMap();
	
	// cache for SparqKnowledgeSource
	SparqlKnowledgeSource ksCached;
	AbstractReasonerComponent rcCached;
	
	private Set<OWLAxiom> learnedOWLAxioms;
	private Set<EvaluatedAxiom> learnedEvaluatedAxioms;
	
	public Enrichment(SparqlEndpoint se, Entity resource, double threshold, int nrOfAxiomsToLearn, boolean useInference, boolean verbose, int chunksize, int maxExecutionTimeInSeconds, boolean omitExistingAxioms) {
		this.se = se;
		this.resource = resource;
		this.verbose = verbose;
		this.threshold = threshold;
		this.nrOfAxiomsToLearn = nrOfAxiomsToLearn;
		this.useInference = useInference;
		this.chunksize = chunksize;
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
		this.omitExistingAxioms = omitExistingAxioms;
		
		try {
			cacheDir = "cache" + File.separator + URLEncoder.encode(se.getURL().toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cache = new ExtractionDBCache(cacheDir);
		
		objectPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
		objectPropertyAlgorithms.add(DisjointObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(EquivalentObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(FunctionalObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(InverseFunctionalObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(ObjectPropertyDomainAxiomLearner.class);
		objectPropertyAlgorithms.add(ObjectPropertyRangeAxiomLearner.class);
		objectPropertyAlgorithms.add(SubObjectPropertyOfAxiomLearner.class);
		objectPropertyAlgorithms.add(SymmetricObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(AsymmetricObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(TransitiveObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(InverseObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(ReflexiveObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(IrreflexiveObjectPropertyAxiomLearner.class);

		dataPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
		dataPropertyAlgorithms.add(DisjointDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(EquivalentDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(FunctionalDataPropertyAxiomLearner.class);
		dataPropertyAlgorithms.add(DataPropertyDomainAxiomLearner.class);
		dataPropertyAlgorithms.add(DataPropertyRangeAxiomLearner.class); 
		dataPropertyAlgorithms.add(SubDataPropertyOfAxiomLearner.class);
		
		classAlgorithms = new LinkedList<Class<? extends LearningAlgorithm>>();
		classAlgorithms.add(DisjointClassesLearner.class);
		classAlgorithms.add(SimpleSubclassLearner.class);
		classAlgorithms.add(CELOE.class);		
		
		algorithmRuns = new LinkedList<AlgorithmRun>();
		
		learnedOWLAxioms = new HashSet<OWLAxiom>();
		learnedEvaluatedAxioms = new HashSet<EvaluatedAxiom>();
	}
	
	public void start() throws ComponentInitException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, LearningProblemUnsupportedException, MalformedURLException {
						
		// instantiate SPARQL endpoint wrapper component
		SparqlEndpointKS ks = new SparqlEndpointKS(se);
		ks.init();
		
		// common helper objects
		SPARQLTasks st = new SPARQLTasks(se);
		
		//check if endpoint supports SPARQL 1.1
//		boolean supportsSPARQL_1_1 = st.supportsSPARQL_1_1();
//		ks.setSupportsSPARQL_1_1(supportsSPARQL_1_1);
		
		if(useInference){
			reasoner = new SPARQLReasoner(ks, cache);
			System.out.print("Precomputing subsumption hierarchy ... ");
			long startTime = System.currentTimeMillis();
			reasoner.prepareSubsumptionHierarchy();
			System.out.println("done in " + (System.currentTimeMillis() - startTime) + " ms");
		}
		
		if(resource == null) {

			// loop over all entities and call appropriate algorithms
			Set<NamedClass> classes = st.getAllClasses();
			int entities = 0;
			for(NamedClass nc : classes) {
				try {
					runClassLearningAlgorithms(ks, nc);
				} catch (Exception e) {
					e.printStackTrace();
				}		
				entities++;
				if(maxEntitiesPerType != -1 && entities > maxEntitiesPerType) {
					break;
				}	
			}
			entities = 0;
			Set<ObjectProperty> objectProperties = st.getAllObjectProperties();			
			for(ObjectProperty property : objectProperties) {
				runObjectPropertyAlgorithms(ks, property);	
				entities++;
				if(maxEntitiesPerType != -1 && entities > maxEntitiesPerType) {
					break;
				}				
			}
			entities = 0;
			Set<DatatypeProperty> dataProperties = st.getAllDataProperties();
			for(DatatypeProperty property : dataProperties) {
				runDataPropertyAlgorithms(ks, property);
				entities++;
				if(maxEntitiesPerType != -1 && entities > maxEntitiesPerType) {
					break;
				}					
			}
		} else {
			if(resource instanceof ObjectProperty) {
				System.out.println(resource + " appears to be an object property. Running appropriate algorithms.\n");
				runObjectPropertyAlgorithms(ks, (ObjectProperty) resource);
			} else if(resource instanceof DatatypeProperty) {
				System.out.println(resource + " appears to be a data property. Running appropriate algorithms.\n");
				runDataPropertyAlgorithms(ks, (DatatypeProperty) resource);
			} else if(resource instanceof NamedClass) {
				System.out.println(resource + " appears to be a class. Running appropriate algorithms.\n");
				try {
					runClassLearningAlgorithms(ks, (NamedClass) resource);
				} catch (Exception e) {e.printStackTrace();
					System.out.println(e.getCause());
				} catch (Error e) {
					System.out.println(e.getCause());
				}			
			} else {
				throw new Error("The type " + resource.getClass() + " of resource " + resource + " cannot be handled by this enrichment tool.");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void runClassLearningAlgorithms(SparqlEndpointKS ks, NamedClass nc) throws ComponentInitException {
//		System.out.println("Running algorithms for class " + nc);
		for (Class<? extends LearningAlgorithm> algorithmClass : classAlgorithms) {
			if(algorithmClass == CELOE.class) {
				applyCELOE(ks, nc, false, false);
				applyCELOE(ks, nc, true, true);
			} else {
				applyLearningAlgorithm((Class<AxiomLearningAlgorithm>)algorithmClass, ks, nc);
			}
		}
	}
	
	private void runObjectPropertyAlgorithms(SparqlEndpointKS ks, ObjectProperty property) throws ComponentInitException {
//		System.out.println("Running algorithms for object property " + property);
		for (Class<? extends AxiomLearningAlgorithm> algorithmClass : objectPropertyAlgorithms) {
			applyLearningAlgorithm(algorithmClass, ks, property);
		}		
	}
	
	private void runDataPropertyAlgorithms(SparqlEndpointKS ks, DatatypeProperty property) throws ComponentInitException {
//		System.out.println("Running algorithms for data property " + property);
		for (Class<? extends AxiomLearningAlgorithm> algorithmClass : dataPropertyAlgorithms) {
			applyLearningAlgorithm(algorithmClass, ks, property);
		}		
	}	
	
	private List<EvaluatedAxiom> applyCELOE(SparqlEndpointKS ks, NamedClass nc, boolean equivalence, boolean reuseKnowledgeSource) throws ComponentInitException {

		// get instances of class as positive examples
		SPARQLReasoner sr = new SPARQLReasoner(ks);
		SortedSet<Individual> posExamples = sr.getIndividuals(nc, 20);
		if(posExamples.isEmpty()){
			System.out.println("Skipping CELOE because class " + nc.toString() + " is empty.");
			return Collections.emptyList();
		}
		SortedSet<String> posExStr = Helper.getStringSet(posExamples);
		
		// use own implementation of negative example finder
		long startTime = System.currentTimeMillis();
		System.out.print("finding negatives ... ");
		AutomaticNegativeExampleFinderSPARQL2 finder = new AutomaticNegativeExampleFinderSPARQL2(ks.getEndpoint());
		SortedSet<String> negExStr = finder.getNegativeExamples(nc.getName(), posExStr);
		negExStr = SetManipulation.fuzzyShrink(negExStr, 20);
		SortedSet<Individual> negExamples = Helper.getIndividualSet(negExStr);
		SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(posExamples, negExamples);
		long runTime = System.currentTimeMillis() - startTime;
		System.out.println("done (" + negExStr.size()+ " examples fround in " + runTime + " ms)");
		
        SparqlKnowledgeSource ks2;
        AbstractReasonerComponent rc;
        if(reuseKnowledgeSource) {
        	ks2 = ksCached;
        	rc = rcCached;
        	System.out.println("re-using previously generated knowledge base fragment");
        } else {
            ks2 = new SparqlKnowledgeSource(); 
            ks2.setInstances(Datastructures.individualSetToStringSet(examples.getCompleteSet()));
            ks2.setUrl(ks.getEndpoint().getURL());
            ks2.setDefaultGraphURIs(new TreeSet<String>(ks.getEndpoint().getDefaultGraphURIs()));
            ks2.setUseLits(false);
            ks2.setUseCacheDatabase(true);
            ks2.setCacheDir(cacheDir);
            ks2.setRecursionDepth(2);
            ks2.setCloseAfterRecursion(true);
            ks2.setDissolveBlankNodes(false);
            ks2.setSaveExtractedFragment(true);
            startTime = System.currentTimeMillis();
            System.out.print("getting knowledge base fragment ... ");
            ks2.init();
            runTime = System.currentTimeMillis() - startTime;
            System.out.println("done in " + runTime + " ms");    
            rc = new FastInstanceChecker(ks2);
            rc.init();
            ksCached = ks2;
            rcCached = rc;
        }

        ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(nc);
        lp.setEquivalence(equivalence);
        lp.setHeuristic(HeuristicType.FMEASURE);
        lp.setUseApproximations(false);
        lp.setMaxExecutionTimeInSeconds(10);
        lp.init();

        CELOE la = new CELOE(lp, rc);
        la.setMaxExecutionTimeInSeconds(10);
        la.setNoisePercentage(25);
        la.init();
        startTime = System.currentTimeMillis();
        System.out.print("running CELOE (for " + (equivalence ? "equivalent classes" : "sub classes") + ") ... ");
        la.start();
        runTime = System.currentTimeMillis() - startTime;
        System.out.println("done in " + runTime + " ms");	

        // convert the result to axioms (to make it compatible with the other algorithms)
        List<? extends EvaluatedDescription> learnedDescriptions = la.getCurrentlyBestEvaluatedDescriptions(threshold);
        List<EvaluatedAxiom> learnedAxioms = new LinkedList<EvaluatedAxiom>();
        for(EvaluatedDescription learnedDescription : learnedDescriptions) {
        	Axiom axiom;
        	if(equivalence) {
        		axiom = new EquivalentClassesAxiom(nc, learnedDescription.getDescription());
        	} else {
        		axiom = new SubClassAxiom(nc, learnedDescription.getDescription());
        	}
        	Score score = lp.computeScore(learnedDescription.getDescription());
        	learnedAxioms.add(new EvaluatedAxiom(axiom, score)); 
        }
        System.out.println(prettyPrint(learnedAxioms));	
        learnedEvaluatedAxioms.addAll(learnedAxioms);
        algorithmRuns.add(new AlgorithmRun(CELOE.class, learnedAxioms, ConfigHelper.getConfigOptionValues(la)));	
		return learnedAxioms;
	}
	
	private List<EvaluatedAxiom> applyLearningAlgorithm(Class<? extends AxiomLearningAlgorithm> algorithmClass, SparqlEndpointKS ks, Entity entity) throws ComponentInitException {
		AxiomLearningAlgorithm learner = null;
		try {
			learner = algorithmClass.getConstructor(
					SparqlEndpointKS.class).newInstance(ks);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(classAlgorithms.contains(algorithmClass)) {
			ConfigHelper.configure(learner, "classToDescribe", entity);
		} else {
			ConfigHelper.configure(learner, "propertyToDescribe", entity);
		}
		ConfigHelper.configure(learner, "maxExecutionTimeInSeconds",
				maxExecutionTimeInSeconds);
		((AbstractAxiomLearningAlgorithm)learner).setLimit(chunksize);
		((AbstractAxiomLearningAlgorithm)learner).setReturnOnlyNewAxioms(omitExistingAxioms);
		learner.init();
		if(reasoner != null){
			((AbstractAxiomLearningAlgorithm)learner).setReasoner(reasoner);
		}
		String algName = AnnComponentManager.getName(learner);
		System.out.print("Applying " + algName + " on " + entity + " ... ");
		long startTime = System.currentTimeMillis();
		try {
			learner.start();
		} catch (Exception e) {
			if(e.getCause() instanceof SocketTimeoutException){
				System.out.println("Query timed out (endpoint possibly too slow).");
			} else {
				e.printStackTrace();
			}
		}
		long runtime = System.currentTimeMillis() - startTime;
		System.out.println("done in " + runtime + " ms");
		List<EvaluatedAxiom> learnedAxioms = learner
				.getCurrentlyBestEvaluatedAxioms(nrOfAxiomsToLearn, threshold);
		System.out.println(prettyPrint(learnedAxioms));
		learnedEvaluatedAxioms.addAll(learnedAxioms);
		for(EvaluatedAxiom evAx : learnedAxioms){
			learnedOWLAxioms.add(OWLAPIAxiomConvertVisitor.convertAxiom(evAx.getAxiom()));
		}
		
		algorithmRuns.add(new AlgorithmRun(learner.getClass(), learnedAxioms, ConfigHelper.getConfigOptionValues(learner)));
		return learnedAxioms;
	}
	
	private String prettyPrint(List<EvaluatedAxiom> learnedAxioms) {
		String str = "suggested axioms and their score in percent:\n";
		if(learnedAxioms.isEmpty()) {
			return "  no axiom suggested\n";
		} else {
			for (EvaluatedAxiom learnedAxiom : learnedAxioms) {
				str += " " + prettyPrint(learnedAxiom) + "\n";
			}		
		}
		return str;
	}
	
	private String prettyPrint(EvaluatedAxiom axiom) {
		double acc = axiom.getScore().getAccuracy() * 100;
		String accs = df.format(acc);
		if(accs.length()==3) { accs = "  " + accs; }
		if(accs.length()==4) { accs = " " + accs; }
		String str =  accs + "%\t" + axiom.getAxiom().toManchesterSyntaxString(null, PrefixCCMap.getInstance());
		return str;
	}
	
	/*
	 * Generates list of OWL axioms.
	 */
	List<OWLAxiom> toRDF(List<EvaluatedAxiom> evalAxioms, Class<? extends LearningAlgorithm> algorithm, Map<ConfigOption,Object> parameters, SparqlEndpointKS ks){
		return toRDF(evalAxioms, algorithm, parameters, ks, null);
	}
	
	private List<OWLAxiom> toRDF(List<EvaluatedAxiom> evalAxioms, Class<? extends LearningAlgorithm> algorithm, Map<ConfigOption,Object> parameters, SparqlEndpointKS ks, String defaultNamespace){
		if(defaultNamespace == null || defaultNamespace.isEmpty()){
			defaultNamespace = DEFAULT_NS;
		}
		List<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
		
		OWLDataFactory f = new OWLDataFactoryImpl();
		
		//create instance for suggestion set
		String suggestionSetID = defaultNamespace + generateId();
		OWLIndividual ind = f.getOWLNamedIndividual(IRI.create(suggestionSetID));
		//add type SuggestionSet
		OWLAxiom ax = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.SuggestionSet, ind);
		axioms.add(ax);
		
		
		//create instance for algorithm run
		String algorithmRunID = defaultNamespace + generateId();
		OWLIndividual algorithmRunInd = f.getOWLNamedIndividual(IRI.create(algorithmRunID));
		//add type AlgorithmRun
		ax = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.AlgorithmRun, algorithmRunInd);
		axioms.add(ax);
		//generate instance for algorithm
		String algorithmName = algorithm.getAnnotation(ComponentAnn.class).name();
		String algorithmID = "http://dl-learner.org#" + algorithmName.replace(" ", "_");
		OWLIndividual algorithmInd = f.getOWLNamedIndividual(IRI.create(algorithmID));
		//add label to algorithm instance
		OWLAnnotation labelAnno = f.getOWLAnnotation(
				f.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()),
				f.getOWLLiteral(algorithmName));
		ax = f.getOWLAnnotationAssertionAxiom(algorithmInd.asOWLNamedIndividual().getIRI(), labelAnno);
		axioms.add(ax);
		//add version to algorithm
		ax = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.version, algorithmInd, algorithm.getAnnotation(ComponentAnn.class).version());
		axioms.add(ax);
		//add algorithm instance to algorithm run instance
		ax = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.usedAlgorithm,
				algorithmRunInd, algorithmInd);
		axioms.add(ax);
		//add Parameters to algorithm run instance
		OWLIndividual paramInd;
		for(Entry<ConfigOption, Object> entry : parameters.entrySet()){
			paramInd = f.getOWLNamedIndividual(IRI.create(generateId()));
			ax = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.Parameter, paramInd);
			axioms.add(ax);
			ax = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.parameterName, paramInd, entry.getKey().name());
			axioms.add(ax);
			ax = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.parameterValue, paramInd, entry.getValue().toString());
			axioms.add(ax);
		}
		//add timestamp
		ax = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.timestamp, algorithmRunInd, System.currentTimeMillis());
		axioms.add(ax);
		//add used input to algorithm run instance
		try {
			OWLNamedIndividual knowldegeBaseInd = f.getOWLNamedIndividual(IRI.create(ks.getEndpoint().getURL()));
			ax = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.SPARQLEndpoint, knowldegeBaseInd);
			axioms.add(ax);
			if(!ks.getEndpoint().getDefaultGraphURIs().isEmpty()) {
				// TODO: only writes one default graph
				ax = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.defaultGraph, knowldegeBaseInd, f.getOWLNamedIndividual(IRI.create(ks.getEndpoint().getDefaultGraphURIs().iterator().next())));
				axioms.add(ax);
			}
			ax = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.hasInput,
					algorithmRunInd, knowldegeBaseInd);
			axioms.add(ax);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		
		//add algorithm run instance to suggestion set instance via ObjectProperty creator 
		ax = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.creator,
				ind, algorithmRunInd);
		axioms.add(ax);
		
		//add suggestions to suggestions set
		Entry<OWLIndividual, List<OWLAxiom>> ind2Axioms;
		for(EvaluatedAxiom evAx : evalAxioms){
			ind2Axioms = evAx.toRDF(defaultNamespace).entrySet().iterator().next();
			ax = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.hasSuggestion, ind, ind2Axioms.getKey());
			axioms.add(ax);
			axioms.addAll(ind2Axioms.getValue());
		}
		
		
//		printManchesterOWLSyntax(axioms, defaultNamespace);
//		printTurtleSyntax(axioms);
//		printNTriplesSyntax(axioms);
		return axioms;
	}
	
	  private String generateId(){
	    return new BigInteger(130, random).toString(32);
	  }
	
	/*
	 * Write axioms in Manchester OWL Syntax.
	 */
	private void printManchesterOWLSyntax(List<OWLAxiom> axioms, String defaultNamespace){
		try {
			System.out.println("ENRICHMENT[");
			
			ManchesterOWLSyntaxOntologyFormat manSyntaxFormat = new ManchesterOWLSyntaxOntologyFormat();
			manSyntaxFormat.setDefaultPrefix(defaultNamespace);
			manSyntaxFormat.setPrefix("enrichment", "http://www.dl-learner.org/enrichment.owl#");
			
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology(new HashSet<OWLAxiom>(axioms), IRI.create(defaultNamespace + "enrichment"));
			OWLManager.createOWLOntologyManager().saveOntology(ontology, manSyntaxFormat, new SystemOutDocumentTarget());
			
			
			
			System.out.println("]");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}
	
//	private Model getModel(List<OWLAxiom> axioms) {
//		Model model = ModelFactory.createDefaultModel();
//		try {
//			Conversion.OWLAPIOntology2JenaModel(OWLManager.createOWLOntologyManager().createOntology(new HashSet<OWLAxiom>(axioms)), model);
//		} catch (OWLOntologyCreationException e) {
//			e.printStackTrace();
//		}
//		return model;
//	}	
	
	Model getModel(List<OWLAxiom> axioms) {
		Model model = ModelFactory.createDefaultModel();
		try {
			OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology(new HashSet<OWLAxiom>(axioms));
//			String s = new org.aksw.commons.owlapi.StringConverter(ontology).toStringAsRDFXML();System.out.println(s);
			String s = new org.aksw.commons.owlapi.StringConverter(ontology).toStringAsTurtle(); //System.out.println(s);
			ByteArrayInputStream bs = new ByteArrayInputStream(s.getBytes());
	        model.read(bs, "", "TURTLE");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		model.setNsPrefix("enr", "http://www.dl-learner.org/enrichment.owl#");
		return model;
	}	
	
	public OWLOntology getGeneratedOntology(){
		OWLOntology ontology = null;
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			ontology = man.createOntology(learnedOWLAxioms);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ontology;
	}
	
	public OWLOntology getGeneratedOntology(boolean withConfidenceAsAnnotations){
		OWLOntology ontology = null;
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = man.getOWLDataFactory();
			if(withConfidenceAsAnnotations){
				OWLAnnotationProperty confAnnoProp = factory.getOWLAnnotationProperty(IRI.create(EnrichmentVocabulary.NS + "confidence"));
				Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
				for(EvaluatedAxiom evAx : learnedEvaluatedAxioms){
					OWLAxiom ax = OWLAPIAxiomConvertVisitor.convertAxiom(evAx.getAxiom());
					ax = ax.getAnnotatedAxiom(Collections.singleton(
							factory.getOWLAnnotation(confAnnoProp, factory.getOWLLiteral(evAx.getScore().getAccuracy()))));
					axioms.add(ax);
				}
				ontology = man.createOntology(axioms);
			} else {
				ontology = man.createOntology(learnedOWLAxioms);
			}
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ontology;
	}
	
	/*
	 * Write axioms in Turtle syntax.
	 */
	private void printTurtleSyntax(List<OWLAxiom> axioms){
		try {
			System.out.println("ENRICHMENT[");
			Model model = ModelFactory.createDefaultModel();
			Conversion.OWLAPIOntology2JenaModel(OWLManager.createOWLOntologyManager().createOntology(new HashSet<OWLAxiom>(axioms)), model);
			model.write(System.out, "TURTLE");
			System.out.println("]");
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Write axioms in Turtle syntax.
	 */
	private void printNTriplesSyntax(List<OWLAxiom> axioms){
		try {
			System.out.println("ENRICHMENT[");
			Model model = ModelFactory.createDefaultModel();
			Conversion.OWLAPIOntology2JenaModel(OWLManager.createOWLOntologyManager().createOntology(new HashSet<OWLAxiom>(axioms)), model);
			model.write(System.out, "N-TRIPLES");
			System.out.println("]");
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<AlgorithmRun> getAlgorithmRuns() {
		return algorithmRuns;
	}

	public static void main(String[] args) throws IOException, ComponentInitException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, LearningProblemUnsupportedException {
		
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.dllearner").setLevel(Level.WARN); // seems to be needed for some reason (?)
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(consoleAppender);
		
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("h", "?", "help"), "Show help.");
//		parser.acceptsAll(asList("v", "verbose"), "Verbosity level.").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
		parser.acceptsAll(asList("e", "endpoint"), "SPARQL endpoint URL to be used.")
				.withRequiredArg().ofType(URL.class);
		parser.acceptsAll(asList("g", "graph"),
				"URI of default graph for queries on SPARQL endpoint.").withOptionalArg()
				.ofType(URI.class);
		parser.acceptsAll(asList("r", "resource"),
				"The resource for which enrichment axioms should be suggested.").withOptionalArg().ofType(URI.class);
		parser.acceptsAll(asList("o", "output"), "Specify a file where the output can be written.")
				.withOptionalArg().ofType(File.class);
		// TODO: other interestig formats: html, manchester, sparul
		parser.acceptsAll(asList("f", "format"),
				"Format of the generated output (plain, rdf/xml, turtle, n-triples).").withOptionalArg()
				.ofType(String.class).defaultsTo("plain");
		parser.acceptsAll(asList("t", "threshold"),
				"Confidence threshold for suggestions. Set it to a value between 0 and 1.").withOptionalArg() 
				.ofType(Double.class).defaultsTo(0.7);
		parser.acceptsAll(asList("l", "limit"),
		"Maximum number of returned axioms per axiom type. Set it to -1 if all axioms above the threshold should be returned.").withOptionalArg() 
		.ofType(Integer.class).defaultsTo(10);
		parser.acceptsAll(asList("i", "inference"),
				"Specifies whether to use inference. If yes, the schema will be loaded into a reasoner and used for computing the scores.").withOptionalArg().ofType(Boolean.class).defaultsTo(true);
		parser.acceptsAll(asList("s", "serialize"), "Specify a file where the ontology with all axioms can be written.")
		.withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("a", "annotations"),
				"Specifies whether to save scores as annotations.").withOptionalArg().ofType(Boolean.class).defaultsTo(true);
		parser.acceptsAll(asList("chunksize"),
		"Specifies the chunk size for the query result as the approach is incrementally.").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
		parser.acceptsAll(asList("maxExecutionTimeInSeconds"),
		"Specifies the max execution time for each algorithm run and each entity.").withRequiredArg().ofType(Integer.class).defaultsTo(10);
		parser.acceptsAll(asList("omitExistingAxioms"),
				"Specifies whether return only axioms which not already exist in the knowlegde base.").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
		
		// parse options and display a message for the user in case of problems
		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage() + ". Use -? to get help.");
			System.exit(0);
		}

		// print help screen
		if (options.has("?")) {
			parser.printHelpOn(System.out);
			String addHelp = "Additional explanations: The resource specified should " +
			"be a class, object \nproperty or data property. DL-Learner will try to " +
			"automatically detect its \ntype. If no resource is specified, DL-Learner will " +
			"generate enrichment \nsuggestions for all detected classes and properties in " + 
			"the given endpoint \nand graph. This can take several hours.";
			System.out.println();
			System.out.println(addHelp);
			// main script
		} else {	
			// check that endpoint was specified
			if(!options.hasArgument("endpoint")) {
				System.out.println("Please specify a SPARQL endpoint (using the -e option).");
				System.exit(0);
			}			
					
			// create SPARQL endpoint object (check that indeed a URL was given)
			URL endpoint = null;
			try {
				endpoint = (URL) options.valueOf("endpoint");
			} catch(OptionException e) {
				System.out.println("The specified endpoint appears not be a proper URL.");
				System.exit(0);
			}
			URI graph = null;
			try {
				graph = (URI) options.valueOf("graph");
			} catch(OptionException e) {
				System.out.println("The specified graph appears not be a proper URL.");
				System.exit(0);
			}
			URI resourceURI = null;
			try {
				resourceURI = (URI) options.valueOf("resource");
			} catch(OptionException e) {
				System.out.println("The specified resource appears not be a proper URI.");
				System.exit(0);
			}
			LinkedList<String> defaultGraphURIs = new LinkedList<String>();
			if(graph != null) {
				defaultGraphURIs.add(graph.toString());
			}
			SparqlEndpoint se = new SparqlEndpoint(endpoint, defaultGraphURIs, new LinkedList<String>());
			
			// sanity check that endpoint/graph returns at least one triple
			String query = "SELECT * WHERE {?s ?p ?o} LIMIT 1";
			SparqlQuery sq = new SparqlQuery(query, se);
			try {
				ResultSet q = sq.send();
				while (q.hasNext()) {
					q.next();
				}
			} catch(QueryExceptionHTTP e) {
				System.out.println("Endpoint not reachable (check spelling).");
				System.exit(0);
			}
			
			// map resource to correct type
			Entity resource = null;
			if(options.valueOf("resource") != null) {
				resource = new SPARQLTasks(se).guessResourceType(resourceURI.toString(), true);
				if(resource == null) {
					throw new IllegalArgumentException("Could not determine the type (class, object property or data property) of input resource " + options.valueOf("resource"));
				}
			}
			
			boolean useInference = (Boolean) options.valueOf("i");
//			boolean verbose = (Boolean) options.valueOf("v");
			double threshold = (Double) options.valueOf("t");
			int maxNrOfResults = (Integer) options.valueOf("l");
			if(maxNrOfResults == -1){
				maxNrOfResults = Integer.MAX_VALUE;
			}
			
			int chunksize = (Integer) options.valueOf("chunksize");
			int maxExecutionTimeInSeconds = (Integer) options.valueOf("maxExecutionTimeInSeconds");
			boolean omitExistingAxioms = (Boolean) options.valueOf("omitExistingAxioms");
			
			// TODO: some handling for inaccessible files or overwriting existing files
			File f = (File) options.valueOf("o");
			
			// if plain and file option is given, redirect System.out to a file
			if(options.has("o") && (!options.has("f") || options.valueOf("f").equals("plain"))) {
				 PrintStream printStream = new PrintStream(new FileOutputStream(f));
				 System.setOut(printStream);
			}			
			
			Enrichment e = new Enrichment(se, resource, threshold, maxNrOfResults, useInference, false, chunksize, maxExecutionTimeInSeconds, omitExistingAxioms);
			e.start();

			SparqlEndpointKS ks = new SparqlEndpointKS(se);
			
			// print output in correct format
			if(options.has("f")) {
				List<AlgorithmRun> runs = e.getAlgorithmRuns();
				List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
				for(AlgorithmRun run : runs) {
					axioms.addAll(e.toRDF(run.getAxioms(), run.getAlgorithm(), run.getParameters(), ks));
				}
				Model model = e.getModel(axioms);
				if(options.valueOf("f").equals("turtle")) {
					if(options.has("o")) {
						model.write(new FileOutputStream(f), "TURTLE");
					} else {
						System.out.println("ENRICHMENT[");
						model.write(System.out, "TURTLE");
						System.out.println("]");
					}
				} else if(options.valueOf("f").equals("rdf/xml")){
					if(options.has("o")) {
						model.write(new FileOutputStream(f), "RDF/XML");
					} else {
						System.out.println("ENRICHMENT[");
						model.write(System.out, "RDF/XML");
						System.out.println("]");
					}
				} else if(options.valueOf("f").equals("n-triples")){
					if(options.has("o")) {
						model.write(new FileOutputStream(f), "N-TRIPLES");
					} else {
						System.out.println("ENRICHMENT[");
						model.write(System.out, "N-TRIPLES");
						System.out.println("]");
					}
				} 
			}
			//serialize ontology
			if(options.has("s")){
				File file = (File)options.valueOf("s");
				try {
					OWLOntology ontology = e.getGeneratedOntology(options.has("a"));
					OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
					OWLManager.createOWLOntologyManager().saveOntology(ontology, new RDFXMLOntologyFormat(), os);
				} catch (OWLOntologyStorageException e1) {
					throw new Error("Could not save ontology.");
				}
			}
			
			
		}

	}
	

}
