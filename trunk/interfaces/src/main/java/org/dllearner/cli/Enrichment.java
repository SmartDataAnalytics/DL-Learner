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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.dllearner.algorithms.properties.DataPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.DataPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.DisjointDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.DisjointObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.InverseFunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.SubDataPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.TransitiveObjectPropertyAxiomLearner;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblemUnsupportedException;
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
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.CommonPrefixMap;
import org.dllearner.utilities.EnrichmentVocabulary;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
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
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Command Line Interface for Enrichment.
 * 
 * @author Jens Lehmann
 * 
 */
public class Enrichment {

	// data structure for holding the result of an algorithm run
	private class AlgorithmRun {
		
		// we only store the algorithm class and not the learning algorithm object,
		// since otherwise we run into memory problems for full enrichment
		private Class<? extends LearningAlgorithm> algorithm;
		private List<EvaluatedAxiom> axioms;
		private Map<ConfigOption,String> parameters;

		public AlgorithmRun(Class<? extends LearningAlgorithm> algorithm, List<EvaluatedAxiom> axioms, Map<ConfigOption,String> parameters) {
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
		
		public Map<ConfigOption, String> getParameters() {
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

	// number of axioms which will be learned/considered (only applies to
	// some learners)
	private int nrOfAxiomsToLearn = 10;	
	private double threshold = 0.7;
	
	// lists of algorithms to apply
	private List<Class<? extends AxiomLearningAlgorithm>> objectPropertyAlgorithms;
	private List<Class<? extends AxiomLearningAlgorithm>> dataPropertyAlgorithms;
	private List<Class<? extends LearningAlgorithm>> classAlgorithms;
	
	// list of generated axioms while script is running
	private List<AlgorithmRun> algorithmRuns;
	
	private CommonPrefixMap prefixes = new CommonPrefixMap();
	
	public Enrichment(SparqlEndpoint se, Entity resource, double threshold, boolean verbose) {
		this.se = se;
		this.resource = resource;
		this.verbose = verbose;
		this.threshold = threshold;
		
		objectPropertyAlgorithms = new LinkedList<Class<? extends AxiomLearningAlgorithm>>();
		objectPropertyAlgorithms.add(DisjointObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(EquivalentObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(FunctionalObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(InverseFunctionalObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(ObjectPropertyDomainAxiomLearner.class);
		objectPropertyAlgorithms.add(ObjectPropertyRangeAxiomLearner.class);
		objectPropertyAlgorithms.add(SubObjectPropertyOfAxiomLearner.class);
		objectPropertyAlgorithms.add(SymmetricObjectPropertyAxiomLearner.class);
		objectPropertyAlgorithms.add(TransitiveObjectPropertyAxiomLearner.class);

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
	}
	
	@SuppressWarnings("unchecked")
	public void start() throws ComponentInitException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, LearningProblemUnsupportedException, MalformedURLException {
		
		// sanity check that endpoint/graph returns at least one triple
		String query = "SELECT * WHERE {?s ?p ?o} LIMIT 1";
		SparqlQuery sq = new SparqlQuery(query, se);
		ResultSet q = sq.send();
		while (q.hasNext()) {
			q.next();
		}
				
		// instantiate SPARQL endpoint wrapper component
		SparqlEndpointKS ks = new SparqlEndpointKS(se);
		ks.init();
		
		// common helper objects
		SPARQLTasks st = new SPARQLTasks(se);
		
		if(resource == null) {
			// loop over all entities and call appropriate algorithms
			Set<NamedClass> classes = st.getAllClasses();
			for(NamedClass nc : classes) {
				runClassLearningAlgorithms(ks, nc);				
			}
			Set<ObjectProperty> objectProperties = st.getAllObjectProperties();			
			for(ObjectProperty property : objectProperties) {
				runObjectPropertyAlgorithms(ks, property);				
			}
			Set<DatatypeProperty> dataProperties = st.getAllDataProperties();
			for(DatatypeProperty property : dataProperties) {
				runDataPropertyAlgorithms(ks, property);		
			}
		} else {
			if(resource instanceof ObjectProperty) {
				System.out.println(resource + " appears to be an object property. Running appropriate algorithms.");
				runObjectPropertyAlgorithms(ks, (ObjectProperty) resource);
			} else if(resource instanceof DatatypeProperty) {
				System.out.println(resource + " appears to be a data property. Running appropriate algorithms.");
				runDataPropertyAlgorithms(ks, (DatatypeProperty) resource);
			} else if(resource instanceof NamedClass) {
				System.out.println(resource + " appears to be a class. Running appropriate algorithms.");
				runClassLearningAlgorithms(ks, (NamedClass) resource);				
			} else {
				throw new Error("The type " + resource.getClass() + " of resource " + resource + " cannot be handled by this enrichment tool.");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void runClassLearningAlgorithms(SparqlEndpointKS ks, NamedClass nc) throws ComponentInitException {
		System.out.println(resource + " appears to be a class. Running appropriate algorithms.");
		for (Class<? extends LearningAlgorithm> algorithmClass : classAlgorithms) {
			if(algorithmClass == CELOE.class) {
				applyCELOE(ks, nc, false);
				applyCELOE(ks, nc, true);
			} else {
				applyLearningAlgorithm((Class<AxiomLearningAlgorithm>)algorithmClass, ks, nc);
			}
		}
	}
	
	private void runObjectPropertyAlgorithms(SparqlEndpointKS ks, ObjectProperty property) throws ComponentInitException {
		for (Class<? extends AxiomLearningAlgorithm> algorithmClass : objectPropertyAlgorithms) {
			applyLearningAlgorithm(algorithmClass, ks, property);
		}		
	}
	
	private void runDataPropertyAlgorithms(SparqlEndpointKS ks, DatatypeProperty property) throws ComponentInitException {
		for (Class<? extends AxiomLearningAlgorithm> algorithmClass : dataPropertyAlgorithms) {
			applyLearningAlgorithm(algorithmClass, ks, property);
		}		
	}	
	
	private List<EvaluatedAxiom> applyCELOE(SparqlEndpointKS ks, NamedClass nc, boolean equivalence) throws ComponentInitException {
//		SPARQLTasks st = new SPARQLTasks(se);
		
		// get instances of class as positive examples
		SPARQLReasoner sr = new SPARQLReasoner(ks);
		SortedSet<Individual> posExamples = sr.getIndividuals(nc, 20);
		SortedSet<String> posExStr = Helper.getStringSet(posExamples);
		
		// get negative examples via various strategies
//		AutomaticNegativeExampleFinderSPARQL finder = new AutomaticNegativeExampleFinderSPARQL(posExStr, st, null);
//		finder.makeNegativeExamplesFromNearbyClasses(posExStr, 50);
//		finder.makeNegativeExamplesFromParallelClasses(posExStr, 50);
//		finder.makeNegativeExamplesFromRelatedInstances(posExStr, "http://dbpedia.org/resource/");
//		finder.makeNegativeExamplesFromSuperClasses(resource.getName(), 50);
//		finder.makeNegativeExamplesFromRandomInstances();
//		SortedSet<String> negExStr = finder.getNegativeExamples(50, false);
		// use own implementation of negative example finder
		System.out.print("finding negatives ... ");
		AutomaticNegativeExampleFinderSPARQL2 finder = new AutomaticNegativeExampleFinderSPARQL2(ks.getEndpoint());
		SortedSet<String> negExStr = finder.getNegativeExamples(nc.getName(), posExStr);
		negExStr = SetManipulation.fuzzyShrink(negExStr, 20);
		SortedSet<Individual> negExamples = Helper.getIndividualSet(negExStr);
		SortedSetTuple<Individual> examples = new SortedSetTuple<Individual>(posExamples, negExamples);
		
		System.out.println("done (" + negExStr.size()+ ")");
		
        ComponentManager cm = ComponentManager.getInstance();

        SparqlKnowledgeSource ks2 = cm.knowledgeSource(SparqlKnowledgeSource.class);
        ks2.setInstances(Datastructures.individualSetToStringSet(examples.getCompleteSet()));
        ks2.setUrl(ks.getEndpoint().getURL());
        ks2.setDefaultGraphURIs(new TreeSet<String>(ks.getEndpoint().getDefaultGraphURIs()));
        ks2.setUseLits(false);
        ks2.setUseCacheDatabase(true);
        ks2.setRecursionDepth(2);
        ks2.setCloseAfterRecursion(true);
//        ks2.getConfigurator().setSaveExtractedFragment(true);
        System.out.println("getting fragment ... ");
        ks2.init();
        System.out.println("done");

        AbstractReasonerComponent rc = cm.reasoner(FastInstanceChecker.class, ks2);
        rc.init();

        // TODO: super class learning
        ClassLearningProblem lp = cm.learningProblem(ClassLearningProblem.class, rc);
//        lp.setPositiveExamples(posExamples);
//        lp.setNegativeExamples(negExamples);
//        try {
			lp.setClassToDescribe(nc);
//		} catch (MalformedURLException e1) {
//			e1.printStackTrace();
//		}
//        lp.setType("equivalence");
        lp.setEquivalence(true);
//        lp.setAccuracyMethod("fmeasure");
        lp.setHeuristic(HeuristicType.FMEASURE);
        lp.setUseApproximations(false);
        lp.setMaxExecutionTimeInSeconds(10);
        lp.init();

        CELOE la = null;
		try {
			la = cm.learningAlgorithm(CELOE.class, lp, rc);
		} catch (LearningProblemUnsupportedException e) {
			e.printStackTrace();
		}
//        CELOEConfigurator cc = la.getConfigurator();
        la.setMaxExecutionTimeInSeconds(10);
        la.setNoisePercentage(25);
        la.init();
        System.out.print("running CELOE ... ");
        la.start();
        System.out.println("done");		

        // convert the result to axioms (to make it compatible with the other algorithms)
        List<? extends EvaluatedDescription> learnedDescriptions = la.getCurrentlyBestEvaluatedDescriptions(threshold);
        List<EvaluatedAxiom> evaluatedAxioms = new LinkedList<EvaluatedAxiom>();
        for(EvaluatedDescription learnedDescription : learnedDescriptions) {
        	Axiom axiom;
        	if(equivalence) {
        		axiom = new EquivalentClassesAxiom(nc, learnedDescription.getDescription());
        	} else {
        		axiom = new SubClassAxiom(nc, learnedDescription.getDescription());
        	}
        	Score score = lp.computeScore(learnedDescription.getDescription());
        	evaluatedAxioms.add(new EvaluatedAxiom(axiom, score)); 
        }
        
        algorithmRuns.add(new AlgorithmRun(CELOE.class, evaluatedAxioms, ConfigHelper.getConfigOptionValuesString(la)));
        cm.freeAllComponents();		
		return evaluatedAxioms;
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
		learner.init();
		String algName = AnnComponentManager.getName(learner);
		System.out.print("Applying " + algName + " on " + entity + " ... ");
		long startTime = System.currentTimeMillis();
		try {
			learner.start();
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getCause() instanceof SocketTimeoutException){
				System.out.println("Query timed out (endpoint possibly too slow).");
			}						
		}
		long runtime = System.currentTimeMillis() - startTime;
		System.out.println("done in " + runtime + "ms");
		List<EvaluatedAxiom> learnedAxioms = learner
				.getCurrentlyBestEvaluatedAxioms(nrOfAxiomsToLearn, threshold);
		System.out.println(prettyPrint(learnedAxioms));	
		
		algorithmRuns.add(new AlgorithmRun(learner.getClass(), learnedAxioms, ConfigHelper.getConfigOptionValuesString(learner)));
		return learnedAxioms;
	}
	
	private String prettyPrint(List<EvaluatedAxiom> learnedAxioms) {
		String str = "suggested axioms and their score in percent:\n";
		if(learnedAxioms.isEmpty()) {
			return "  no axiom suggested";
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
		if(acc<10d) { accs = " " + accs; }
		if(acc<100d) { accs = " " + accs; }
		String str =  accs + "%\t" + axiom.getAxiom().toManchesterSyntaxString(null, prefixes);
		return str;
	}
	
	/*
	 * Generates list of OWL axioms.
	 */
	private List<OWLAxiom> toRDF(List<EvaluatedAxiom> evalAxioms, Class<? extends LearningAlgorithm> algorithm, Map<ConfigOption,String> parameters, SparqlEndpointKS ks){
		return toRDF(evalAxioms, algorithm, parameters, ks, null);
	}
	
	private List<OWLAxiom> toRDF(List<EvaluatedAxiom> evalAxioms, Class<? extends LearningAlgorithm> algorithm, Map<ConfigOption,String> parameters, SparqlEndpointKS ks, String defaultNamespace){
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
		for(Entry<ConfigOption, String> entry : parameters.entrySet()){
			paramInd = f.getOWLNamedIndividual(IRI.create(generateId()));
			ax = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.Parameter, paramInd);
			axioms.add(ax);
			ax = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.parameterName, paramInd, entry.getKey().name());
			axioms.add(ax);
			ax = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.parameterValue, paramInd, entry.getValue());
			axioms.add(ax);
		}
		//add used input to algorithm run instance
		try {
			OWLNamedIndividual knowldegeBaseInd = f.getOWLNamedIndividual(IRI.create(ks.getEndpoint().getURL()));
			ax = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.SPARQLEndpoint, knowldegeBaseInd);
			axioms.add(ax);
			ax = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.defaultGraph, knowldegeBaseInd, f.getOWLNamedIndividual(IRI.create(ks.getEndpoint().getDefaultGraphURIs().iterator().next())));
			axioms.add(ax);
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
	
	private Model getModel(List<OWLAxiom> axioms) {
		Model model = ModelFactory.createDefaultModel();
		try {
			OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology(new HashSet<OWLAxiom>(axioms));
//			String s = new org.aksw.commons.owlapi.StringConverter(ontology).toStringAsRDFXML();System.out.println(s);
			String s = new org.aksw.commons.owlapi.StringConverter(ontology).toStringAsTurtle();System.out.println(s);
			ByteArrayInputStream bs = new ByteArrayInputStream(s.getBytes());
	        model.read(bs, "", "TURTLE");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return model;
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
		parser.acceptsAll(asList("v", "verbose"), "Verbosity level.").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
		parser.acceptsAll(asList("e", "endpoint"), "SPARQL endpoint URL to be used.")
				.withRequiredArg().ofType(URL.class);
		parser.acceptsAll(asList("g", "graph"),
				"URI of default graph for queries on SPARQL endpoint.").withOptionalArg()
				.ofType(URI.class);
		parser.acceptsAll(asList("r", "resource"),
				"The resource for which enrichment axioms should be suggested.").withOptionalArg().ofType(URI.class);
		parser.acceptsAll(asList("o", "output"), "Specify a file where the output can be written.")
				.withOptionalArg().ofType(File.class);
		parser.acceptsAll(asList("f", "format"),
				"Format of the generated output (plain, html, rdf/xml, turtle, manchester, sparul).").withOptionalArg()
				.ofType(String.class).defaultsTo("plain");
		parser.acceptsAll(asList("t", "threshold"),
				"Confidence threshold for suggestions. Set it to a value between 0 and 1.").withOptionalArg() 
				.ofType(Double.class).defaultsTo(0.7);

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
			// create SPARQL endpoint object
			URL endpoint = (URL) options.valueOf("endpoint");
			URI graph = (URI) options.valueOf("graph");
			LinkedList<String> defaultGraphURIs = new LinkedList<String>();
			if(graph != null) {
				defaultGraphURIs.add(graph.toString());
			}
			SparqlEndpoint se = new SparqlEndpoint(endpoint, defaultGraphURIs, new LinkedList<String>());
			
			// map resource to correct type
			Entity resource = null;
			if(options.valueOf("resource") != null) {
				resource = new SPARQLTasks(se).guessResourceType(((URI)options.valueOf("resource")).toString());
				if(resource == null) {
					throw new IllegalArgumentException("Could not determine the type (class, object property or data property) of input resource " + options.valueOf("resource"));
				}
			}
			
			if(!options.hasArgument("endpoint")) {
				System.out.println("Please specify a SPARQL endpoint (using the -e option).");
			}
			
			boolean verbose = (Boolean) options.valueOf("v");
			double threshold = (Double) options.valueOf("t");
			
			Enrichment e = new Enrichment(se, resource, threshold, verbose);
			e.start();

			SparqlEndpointKS ks = new SparqlEndpointKS(se);
			
			// TODO: some handling for inaccessible files or overwriting existing files
			File f = (File) options.valueOf("o");
			
			// print output in correct format
			if(options.has("f")) {
				// TODO: handle other formats
				if(options.valueOf("f").equals("turtle")) {
					List<AlgorithmRun> runs = e.getAlgorithmRuns();
					List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
					for(AlgorithmRun run : runs) {
						axioms.addAll(e.toRDF(run.getAxioms(), run.getAlgorithm(), run.getParameters(), ks));
					}
					Model model = e.getModel(axioms);
					for(Statement st : model.listStatements().toList()){
						System.out.println("--------------------");
//						System.out.println(st);
						if(st.getSubject().isResource()){
							System.out.println(st.getSubject());
						}
						System.out.println(st.getPredicate());
						if(st.getObject().isResource()){
							
						}
						System.out.println(st.getObject());
					}
					if(options.has("o")) {
						model.write(new FileOutputStream(f));
					} else {
						System.out.println("ENRICHMENT[");
						model.write(System.out);
						System.out.println("]");
					}
				}
			}
			
		}

	}
	

}
