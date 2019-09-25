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

import com.clarkparsia.owlapiv3.XSD;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Sets;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.jena.riot.checker.CheckerLiterals;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.properties.AxiomAlgorithms;
import org.dllearner.algorithms.properties.MultiPropertyAxiomLearner;
import org.dllearner.configuration.spring.editors.ConfigHelper;
import org.dllearner.core.*;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.*;
import org.dllearner.accuracymethods.AccMethodFMeasure;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.EnrichmentVocabulary;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL2;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLEntityTypeAdder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.*;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import static java.util.Arrays.asList;

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
		private List<EvaluatedAxiom<OWLAxiom>> axioms;
		private Map<Field,Object> parameters;

		public AlgorithmRun(Class<? extends LearningAlgorithm> algorithm, List<EvaluatedAxiom<OWLAxiom>> axioms, Map<Field,Object> parameters) {
			this.algorithm = algorithm;
			this.axioms = axioms;
			this.parameters = parameters;
		}

		public Class<? extends LearningAlgorithm> getAlgorithm() {
			return algorithm;
		}

		public List<EvaluatedAxiom<OWLAxiom>> getAxioms() {
			return axioms;
		}

		public Map<Field, Object> getParameters() {
			return parameters;
		}
	}

	private static Logger logger = Logger.getLogger(Enrichment.class);
	private DecimalFormat df = new DecimalFormat("##0.0");
	private static final String DEFAULT_NS = "http://localhost:8080/";

	//used to generate unique random identifiers
	private SecureRandom random = new SecureRandom();

	// enrichment parameters
	private SparqlEndpointKS ks;
	private OWLEntity resource;
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
	private List<String> allowedNamespaces = new ArrayList<>();
	private int maxNrOfPositiveExamples = 20;
	private int maxNrOfNegativeExamples = 20;

	private boolean useInference;
	private SPARQLReasoner reasoner;
	private String cacheDir = "cache";

	// lists of algorithms to apply
	private List<Class<? extends LearningAlgorithm>> classAlgorithms;

	// list of generated axioms while script is running
	private List<AlgorithmRun> algorithmRuns;

//	private CommonPrefixMap prefixes = new CommonPrefixMap();

	// cache for SparqKnowledgeSource
	KnowledgeSource ksCached;
	AbstractReasonerComponent rcCached;

	private Set<OWLAxiom> learnedOWLAxioms;
	private Set<EvaluatedAxiom> learnedEvaluatedAxioms;
	private boolean processPropertiesTypeInferred = false;
	private boolean iterativeMode = false;

	private boolean processObjectProperties;
	private boolean processDataProperties;
	private boolean processClasses;

	AxiomLearningProgressMonitor progressMonitor = new ConsoleAxiomLearningProgressMonitor();

	private OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	public Enrichment(SparqlEndpoint se, OWLEntity resource, double threshold, int nrOfAxiomsToLearn,
			boolean useInference, boolean verbose, int chunksize, int maxExecutionTimeInSeconds,
			boolean omitExistingAxioms) {
		this(new SparqlEndpointKS(se), resource, threshold, nrOfAxiomsToLearn, useInference, verbose, chunksize,
				maxExecutionTimeInSeconds, omitExistingAxioms);
	}

	public Enrichment(SparqlEndpointKS ks, OWLEntity resource, double threshold, int nrOfAxiomsToLearn,
			boolean useInference, boolean verbose, int chunksize,
			int maxExecutionTimeInSeconds, boolean omitExistingAxioms) {
		this.ks = ks;
		this.resource = resource;
		this.verbose = verbose;
		this.threshold = threshold;
		this.nrOfAxiomsToLearn = nrOfAxiomsToLearn;
		this.useInference = useInference;
		this.chunksize = chunksize;
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
		this.omitExistingAxioms = omitExistingAxioms;

		try {
			ks.init();
		} catch (ComponentInitException e1) {
			e1.printStackTrace();
		}
		
		if(ks.isRemote()){
			try {
				cacheDir = "cache" + File.separator + URLEncoder.encode(ks.getEndpoint().getURL().toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		classAlgorithms = new LinkedList<>();
//		classAlgorithms.add(DisjointClassesLearner.class);
//		classAlgorithms.add(SimpleSubclassLearner.class);
		classAlgorithms.add(CELOE.class);

		algorithmRuns = new LinkedList<>();

		learnedOWLAxioms = new HashSet<>();
		learnedEvaluatedAxioms = new HashSet<>();
	}

	public void setAllowedNamespaces(List<String> allowedNamespaces) {
		this.allowedNamespaces = allowedNamespaces;
	}

	/**
	 * @param iterativeMode the iterativeMode to set
	 */
	public void setIterativeMode(boolean iterativeMode) {
		this.iterativeMode = iterativeMode;
	}

	public EntityType<? extends OWLEntity> getEntityType(String resourceURI) {
		EntityType<? extends OWLEntity> entityType = reasoner.getOWLEntityType(resourceURI);
		if(entityType != null){
			return entityType;
		} else {
			throw new IllegalArgumentException("Could not detect type of entity");
		}
	}

	public void start() throws ComponentInitException, IllegalArgumentException, SecurityException {
		reasoner = new SPARQLReasoner(ks);
		reasoner.init();

		if(useInference){
			System.out.print("Precomputing subsumption hierarchy ... ");
			long startTime = System.currentTimeMillis();
			reasoner.prepareSubsumptionHierarchy();
			System.out.println("done in " + (System.currentTimeMillis() - startTime) + " ms");
		}

		if(resource == null) {

			// loop over all entities and call appropriate algorithms
			Set<OWLProperty> processedProperties = new HashSet<>();
			if(processClasses){
				Set<OWLClass> classes = reasoner.getOWLClasses();
				filterByNamespaces(classes);
				processClasses(classes);
			}

			// process object properties
			if(processObjectProperties){
				Set<OWLObjectProperty> objectProperties = reasoner.getOWLObjectProperties();
				filterByNamespaces(objectProperties);
				processProperties(objectProperties, AxiomAlgorithms.getAxiomTypes(EntityType.OBJECT_PROPERTY));
				processedProperties.addAll(objectProperties);
			}

			// process data properties
			if(processDataProperties){
				Set<OWLDataProperty> dataProperties = reasoner.getOWLDataProperties();
				filterByNamespaces(dataProperties);
				processProperties(dataProperties, AxiomAlgorithms.getAxiomTypes(EntityType.DATA_PROPERTY));
				processedProperties.addAll(dataProperties);
			}

		} else {
			System.out.println(resource + " appears to be a" + (resource.isOWLObjectProperty() ? "n " : " ")
					+ resource.getEntityType().getPrintName().toLowerCase()
					+ ". Running appropriate algorithms.\n");
			if(resource instanceof OWLObjectProperty) {
				processProperties(Collections.singleton(resource.asOWLObjectProperty()), AxiomAlgorithms.getAxiomTypes(EntityType.OBJECT_PROPERTY));
			} else if(resource instanceof OWLDataProperty) {
				processProperties(Collections.singleton(resource.asOWLDataProperty()), AxiomAlgorithms.getAxiomTypes(EntityType.DATA_PROPERTY));
			} else if(resource instanceof OWLClass) {
				processClasses(Collections.singleton(resource.asOWLClass()));
			} else {
				throw new Error("The type " + resource.getClass() + " of resource " + resource + " cannot be handled by this enrichment tool.");
			}
		}
	}

	private void processClasses(Set<OWLClass> classes) {
		for(OWLClass cls : classes) {
			try {
				runClassLearningAlgorithms(ks, cls);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void processProperties(Set<? extends OWLProperty> properties, Set<AxiomType<? extends OWLAxiom>> axiomTypes){
		MultiPropertyAxiomLearner la = new MultiPropertyAxiomLearner(ks);
//		la.setUseSampling(true);
		la.setProgressMonitor(progressMonitor);
		la.setAxiomTypes(axiomTypes);
		for(OWLProperty property : properties) {
			System.out.println("Processing property " + property.toStringID());
			la.setEntityToDescribe(property);
			la.start();

			for (AxiomType<? extends OWLAxiom> axiomType : axiomTypes) {

				List<EvaluatedAxiom<OWLAxiom>> evaluatedAxioms = la.getCurrentlyBestEvaluatedAxioms(axiomType, threshold);
				learnedEvaluatedAxioms.addAll(evaluatedAxioms);

				AbstractAxiomLearningAlgorithm algorithm = la.getAlgorithm(axiomType);
				
				if(algorithm != null) {
					AlgorithmRun algorithmRun = new AlgorithmRun(
							AxiomAlgorithms.getAlgorithmClass(axiomType),
							evaluatedAxioms,
							ConfigHelper.getConfigOptionValues(la.getAlgorithm(axiomType)));
					algorithmRuns.add(algorithmRun);
				} else {
					// TODO what to do when algorithm failed
				}
				
			}
		}
	}

	private <T extends OWLEntity> void filterByNamespaces(Collection<T> entities){
		if(allowedNamespaces != null && !allowedNamespaces.isEmpty()){
			for (Iterator<T> iterator = entities.iterator(); iterator.hasNext();) {
				T entity = iterator.next();
				boolean startsWithAllowedNamespace = false;
				for (String ns : allowedNamespaces) {
					if(entity.toStringID().startsWith(ns)){
						startsWithAllowedNamespace = true;
						break;
					}
				}
				if(!startsWithAllowedNamespace){
					iterator.remove();
				}
			}
		}
	}

	private void filterByNamespaces(Model model){
		List<Statement> toRemove = new ArrayList<>();
		if(allowedNamespaces != null && !allowedNamespaces.isEmpty()){
			for (StmtIterator iterator = model.listStatements(); iterator.hasNext();) {
				Statement st = iterator.next();
				Property predicate = st.getPredicate();
				RDFNode object = st.getObject();
				boolean startsWithAllowedNamespace = false;
				if(predicate.equals(RDF.type) || predicate.equals(OWL.equivalentClass)){
					if(object.isURIResource()){
						for (String ns : allowedNamespaces) {
							if(object.asResource().getURI().startsWith(ns)){
								startsWithAllowedNamespace = true;
								break;
							}
						}
					} else {
						startsWithAllowedNamespace = true;
					}
				} else {
					for (String ns : allowedNamespaces) {
						if(predicate.getURI().startsWith(ns)){
							startsWithAllowedNamespace = true;
							break;
						}
					}
				}
				if(!startsWithAllowedNamespace){
					toRemove.add(st);
				}
			}
		}
		model.remove(toRemove);
	}

	@SuppressWarnings("unchecked")
	private void runClassLearningAlgorithms(SparqlEndpointKS ks, OWLClass nc) throws ComponentInitException {
		System.out.println("Running algorithms for class " + nc);
		for (Class<? extends LearningAlgorithm> algorithmClass : classAlgorithms) {
			if(algorithmClass == CELOE.class) {
//				applyCELOE(ks, nc, false, false);
//				applyCELOE(ks, nc, true, true);
				applyCELOE(ks, nc, true, false);
			} else {
				applyLearningAlgorithm((Class<AxiomLearningAlgorithm>)algorithmClass, ks, nc);
			}
		}
	}

	private List<EvaluatedAxiom<OWLAxiom>> applyCELOE(SparqlEndpointKS ks, OWLClass nc, boolean equivalence, boolean reuseKnowledgeSource) throws ComponentInitException {
		// get instances of class as positive examples
		System.out.print("finding positives ... ");
		long startTime = System.currentTimeMillis();
		SortedSet<OWLIndividual> posExamples = reasoner.getIndividuals(nc, maxNrOfPositiveExamples);
		long runTime = System.currentTimeMillis() - startTime;
		if(posExamples.isEmpty()){
			System.out.println("Skipping CELOE because class " + nc.toString() + " is empty.");
			return Collections.emptyList();
		}
		SortedSet<String> posExStr = Helper.getStringSet(posExamples);
		System.out.println("done (" + posExStr.size()+ " examples found in " + runTime + " ms)");

		// use own implementation of negative example finder
		System.out.print("finding negatives ... ");
		startTime = System.currentTimeMillis();
		AutomaticNegativeExampleFinderSPARQL2 finder = new AutomaticNegativeExampleFinderSPARQL2(reasoner);
		SortedSet<OWLIndividual> negExamples = finder.getNegativeExamples(nc, posExamples, maxNrOfNegativeExamples);
		SortedSetTuple<OWLIndividual> examples = new SortedSetTuple<>(posExamples, negExamples);
		runTime = System.currentTimeMillis() - startTime;
		System.out.println("done (" + negExamples.size()+ " examples found in " + runTime + " ms)");

		AbstractReasonerComponent rc;
		KnowledgeSource ksFragment;
		if(reuseKnowledgeSource){
			ksFragment = ksCached;
			rc = rcCached;
		} else {
			System.out.print("extracting fragment ... ");//org.apache.jena.shared.impl.JenaParameters.enableEagerLiteralValidation = true;
			startTime = System.currentTimeMillis();
			Model model;
			if(ks.isRemote()){
//				model = getFragmentMultithreaded(ks, Sets.union(posExamples, negExamples));
				model = getFragment(ks, Sets.union(posExamples, negExamples));
			} else {
				model = ((LocalModelBasedSparqlEndpointKS)ks).getModel();
			}

			filter(model);
			filterByNamespaces(model);
			OWLEntityTypeAdder.addEntityTypes(model);

			runTime = System.currentTimeMillis() - startTime;
			System.out.println("done (" + model.size()+ " triples found in " + runTime + " ms)");
			OWLOntology ontology = asOWLOntology(model);
			if(reasoner.getClassHierarchy() != null){
				ontology.getOWLOntologyManager().addAxioms(ontology, reasoner.getClassHierarchy().toOWLAxioms());
			}
			ksFragment = new OWLAPIOntology(ontology);
			try {
				OWLManager.createOWLOntologyManager().saveOntology(ontology, new TurtleDocumentFormat(), new FileOutputStream(System.getProperty("java.io.tmpdir") + File.separator + "test.ttl"));
			} catch (OWLOntologyStorageException | FileNotFoundException e) {
				e.printStackTrace();
			}
//			ksFragment.init();
			System.out.println("Init reasoner");
			rc = new ClosedWorldReasoner(ksFragment);
			rc.init();
			System.out.println("Finished init reasoner");
//			rc.setSubsumptionHierarchy(reasoner.getClassHierarchy());
			ksCached = ksFragment;
			rcCached = rc;
//			for (Individual ind : posExamples) {
//				System.out.println(ResultSetFormatter.asText(org.apache.jena.query.QueryExecutionFactory.create("SELECT * WHERE {<" + ind.getName() + "> ?p ?o. OPTIONAL{?o a ?o_type}}",model).execSelect()));
//			}
		}

        ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(nc);
        lp.setEquivalence(equivalence);
        lp.setAccuracyMethod(new AccMethodFMeasure(true));
        lp.setMaxExecutionTimeInSeconds(10);
        lp.init();

        CELOE la = new CELOE(lp, rc);
        la.setMaxExecutionTimeInSeconds(10);
        la.setNoisePercentage(25);
        la.setMaxNrOfResults(100);
        la.init();
//        ((RhoDRDown)la.getOperator()).setUseNegation(false);
        startTime = System.currentTimeMillis();
        System.out.print("running CELOE (for " + (equivalence ? "equivalent classes" : "sub classes") + ") ... ");
        la.start();
        runTime = System.currentTimeMillis() - startTime;
        System.out.println("done in " + runTime + " ms");

        // convert the result to axioms (to make it compatible with the other algorithms)
        List<? extends EvaluatedDescription<? extends Score>> learnedDescriptions = la.getCurrentlyBestEvaluatedDescriptions(threshold);
        List<EvaluatedAxiom<OWLAxiom>> learnedAxioms = new LinkedList<>();
        for(EvaluatedDescription<? extends Score> learnedDescription : learnedDescriptions) {
        	OWLAxiom axiom;
        	if(equivalence) {
        		axiom = dataFactory.getOWLEquivalentClassesAxiom(nc, learnedDescription.getDescription());
        	} else {
        		axiom = dataFactory.getOWLSubClassOfAxiom(nc, learnedDescription.getDescription());
        	}
        	Score score = lp.computeScore(learnedDescription.getDescription());
        	learnedAxioms.add(new EvaluatedAxiom<>(axiom, new AxiomScore(score.getAccuracy())));
        }
        System.out.println(prettyPrint(learnedAxioms));
        learnedEvaluatedAxioms.addAll(learnedAxioms);
        algorithmRuns.add(new AlgorithmRun(CELOE.class, learnedAxioms, ConfigHelper.getConfigOptionValues(la)));
		return learnedAxioms;
	}

	private Model getFragment(SparqlEndpointKS ks, Set<OWLIndividual> individuals){
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
		Model model = ModelFactory.createDefaultModel();
		for(OWLIndividual ind : individuals){
			Model cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), 2);
			model.add(cbd);
		}
		return model;
	}

	private Model getFragmentMultithreaded(final SparqlEndpointKS ks, Set<OWLIndividual> individuals){
		Model model = ModelFactory.createDefaultModel();
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Future<Model>> futures = new ArrayList<>();
		for (final OWLIndividual ind : individuals) {
			futures.add(threadPool.submit(new Callable<Model>() {
				@Override
				public Model call() throws Exception {
					ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
					return cbdGen.getConciseBoundedDescription(ind.toStringID(), 2);
				}
			}));
		}
		for (Future<Model> future : futures) {
			try {
				model.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		threadPool.shutdown();
		return model;
	}

	private List<EvaluatedAxiom<OWLAxiom>> applyLearningAlgorithm(Class<? extends AxiomLearningAlgorithm> algorithmClass, SparqlEndpointKS ks, OWLEntity entity) throws ComponentInitException {
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
		List<EvaluatedAxiom<OWLAxiom>> learnedAxioms = learner
				.getCurrentlyBestEvaluatedAxioms(nrOfAxiomsToLearn, threshold);
		System.out.println(prettyPrint(learnedAxioms));
		learnedEvaluatedAxioms.addAll(learnedAxioms);
		for(EvaluatedAxiom<OWLAxiom> evAx : learnedAxioms){
			learnedOWLAxioms.add(evAx.getAxiom());
		}

		algorithmRuns.add(new AlgorithmRun(learner.getClass(), learnedAxioms, ConfigHelper.getConfigOptionValues(learner)));
		return learnedAxioms;
	}

	private String prettyPrint(List<EvaluatedAxiom<OWLAxiom>> learnedAxioms) {
		String str = "suggested axioms and their score in percent:\n";
		if(learnedAxioms.isEmpty()) {
			return "  no axiom suggested\n";
		} else {
			for (EvaluatedAxiom<OWLAxiom> learnedAxiom : learnedAxioms) {
				str += " " + prettyPrint(learnedAxiom) + "\n";
			}
		}
		return str;
	}

	private String prettyPrint(EvaluatedAxiom<OWLAxiom> axiom) {
		double acc = axiom.getScore().getAccuracy() * 100;
		String accs = df.format(acc);
		if(accs.length()==3) { accs = "  " + accs; }
		if(accs.length()==4) { accs = " " + accs; }
		String str =  accs + "%\t" + OWLAPIRenderers.toManchesterOWLSyntax(axiom.getAxiom());
		return str;
	}

	/*
	 * Generates list of OWL axioms.
	 */
	List<OWLAxiom> toRDF(List<EvaluatedAxiom<OWLAxiom>> evalAxioms, Class<? extends LearningAlgorithm> algorithm, Map<Field, Object> parameters, SparqlEndpointKS ks){
		return toRDF(evalAxioms, algorithm, parameters, ks, null);
	}

	private List<OWLAxiom> toRDF(List<EvaluatedAxiom<OWLAxiom>> evalAxioms, Class<? extends LearningAlgorithm> algorithm, Map<Field,Object> parameters, SparqlEndpointKS ks, String defaultNamespace){
		if(defaultNamespace == null || defaultNamespace.isEmpty()){
			defaultNamespace = DEFAULT_NS;
		}
		List<OWLAxiom> axioms = new ArrayList<>();

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
		String algorithmName = AnnComponentManager.getName(algorithm);
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
		for(Entry<Field, Object> entry : parameters.entrySet()){
			paramInd = f.getOWLNamedIndividual(IRI.create(generateId()));
			ax = f.getOWLClassAssertionAxiom(EnrichmentVocabulary.Parameter, paramInd);
			axioms.add(ax);
			ax = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.parameterName, paramInd, AnnComponentManager.getName(entry.getKey()));
			axioms.add(ax);
			ax = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.parameterValue, paramInd, entry.getValue().toString());
			axioms.add(ax);
		}
		//add timestamp
		ax = f.getOWLDataPropertyAssertionAxiom(EnrichmentVocabulary.timestamp, algorithmRunInd, System.currentTimeMillis());
		axioms.add(ax);

		//add used input to algorithm run instance
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

		//add algorithm run instance to suggestion set instance via ObjectProperty creator
		ax = f.getOWLObjectPropertyAssertionAxiom(EnrichmentVocabulary.creator,
				ind, algorithmRunInd);
		axioms.add(ax);

		//add suggestions to suggestions set
		Entry<OWLIndividual, List<OWLAxiom>> ind2Axioms;
		for(EvaluatedAxiom evAx : evalAxioms){
			Map<OWLIndividual, List<OWLAxiom>> map = evAx.toRDF(defaultNamespace);
			ind2Axioms = map.entrySet().iterator().next();
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

			ManchesterSyntaxDocumentFormat manSyntaxFormat = new ManchesterSyntaxDocumentFormat();
			manSyntaxFormat.setDefaultPrefix(defaultNamespace);
			manSyntaxFormat.setPrefix("enrichment", "http://www.dl-learner.org/enrichment.owl#");

			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology(new HashSet<>(axioms), IRI.create(defaultNamespace + "enrichment"));
			OWLManager.createOWLOntologyManager().saveOntology(ontology, manSyntaxFormat, new SystemOutDocumentTarget());

			System.out.println("]");
		} catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
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

	private OWLOntology asOWLOntology(Model model) {
		try {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream("bug.ttl");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			model.write(baos, "TURTLE", null);
			model.write(fos, "TURTLE", null);
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(baos.toByteArray()));
			return ontology;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			try {
				model.write(new FileOutputStream("parse-error.ttl"), "TURTLE", null);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	private void filter(Model model) {
		// filter out triples with String literals, as therein often occur
		// some syntax errors and they are not relevant for learning
		List<Statement> statementsToRemove = new ArrayList<>();
		List<Statement> statementsToAdd = new ArrayList<>();
		for (Statement st : model.listStatements().toList()) {
			RDFNode subject = st.getSubject();
			RDFNode object = st.getObject();

			if (object.isAnon()) {
				if (!model.listStatements(object.asResource(), null, (RDFNode) null).hasNext()) {
					statementsToRemove.add(st);
				}
			} else if (st.getPredicate().equals(RDF.type) &&
					(object.equals(RDFS.Class.asNode()) || object.equals(OWL.Class.asNode()) || object.equals(RDFS.Literal.asNode()))) {
				//remove statements like <x a owl:Class>
				statementsToRemove.add(st);
			} else {
				// fix URIs with spaces
				Resource newSubject = (Resource) subject;
				RDFNode newObject = object;
				boolean validTriple = true;
				if (subject.isURIResource()) {
					String uri = subject.asResource().getURI();
					if (uri.contains(" ")) {
						newSubject = model.createResource(uri.replace(" ", ""));
					}
				}
				if (object.isURIResource()) {
					String uri = object.asResource().getURI();
					if (uri.contains(" ")) {
						newObject = model.createResource(uri.replace(" ", ""));
					}
				}
				if (object.isLiteral()) {
					Literal lit = object.asLiteral();
					if (lit.getDatatype() == null || lit.getDatatype().equals(XSD.STRING)) {
						newObject = model.createLiteral("shortened", "en");
					}
					validTriple = CheckerLiterals.checkLiteral(object.asNode(), ErrorHandlerFactory.errorHandlerNoLogging, 1L, 1L);
				}
				if (validTriple) {
					statementsToAdd.add(model.createStatement(newSubject, st.getPredicate(), newObject));
				}
				statementsToRemove.add(st);
			}

		}
		model.remove(statementsToRemove);
		model.add(statementsToAdd);
	}

	Model getModel(List<OWLAxiom> axioms) {
		try {
			OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology(new HashSet<>(axioms));
			Model model = OwlApiJenaUtils.getModel(ontology);
			model.setNsPrefix("enr", "http://www.dl-learner.org/enrichment.owl#");
			return model;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return null;
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

			OWLAnnotationProperty confAnnoProp = factory.getOWLAnnotationProperty(IRI.create(EnrichmentVocabulary.NS
					+ "confidence"));
			Set<OWLAxiom> axioms = new HashSet<>();
			for (EvaluatedAxiom evAx : learnedEvaluatedAxioms) {
				OWLAxiom ax = evAx.getAxiom();
				if (withConfidenceAsAnnotations) {
					ax = ax.getAnnotatedAxiom(Collections.singleton(factory.getOWLAnnotation(confAnnoProp,
							factory.getOWLLiteral(evAx.getScore().getAccuracy()))));
				}
				axioms.add(ax);
			}
			ontology = man.createOntology(axioms);

		} catch (OWLOntologyCreationException e) {
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
			Model model = OwlApiJenaUtils.getModel(OWLManager.createOWLOntologyManager().createOntology(new HashSet<>(axioms)));
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
			Model model = OwlApiJenaUtils.getModel(OWLManager.createOWLOntologyManager().createOntology(new HashSet<>(axioms)));
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

	/**
	 * @param processClasses the processClasses to set
	 */
	public void setProcessClasses(boolean processClasses) {
		this.processClasses = processClasses;
	}

	/**
	 * @param processDataProperties the processDataProperties to set
	 */
	public void setProcessDataProperties(boolean processDataProperties) {
		this.processDataProperties = processDataProperties;
	}

	/**
	 * @param processObjectProperties the processObjectProperties to set
	 */
	public void setProcessObjectProperties(boolean processObjectProperties) {
		this.processObjectProperties = processObjectProperties;
	}

	/**
	 * @param processPropertiesTypeInferred the processPropertiesTypeInferred to set
	 */
	public void setProcessPropertiesTypeInferred(boolean processPropertiesTypeInferred) {
		this.processPropertiesTypeInferred = processPropertiesTypeInferred;
	}

	public static void main(String[] args) throws IOException, ComponentInitException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, LearningProblemUnsupportedException {
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
		// TODO: other interesting formats: html, manchester, sparul
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
				"Specifies whether to use inference. If yes, the schema will be loaded into a reasoner and used for computing the scores.").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
		parser.acceptsAll(asList("iterative"),
				"Specifies whether to use local fragments or single query mode.").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
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
		OptionSpec<String> allowedNamespacesOption = parser.accepts( "ns" ).withRequiredArg().ofType( String.class )
	            .withValuesSeparatedBy( ',' );

		parser.acceptsAll(asList("op"),
				"Specifies whether to compute axiom for object properties.").withOptionalArg().ofType(Boolean.class).defaultsTo(true);
		parser.acceptsAll(asList("dp"),
				"Specifies whether to compute axiom for data properties.").withOptionalArg().ofType(Boolean.class).defaultsTo(true);
		parser.acceptsAll(asList("cls"),
				"Specifies whether compute axiom for classes.").withOptionalArg().ofType(Boolean.class).defaultsTo(true);

		//username and password if endpoint is protected
		parser.acceptsAll(asList("u", "username"), "Specify the username.")
		.withOptionalArg().ofType(String.class);
		parser.acceptsAll(asList("pw", "password"), "Specify the password.")
		.withOptionalArg().ofType(String.class);

		// parse options and display a message for the user in case of problems
		OptionSet options = null;

		if (args.length == 0) {
		    parser.printHelpOn(System.out);
		    System.exit(0);
		}

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

			SparqlEndpointKS ks = null;
			// create SPARQL endpoint object (check that indeed a URL was given)
			URL endpoint = null;
			try {
				endpoint = (URL) options.valueOf("endpoint");
			} catch(OptionException e) {
				System.out.println("The specified endpoint appears not be a proper URL.");
				System.exit(0);
			}
			//check if the URL is a file and if exists load it into a JENA model
			try {
				if(isLocalFile(endpoint)){
					File file = new File(endpoint.toURI());
					if(file.exists()){
						Model kbModel = ModelFactory.createDefaultModel();
						kbModel.read(new FileInputStream(file), null);
						ks = new LocalModelBasedSparqlEndpointKS(kbModel);
					}
				} else {
					URI graph = null;
					try {
						graph = (URI) options.valueOf("graph");
					} catch(OptionException e) {
						System.out.println("The specified graph appears not be a proper URL.");
						System.exit(0);
					}

					LinkedList<String> defaultGraphURIs = new LinkedList<>();
					if(graph != null) {
						defaultGraphURIs.add(graph.toString());
					}
					SparqlEndpoint se = new SparqlEndpoint(endpoint, defaultGraphURIs, new LinkedList<>());
//					Path tempDirectory = Files.createTempDirectory("dllearner");
					String cacheDir = System.getProperty("java.io.tmpdir") + File.separator + "dl-learner";
					ks = new SparqlEndpointKS(se, cacheDir);
				}
				ks.init();
			} catch (URISyntaxException e2) {
				e2.printStackTrace();
			}

			URI resourceURI = null;
			try {
				resourceURI = (URI) options.valueOf("resource");
			} catch(OptionException e) {
				System.out.println("The specified resource appears not be a proper URI.");
				System.exit(0);
			}
			//set credentials if needed
			if(options.has("username") && options.has("password")){
				final String username = (String) options.valueOf("username");
				final String password = (String) options.valueOf("password");
				Authenticator.setDefault (new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password.toCharArray());
					}
				});
			}

			if(ks.isRemote()){
				// sanity check that endpoint/graph returns at least one triple
				String query = "SELECT * WHERE {?s ?p ?o} LIMIT 1";
				SparqlQuery sq = new SparqlQuery(query, ks.getEndpoint());
				try {
					ResultSet q = sq.send();
					while (q.hasNext()) {
						q.next();
					}
				} catch(QueryExceptionHTTP e) {
					System.out.println("Endpoint not reachable (check spelling).");
					System.exit(0);
				}
			}

			// map resource to correct type
			OWLEntity resource = null;
			if(options.valueOf("resource") != null) {
				resource = new SPARQLTasks(ks.getEndpoint()).guessResourceType(resourceURI.toString(), true);
				if(resource == null) {
					throw new IllegalArgumentException("Could not determine the type (class, object property or data property) of input resource " + options.valueOf("resource")
							+ ". Enrichment only works for classes and properties.");
				}
			}

			boolean useInference = (Boolean) options.valueOf("i");
			boolean iterativeMode = (Boolean) options.valueOf("iterative");
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

			//extract namespaces to which the analyzed entities will be restricted
			List<String> allowedNamespaces = options.valuesOf(allowedNamespacesOption);

			//check which entity types we have to process
			boolean processObjectProperties = (Boolean) options.valueOf("op");
			boolean processDataProperties = (Boolean) options.valueOf("dp");
			boolean processClasses = (Boolean) options.valueOf("cls");

			Enrichment e = new Enrichment(ks, resource, threshold, maxNrOfResults, useInference, false, chunksize, maxExecutionTimeInSeconds, omitExistingAxioms);
			e.setAllowedNamespaces(allowedNamespaces);
			e.setIterativeMode(iterativeMode);
			e.setProcessObjectProperties(processObjectProperties);
			e.setProcessDataProperties(processDataProperties);
			e.setProcessClasses(processClasses);
			e.start();

			// print output in correct format
			if(options.has("f")) {
				List<AlgorithmRun> runs = e.getAlgorithmRuns();
				List<OWLAxiom> axioms = new LinkedList<>();
				for(AlgorithmRun run : runs) {
					axioms.addAll(e.toRDF(run.getAxioms(), run.getAlgorithm(), run.getParameters(), ks));
				}
				Model model = e.getModel(axioms);
				OutputStream os = options.has("o") ? new FileOutputStream((File)options.valueOf("o")) : System.out;

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
					OWLManager.createOWLOntologyManager().saveOntology(ontology, new RDFXMLDocumentFormat(), os);
				} catch (OWLOntologyStorageException e1) {
					throw new Error("Could not save ontology.");
				}
			}

		}

	}

	/** Whether the URL is a file in the local file system. */
	public static boolean isLocalFile(java.net.URL url) {
		String scheme = url.getProtocol();
		return "file".equalsIgnoreCase(scheme) && !hasHost(url);
	}

	public static boolean hasHost(java.net.URL url) {
		String host = url.getHost();
		return host != null && !"".equals(host);
	}

}
